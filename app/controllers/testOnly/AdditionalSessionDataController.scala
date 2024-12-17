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
import connectors.IncomeTaxSessionDataConnector
import models.sessionData.SessionData

import javax.inject.Inject
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.OverviewPageView

import scala.concurrent.{ExecutionContext, Future}

class AdditionalSessionDataController @Inject()(
                                                 appConfig: AppConfig,
                                                 mcc: MessagesControllerComponents,
                                                 sessionDataConnector: IncomeTaxSessionDataConnector,
                                                 overviewPageView: OverviewPageView)(implicit ec: ExecutionContext) extends FrontendController(mcc) with I18nSupport {

  implicit val config: AppConfig = appConfig

  def show(taxYear: Int): Action[AnyContent] = Action.async { implicit request =>

    val queryParams = request.queryString

    lazy val result: Result = queryParams
      .map(mapValue => mapValue._1 -> mapValue._2.head)
      .foldRight(Redirect(controllers.routes.StartPageController.show(taxYear))) { (keyValuePair, passedResult) =>
        val (key: String, value: String) = keyValuePair
        passedResult.addingToSession(key -> value)
      }

    val sessionId = request
      .session.get("sessionId")
      .orElse(hc.sessionId.map(_.value))
      .getOrElse(throw new IllegalStateException("Session ID not found"))
    val sessionData = SessionData.mkSessionData(sessionId, queryParams)

    println(sessionId)
    println(sessionData)

    sessionDataConnector.upsert(sessionId, sessionData).map(_ => result)
  }
}
