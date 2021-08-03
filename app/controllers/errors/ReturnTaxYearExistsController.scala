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

package controllers.errors

import config.AppConfig
import controllers.predicates.AuthorisedAction
import javax.inject.{Inject, Singleton}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.errors.ReturnTaxYearExistsView
import controllers.predicates.TaxYearAction.taxYearAction

@Singleton
class ReturnTaxYearExistsController @Inject()(val authorisedAction: AuthorisedAction,
                                               val mcc: MessagesControllerComponents,
                                              val returnTaxYearExistsView: ReturnTaxYearExistsView,
                                              implicit val appConfig: AppConfig) extends FrontendController(mcc) with I18nSupport {

  def show(taxYear: Int): Action[AnyContent] = (authorisedAction andThen taxYearAction(taxYear, missingTaxYearReset = false)) {
    implicit user =>
      Ok(returnTaxYearExistsView(isAgent = user.isAgent, taxYear))
  }
}


