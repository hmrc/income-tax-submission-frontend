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

package controllers.testOnly

import config.AppConfig
import connectors.testOnly.AuthLoginApiConnector
import forms.testOnly.UserResearchLoginForm
import models.userResearch.{AuthLoginAPIResponse, ResearchUser, ResearchUsers}
import java.time.Instant
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Session}
import uk.gov.hmrc.http.{SessionId, SessionKeys}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.UserResearchLoginView

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.ExecutionContext

class UserResearchLoginController @Inject()(mcc: MessagesControllerComponents,
                                            view: UserResearchLoginView,
                                            loginStubConnector: AuthLoginApiConnector)
                                           (implicit appConfig: AppConfig, ec: ExecutionContext) extends FrontendController(mcc) {

  def show(): Action[AnyContent] = Action { implicit request =>
    Ok(view())
  }

  def submit(): Action[AnyContent] = Action.async { implicit request =>
    val suppliedCredentialAndYear: Array[String] = UserResearchLoginForm.researchLoginForm.bindFromRequest().get.split("::")
    val suppliedCredential = suppliedCredentialAndYear.head
    val suppliedYear =
      if(suppliedCredentialAndYear.length > 1 && suppliedCredentialAndYear.last.matches("[0-9]{4}")) {
        suppliedCredentialAndYear.last.toInt
      } else {
        appConfig.defaultTaxYear
      }

    val userDetails: ResearchUser = ResearchUsers.generateUserCredentials(suppliedCredential, suppliedYear)

    loginStubConnector.submitLoginRequest(userDetails).map {
      case Some(response) => Redirect(controllers.routes.StartPageController.show(userDetails.taxYear))
        .withSession(buildGGSession(response))
      case None => Redirect(controllers.testOnly.routes.UserResearchLoginController.show())
    }
  }

  private[testOnly] def buildGGSession(authExchange: AuthLoginAPIResponse): Session = Session(Map(
    SessionKeys.sessionId -> (if (authExchange.sessionId.isEmpty) SessionId(s"session-${UUID.randomUUID}").value else authExchange.sessionId),
    SessionKeys.authToken -> authExchange.token,
    SessionKeys.lastRequestTimestamp -> Instant.now().toEpochMilli.toString
  ))
}
