/*
 * Copyright 2021 HM Revenue & Customs
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

import common.{EnrolmentIdentifiers, EnrolmentKeys, SessionValues}
import config.AppConfig
import controllers.routes
import models.User
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Results._
import play.api.mvc._
import services.AuthService
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{affinityGroup, allEnrolments, confidenceLevel}
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.auth.core.{ConfidenceLevel, _}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter
import views.html.authErrorPages.AgentAuthErrorPageView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AuthorisedAction @Inject()(appConfig: AppConfig,
                                 val agentAuthErrorPage: AgentAuthErrorPageView)
                                (implicit val authService: AuthService,
                                 val mcc: MessagesControllerComponents) extends ActionBuilder[User, AnyContent] with I18nSupport {

  implicit val executionContext: ExecutionContext = mcc.executionContext
  lazy val logger: Logger = Logger.apply(this.getClass)
  implicit val config: AppConfig = appConfig
  implicit val messagesApi: MessagesApi = mcc.messagesApi

  val minimumConfidenceLevel: Int = ConfidenceLevel.L200.level

  override def parser: BodyParser[AnyContent] = mcc.parsers.default

  override def invokeBlock[A](request: Request[A], block: User[A] => Future[Result]): Future[Result] = {

    implicit lazy val headerCarrier: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))

    authService.authorised.retrieve(affinityGroup) {
      case Some(AffinityGroup.Agent) => agentAuthentication(block)(request, headerCarrier)
      case _ => individualAuthentication(block)(request, headerCarrier)
    } recover {
      case _: NoActiveSession =>
        logger.info(s"[AgentPredicate][authoriseAsAgent] - No active session. Redirecting to ${appConfig.signInUrl}")
        Redirect(appConfig.signInUrl)
      case _: AuthorisationException =>
        logger.info(s"[AgentPredicate][authoriseAsAgent] - Agent does not have delegated authority for Client.")
        Redirect(controllers.routes.UnauthorisedUserErrorController.show())
    }
  }

  def individualAuthentication[A](block: User[A] => Future[Result])
                                 (implicit request: Request[A], hc: HeaderCarrier): Future[Result] = {
    authService.authorised.retrieve(allEnrolments and confidenceLevel) {
      case enrolments ~ userConfidence if userConfidence.level >= minimumConfidenceLevel =>
        val optionalMtditid: Option[String] = enrolmentGetIdentifierValue(EnrolmentKeys.Individual, EnrolmentIdentifiers.individualId, enrolments)
        val optionalNino: Option[String] = enrolmentGetIdentifierValue(EnrolmentKeys.nino, EnrolmentIdentifiers.ninoId, enrolments)

        (optionalMtditid, optionalNino) match {
          case (Some(mtditid), Some(nino)) =>
            enrolments.enrolments.collectFirst {
              case Enrolment(EnrolmentKeys.Individual, enrolmentIdentifiers, _, _)
                if enrolmentIdentifiers.exists(identifier => identifier.key == EnrolmentIdentifiers.individualId) =>
                block(User(mtditid, None, nino))
            } getOrElse {
              logger.info("[AuthorisedAction][individualAuthentication] Non-agent with an invalid MTDITID.")
              Future.successful(Redirect(controllers.routes.UnauthorisedUserErrorController.show()))
            }
          case (_, None) => Future.successful(Redirect(appConfig.signInUrl))
          case (None, _) => Future.successful(Redirect(controllers.errors.routes.IndividualAuthErrorController.show()))
        }
      case _ =>
        logger.info("[AuthorisedAction][invokeBlock] User has confidence level below 200, routing user to IV uplift.")
        Future(Redirect(routes.IVUpliftController.initialiseJourney()))
    }
  }

  private[predicates] def agentAuthentication[A](block: User[A] => Future[Result])
                                                (implicit request: Request[A], hc: HeaderCarrier): Future[Result] = {

    lazy val agentDelegatedAuthRuleKey = "mtd-it-auth"

    lazy val agentAuthPredicate: String => Enrolment = identifierId =>
      Enrolment(EnrolmentKeys.Individual)
        .withIdentifier(EnrolmentIdentifiers.individualId, identifierId)
        .withDelegatedAuthRule(agentDelegatedAuthRuleKey)

    val optionalNino = request.session.get(SessionValues.CLIENT_NINO)
    val optionalMtditid = request.session.get(SessionValues.CLIENT_MTDITID)

    (optionalMtditid, optionalNino) match {
      case (Some(mtditid), Some(nino)) =>
        authService
          .authorised(agentAuthPredicate(mtditid))
          .retrieve(allEnrolments) { enrolments =>

            enrolmentGetIdentifierValue(EnrolmentKeys.Agent, EnrolmentIdentifiers.agentReference, enrolments) match {
              case Some(arn) =>
                block(User(mtditid, Some(arn), nino))
              case None =>
                logger.info("[AuthorisedAction][CheckAuthorisation] Agent with no HMRC-AS-AGENT enrolment. Rendering unauthorised view.")
                Future.successful(Redirect(controllers.errors.routes.YouNeedAgentServicesController.show()))
            }
          } recover {
          case _: NoActiveSession =>
            logger.info(s"AgentPredicate][authoriseAsAgent] - No active session. Redirecting to ${appConfig.signInUrl}")
            Redirect(appConfig.signInUrl) //TODO Check this is the correct location
          case ex: AuthorisationException =>
            logger.info(s"[AgentPredicate][authoriseAsAgent] - Agent does not have delegated authority for Client.")
            Unauthorized(agentAuthErrorPage())
        }
      case (None, _) => Future.successful(Redirect(appConfig.viewAndChangeEnterUtrUrl))
      case (_, None) => Future.successful(Redirect(controllers.errors.routes.YouNeedAgentServicesController.show()))
    }
  }

  private[predicates] def enrolmentGetIdentifierValue(
                                                       checkedKey: String,
                                                       checkedIdentifier: String,
                                                       enrolments: Enrolments
                                                     ): Option[String] = enrolments.enrolments.collectFirst {
    case Enrolment(`checkedKey`, enrolmentIdentifiers, _, _) => enrolmentIdentifiers.collectFirst {
      case EnrolmentIdentifier(`checkedIdentifier`, identifierValue) => identifierValue
    }
  }.flatten
}
