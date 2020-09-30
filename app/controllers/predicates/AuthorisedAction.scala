/*
 * Copyright 2020 HM Revenue & Customs
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
import config.AppConfig
import javax.inject.Inject
import models.User
import play.api.Logger
import play.api.mvc.Results._
import play.api.mvc._
import services.AuthService
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{affinityGroup, allEnrolments}
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

class AuthorisedAction @Inject()(
                                  appConfig: AppConfig
                                )(
                                  implicit val authService: AuthService,
                                  val mcc: MessagesControllerComponents
                                ) extends ActionBuilder[User, AnyContent] {

  implicit val executionContext: ExecutionContext = mcc.executionContext
  lazy val logger: Logger = Logger.apply(this.getClass)

  override def parser: BodyParser[AnyContent] = mcc.parsers.default

  override def invokeBlock[A](request: Request[A], block: User[A] => Future[Result]): Future[Result] = {

    implicit lazy val headerCarrier: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))

    authService.authorised.retrieve(allEnrolments and affinityGroup) {
      case enrolments ~ Some(AffinityGroup.Agent) =>
        checkAuthorisation(block, enrolments, isAgent = true)(request, headerCarrier)
      case enrolments ~ _ =>
        checkAuthorisation(block, enrolments)(request, headerCarrier)
    } recover {
      case _: NoActiveSession =>
        logger.debug(s"AgentPredicate][authoriseAsAgent] - No active session. Redirecting to {appConfig.signInUrl}")
        Redirect(appConfig.signInUrl) //TODO Check this is the correct location
      case _: AuthorisationException =>
        logger.debug(s"[AgentPredicate][authoriseAsAgent] - Agent does not have delegated authority for Client. " +
          s"Redirecting to {appConfig.agentClientUnauthorisedUrl(request.uri)}")
        Unauthorized("") //TODO Redirect to unauthorised page
    }
  }

  def checkAuthorisation[A](block: User[A] => Future[Result], enrolments: Enrolments, isAgent: Boolean = false)
                           (implicit request: Request[A], hc: HeaderCarrier): Future[Result] = {

    val neededKey = if (isAgent) EnrolmentKeys.Agent else EnrolmentKeys.Individual
    val neededIdentifier = if (isAgent) EnrolmentIdentifiers.agentReference else EnrolmentIdentifiers.individualId

    enrolmentGetIdentifierValue(neededKey, neededIdentifier, enrolments).fold(
      Future.successful(Unauthorized("No MTDITID"))
    ) { mtditid =>
      if (isAgent) agentAuthentication(block, mtditid) else individualAuthentication(block, enrolments, mtditid)
    }

  }

  private[predicates] def agentAuthentication[A](block: User[A] => Future[Result], mtditid: String)
                                                (implicit request: Request[A], hc: HeaderCarrier): Future[Result] = {
    val agentDelegatedAuthRuleKey = "mtd-vat-auth"

    val agentAuthPredicate: String => Enrolment = identifierId =>
      Enrolment(EnrolmentKeys.Individual)
        .withIdentifier(EnrolmentIdentifiers.individualId, identifierId) //TODO clarify identifier values
        .withDelegatedAuthRule(agentDelegatedAuthRuleKey)

    authService.authorised(agentAuthPredicate(mtditid))
      .retrieve(allEnrolments) { enrolments =>
        enrolments.enrolments.collectFirst {
          case Enrolment(EnrolmentKeys.Agent, EnrolmentIdentifier(_, arn) :: _, "Activated", _) => arn
        } match {
          case Some(arn) => block(User(mtditid, Some(arn)))
          case None =>
            logger.debug("[AuthorisedAction][CheckAuthorisation] Agent with no HMRC-AS-AGENT enrolment. Rendering unauthorised view.")
            Future.successful(Forbidden("")) //TODO add agent unauthorised page
        }
      } recover {
      case _: NoActiveSession =>
        logger.debug(s"AgentPredicate][authoriseAsAgent] - No active session. Redirecting to {appConfig.signInUrl}")
        Redirect(appConfig.signInUrl) //TODO Check this is the correct location
      case _: AuthorisationException =>
        logger.debug(s"[AgentPredicate][authoriseAsAgent] - Agent does not have delegated authority for Client. " +
          s"Redirecting to {appConfig.agentClientUnauthorisedUrl(request.uri)}")
        Unauthorized("") //TODO Redirect to unauthorised page
    }
  }

  private[predicates] def individualAuthentication[A](block: User[A] => Future[Result], enrolments: Enrolments, mtditid: String)
                                                     (implicit request: Request[A], hc: HeaderCarrier): Future[Result] = {
    enrolments.enrolments.collectFirst {
      case Enrolment(EnrolmentKeys.Individual, EnrolmentIdentifier(EnrolmentIdentifiers.individualId, _) :: _, _, _) =>
        block(User(mtditid, None))
    } getOrElse {
      logger.warn("[AuthorisedAction][IndividualAuthentication] Non-agent with an invalid UTR.")
      Future.successful(Forbidden("")) //TODO send to an unauthorised page
    }
  }

  private[predicates] def enrolmentGetIdentifierValue(
                                                       checkedKey: String,
                                                       checkedIdentifier: String,
                                                       enrolments: Enrolments
                                                     ): Option[String] = enrolments.enrolments.collectFirst {
    case Enrolment(`checkedKey`, EnrolmentIdentifier(`checkedIdentifier`, identifierValue) :: _, _, _) => identifierValue
  }

}
