/*
 * Copyright 2022 HM Revenue & Customs
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
import controllers.predicates.AuthorisedAction
import controllers.predicates.TaxYearAction.taxYearAction
import javax.inject.{Inject, Singleton}
import models.TaxReturnReceivedModel
import play.api.i18n.I18nSupport
import play.api.Logger
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.TaxReturnReceivedView
import uk.gov.hmrc.play.language.LanguageUtils
import utils.{ImplicitDateFormatter, SessionDataHelper}

import java.time.LocalDate
import scala.concurrent.ExecutionContext

@Singleton
class TaxReturnReceivedController @Inject()(val authorisedAction: AuthorisedAction,
                                            val taxReturnReceivedView: TaxReturnReceivedView,
                                            val languageUtils: LanguageUtils,
                                            implicit val appConfig: AppConfig,
                                            implicit val mcc: MessagesControllerComponents,
                                            implicit val ec: ExecutionContext) extends FrontendController(mcc)
  with I18nSupport with SessionDataHelper with ImplicitDateFormatter {

  lazy val logger: Logger = Logger.apply(this.getClass)

  def show(taxYear: Int): Action[AnyContent] = (authorisedAction andThen taxYearAction(taxYear)).apply { implicit user =>

    val summaryDataReceived: Option[TaxReturnReceivedModel] = getSessionData[TaxReturnReceivedModel](SessionValues.SUMMARY_DATA)

    summaryDataReceived match {
      case Some(summaryData) =>
        Ok(taxReturnReceivedView(summaryData, user.isAgent, taxYear, LocalDate.now.toLongDate))
      case _ =>
        logger.info("[TaxReturnReceivedController][show] No Tax Return Submission Data in session, routing user to Overview page.")
        Redirect(routes.OverviewPageController.show(taxYear))
    }
  }
}
