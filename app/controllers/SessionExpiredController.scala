/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers

import common.SessionValues
import config.AppConfig
import javax.inject.Inject
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.SessionDataHelper
import views.html.errors.TimeoutPage


class SessionExpiredController @Inject()(val mcc: MessagesControllerComponents,
                                         implicit val appConfig: AppConfig,
                                         timeoutPage: TimeoutPage) extends FrontendController(mcc) with I18nSupport with SessionDataHelper{

  def keepAlive(): Action[AnyContent] = Action(NoContent)

  def timeout: Action[AnyContent] = Action { implicit request =>

    //TODO Use session key for tax year in all places, SASS-253
    getSessionData[Int](SessionValues.TAX_YEAR) match {
      case Some(taxYear) => Ok(timeoutPage(routes.StartPageController.show(taxYear))).withNewSession
      case None => Ok(timeoutPage(routes.StartPageController.show(appConfig.defaultTaxYear))).withNewSession
    }
  }
}
