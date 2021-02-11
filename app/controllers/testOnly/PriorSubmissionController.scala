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

package controllers.testOnly

import config.AppConfig
import javax.inject.{Inject, Singleton}
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.OverviewPageView

import scala.concurrent.Future

@Singleton
class PriorSubmissionController @Inject()(
                                           appConfig: AppConfig,
                                           mcc: MessagesControllerComponents,
                                           overviewPageView: OverviewPageView) extends FrontendController(mcc) with I18nSupport {

  implicit val config: AppConfig = appConfig

  def show(ukDividends: Option[String], otherUkDividends: Option[String], taxYear: Int): Action[AnyContent] = Action.async { implicit request =>

    val json = Json.obj(
      "ukDividends" -> ukDividends,
      "otherUkDividends" -> otherUkDividends
    )

    Future.successful(Redirect(controllers.routes.StartPageController.show(taxYear)).addingToSession("DIVIDENDS_PRIOR_SUB" -> json.toString()))
  }

}

