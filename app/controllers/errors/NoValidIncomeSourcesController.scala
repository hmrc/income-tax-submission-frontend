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
import controllers.predicates.{AuthorisedAction, InYearAction}
import play.api.i18n.I18nSupport
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.SessionDataHelper
import views.html.errors.NoValidIncomeSourcesView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class NoValidIncomeSourcesController @Inject()(val authorisedAction: AuthorisedAction,
                                               val view: NoValidIncomeSourcesView,
                                               implicit val appConfig: AppConfig,
                                               implicit val mcc: MessagesControllerComponents,
                                               implicit val inYearAction: InYearAction,
                                               implicit val ec: ExecutionContext
                                             ) extends FrontendController(mcc) with I18nSupport with SessionDataHelper {

  def show(taxYear: Int): Action[AnyContent] = authorisedAction.async { implicit user =>
    inYearAction.notInYear(taxYear) {
      Future.successful(UnprocessableEntity(view(isAgent = user.isAgent, taxYear)))
    }
  }
}
