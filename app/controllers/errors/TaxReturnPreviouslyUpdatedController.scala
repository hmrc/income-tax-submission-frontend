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

package controllers.errors

import config.AppConfig
import controllers.predicates.{AuthorisedAction, InYearAction}

import javax.inject.{Inject, Singleton}
import play.api.i18n.I18nSupport
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.errors.TaxReturnPreviouslyUpdatedView

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class TaxReturnPreviouslyUpdatedController @Inject()(val authorisedAction: AuthorisedAction,
                                                     val taxReturnPreviouslyUpdatedView: TaxReturnPreviouslyUpdatedView,
                                                     implicit val appConfig: AppConfig,
                                                     implicit val mcc: MessagesControllerComponents,
                                                     implicit val ec: ExecutionContext,
                                                     implicit val inYearAction: InYearAction
                                               ) extends FrontendController(mcc) with I18nSupport {

  def show(taxYear: Int): Action[AnyContent] = authorisedAction.async { implicit user =>
    inYearAction.notInYear(taxYear) {
      Future.successful(Conflict(taxReturnPreviouslyUpdatedView(isAgent = user.isAgent, taxYear)))
    }
  }

}
