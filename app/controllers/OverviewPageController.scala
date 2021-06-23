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

package controllers

import common.SessionValues._
import config.{AppConfig, ErrorHandler}
import controllers.predicates.AuthorisedAction
import controllers.predicates.TaxYearAction.taxYearAction
import javax.inject.{Inject, Singleton}
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.{CalculationIdService, IncomeSourcesService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.OverviewPageView

import scala.concurrent.ExecutionContext

@Singleton
class OverviewPageController @Inject()(
                                        appConfig: AppConfig,
                                        implicit val mcc: MessagesControllerComponents,
                                        implicit val ec: ExecutionContext,
                                        incomeSourcesService: IncomeSourcesService,
                                        calculationIdService: CalculationIdService,
                                        overviewPageView: OverviewPageView,
                                        authorisedAction: AuthorisedAction,
                                        errorHandler: ErrorHandler) extends FrontendController(mcc) with I18nSupport {

  implicit val config: AppConfig = appConfig

  def show(taxYear: Int): Action[AnyContent] = (authorisedAction andThen taxYearAction(taxYear)).async { implicit user =>
    incomeSourcesService.getIncomeSources(user.nino, taxYear, user.mtditid).map {
      case Right(incomeSources) =>
        Ok(overviewPageView(isAgent = user.isAgent, Some(incomeSources), taxYear))
      case Left(error) => errorHandler.handleError(error.status)
    }
  }

  def getCalculation(taxYear: Int): Action[AnyContent] = authorisedAction.async { implicit user =>
    calculationIdService.getCalculationId(user.nino, taxYear, user.mtditid).map {
      case Right(calculationId) =>
        Redirect(appConfig.viewAndChangeCalculationUrl(taxYear)).addingToSession(CALCULATION_ID -> calculationId.id)
      case Left(error) => errorHandler.handleError(error.status)
    }
  }

}
