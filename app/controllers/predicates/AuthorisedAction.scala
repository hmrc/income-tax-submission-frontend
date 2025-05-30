/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers.predicates

import common.{EnrolmentIdentifiers, EnrolmentKeys}
import config.{AppConfig, ErrorHandler}
import controllers.routes
import models.User
import models.errors.MissingAgentClientDetails
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Results._
import play.api.mvc._
import services.{AuthService, SessionDataService}
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{affinityGroup, allEnrolments, confidenceLevel}
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.{EnrolmentHelper, SessionDataHelper}
import views.html.authErrorPages.{AgentAuthErrorPageView, SupportingAgentAuthErrorPageView}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AuthorisedAction @Inject()(
                                 val agentAuthErrorPage: AgentAuthErrorPageView,
                                 val supportingAgentAuthErrorPageView: SupportingAgentAuthErrorPageView,
                                 errorHandler: ErrorHandler,
                                 sessionDataService: SessionDataService)
                                (implicit val authService: AuthService,
                                 val appConfig: AppConfig,
                                 val mcc: MessagesControllerComponents
                                ) extends ActionBuilder[User, AnyContent] with I18nSupport with SessionDataHelper {

  implicit val executionContext: ExecutionContext = mcc.executionContext
  implicit val messagesApi: MessagesApi = mcc.messagesApi

  val minimumConfidenceLevel: Int = ConfidenceLevel.L250.level

  override def parser: BodyParser[AnyContent] = mcc.parsers.default

  private val signInRedirectFutureResult: Future[Result] = Future.successful(Redirect(appConfig.signInUrl))

  override def invokeBlock[A](request: Request[A], block: User[A] => Future[Result]): Future[Result] = {

    implicit val req: Request[A] = request
    implicit lazy val headerCarrier: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request,request.session)
    withSessionId { sessionId =>
      authService.authorised().retrieve(affinityGroup) {
        case Some(AffinityGroup.Agent) => agentAuthentication(block, sessionId)(request, headerCarrier)
        case Some(individualUser) => individualAuthentication(block, individualUser, sessionId)(request, headerCarrier)
        case _ => Future.successful(Redirect(controllers.routes.UnauthorisedUserErrorController.show))
      } recover {
        case _: NoActiveSession =>
          Redirect(appConfig.signInUrl)
        case _: AuthorisationException =>
          logger.warn(s"[AuthorisedAction][invokeBlock] - User failed to authenticate")
          Redirect(controllers.routes.UnauthorisedUserErrorController.show)
        case e =>
          logger.error(s"[AuthorisedAction][invokeBlock] - Unexpected exception of type '${e.getClass.getSimpleName}' was caught.")
          errorHandler.internalServerError()(request)
      }
    }
  }

  def individualAuthentication[A](block: User[A] => Future[Result],
                                  individualUser: AffinityGroup,
                                  sessionId: String
                                  )(implicit request: Request[A], hc: HeaderCarrier): Future[Result] = {
    authService.authorised().retrieve(allEnrolments and confidenceLevel) {
      case enrolments ~ userConfidence if userConfidence.level >= minimumConfidenceLevel =>
        (
          EnrolmentHelper.getEnrolmentValueOpt(EnrolmentKeys.Individual, EnrolmentIdentifiers.individualId, enrolments),
          EnrolmentHelper.getEnrolmentValueOpt(EnrolmentKeys.nino, EnrolmentIdentifiers.ninoId, enrolments)
        ) match {
          case (Some(mtdItId), Some(nino)) =>
            block(User(mtdItId, None, nino, individualUser.toString, sessionId))
          case (_, None) =>
            logger.warn(s"[AuthorisedAction][individualAuthentication] - No active session. Redirecting to ${appConfig.signInUrl}")
            Future.successful(Redirect(appConfig.signInUrl))
          case (None, _) =>
            logger.warn("[AuthorisedAction][individualAuthentication] - User has no MTD IT enrolment. Redirecting user to sign up for MTD.")
            Future.successful(Redirect(controllers.errors.routes.IndividualAuthErrorController.show))
        }
      case _ =>
        logger.warn("[AuthorisedAction][individualAuthentication] User has confidence level below 250, routing user to IV uplift.")
        Future(Redirect(routes.IVUpliftController.initialiseJourney))
    }
  }

  private[predicates] def agentAuthentication[A](block: User[A] => Future[Result],
                                                 sessionId: String)
                                                (implicit request: Request[A], hc: HeaderCarrier): Future[Result] =
    sessionDataService.getSessionData(sessionId).flatMap { sessionData =>
      authService
        .authorised(EnrolmentHelper.agentAuthPredicate(sessionData.mtditid))
        .retrieve(allEnrolments)(
          enrolments => handleForValidAgent(block, sessionData.mtditid, sessionData.nino, enrolments, isSupportingAgent = false, sessionId)
        )
        .recoverWith(agentRecovery(block, sessionData.mtditid, sessionData.nino, sessionId))
    }.recover {
      case _: MissingAgentClientDetails =>
        Redirect(appConfig.viewAndChangeEnterUtrUrl)
    }

  private def agentRecovery[A](block: User[A] => Future[Result],
                               mtdItId: String,
                               nino: String,
                               sessionId: String)
                              (implicit request: Request[A], hc: HeaderCarrier): PartialFunction[Throwable, Future[Result]] = {
    case _: NoActiveSession =>
      signInRedirectFutureResult
    case _: AuthorisationException =>
      authService.authorised(EnrolmentHelper.secondaryAgentPredicate(mtdItId))
        .retrieve(allEnrolments)(
          enrolments => handleForValidAgent(block, mtdItId, nino, enrolments, isSupportingAgent = true, sessionId = sessionId)
        )
        .recover {
          case _: AuthorisationException =>
            logger.warn(s"[AuthorisedAction][agentAuthentication] - Agent does not have delegated authority for Client.")
            Redirect(controllers.errors.routes.AgentAuthErrorController.show)
          case e =>
            logger.error(s"[AuthorisedAction][agentAuthentication] - Unexpected exception of type '${e.getClass.getSimpleName}' was caught.")
            errorHandler.internalServerError()
        }
    case e =>
      logger.error(s"[AuthorisedAction][agentAuthentication] - Unexpected exception of type '${e.getClass.getSimpleName}' was caught.")
      errorHandler.futureInternalServerError()
  }

  private def handleForValidAgent[A](block: User[A] => Future[Result],
                                     mtdItId: String,
                                     nino: String,
                                     enrolments: Enrolments,
                                     isSupportingAgent: Boolean,
                                     sessionId: String)
                                    (implicit request: Request[A]): Future[Result] =
    if (isSupportingAgent) {
      logger.warn(s"[AuthorisedAction][agentAuthentication] - Secondary agent unauthorised")
      Future.successful(Redirect(controllers.errors.routes.SupportingAgentAuthErrorController.show))
    } else {
      EnrolmentHelper.getEnrolmentValueOpt(EnrolmentKeys.Agent, EnrolmentIdentifiers.agentReference, enrolments) match {
        case Some(arn) =>
          block(User(mtdItId, Some(arn), nino, AffinityGroup.Agent.toString, sessionId, isSupportingAgent))
        case None =>
          logger.info(s"[AuthorisedAction][agentAuthentication] - Agent with no HMRC-AS-AGENT enrolment. Rendering unauthorised view.")
          Future.successful(Redirect(controllers.errors.routes.YouNeedAgentServicesController.show))
      }
    }

  private[predicates] def enrolmentGetIdentifierValue(
                                                       checkedKey: String,
                                                       checkedIdentifier: String,
                                                       enrolments: Enrolments
                                                     ): Option[String] = {
    enrolments.enrolments.collectFirst {
      case Enrolment(`checkedKey`, enrolmentIdentifiers, _, _) => enrolmentIdentifiers.collectFirst {
        case EnrolmentIdentifier(`checkedIdentifier`, identifierValue) => identifierValue
      }
    }.flatten
  }
}
