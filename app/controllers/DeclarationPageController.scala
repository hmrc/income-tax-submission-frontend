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

import audit.{AuditService, DeclarationDetail, FinalDeclarationDetail}
import common.SessionValues._
import config.{AppConfig, ErrorHandler}
import controllers.predicates.TaxYearAction.taxYearAction
import controllers.predicates.AuthorisedAction

import javax.inject.{Inject, Singleton}
import models.{APIErrorBodyModel, APIErrorsBodyModel, DeclarationModel}
import play.api.i18n.I18nSupport
import play.api.Logger
import play.api.mvc._
import services.DeclareCrystallisationService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.DeclarationPageView
import utils.SessionDataHelper

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeclarationPageController @Inject()(declareCrystallisationService: DeclareCrystallisationService,
                                          appConfig: AppConfig,
                                          implicit val mcc: MessagesControllerComponents,
                                          implicit val ec: ExecutionContext,
                                          declarationPageView: DeclarationPageView,
                                          authorisedAction: AuthorisedAction,
                                          errorHandler: ErrorHandler,
                                          auditService: AuditService) extends FrontendController(mcc) with I18nSupport with SessionDataHelper {

  lazy val logger: Logger = Logger.apply(this.getClass)

  implicit val config: AppConfig = appConfig

  def show(taxYear: Int): Action[AnyContent] = (authorisedAction andThen taxYearAction(taxYear)).apply { implicit user =>

    val summaryDataReceived: Option[DeclarationModel] = getSessionData[DeclarationModel](SUMMARY_DATA)

    summaryDataReceived match {
      case Some(summaryData) =>
        Ok(declarationPageView(summaryData, user.isAgent, taxYear))
      case _ =>
        logger.info("[DeclarationPageController][show] No Tax Return Submission Data in session, routing user to Overview page.")
        Redirect(routes.OverviewPageController.show(taxYear))
    }
  }

  def submit(taxYear: Int): Action[AnyContent] = (authorisedAction andThen taxYearAction(taxYear)).async {
    implicit user =>
      val optionalCalculationId: Option[String] = user.session.get(CALCULATION_ID)
      val optionalSummaryDataReceived: Option[DeclarationModel] = getSessionData[DeclarationModel](SUMMARY_DATA)

      (optionalSummaryDataReceived, optionalCalculationId) match {
        case (Some(summaryDataReceived), Some(calculationId)) =>
          declareCrystallisationService.postDeclareCrystallisation(user.nino, taxYear, calculationId, user.mtditid).map {
            case Right(_) =>
              auditService.sendAudit(FinalDeclarationDetail(
                taxYear, if(user.isAgent) "agent" else "individual", user.nino, user.mtditid,
                DeclarationDetail(
                  summaryDataReceived.income,
                  summaryDataReceived.allowancesAndDeductions,
                  summaryDataReceived.totalTaxableIncome,
                  summaryDataReceived.incomeTaxAndNationalInsuranceContributions
                )
              ).toAuditModel)
              Redirect(controllers.routes.TaxReturnReceivedController.show(taxYear))
            case Left(error) =>
              error.body match {
                case apiError: APIErrorBodyModel => errorHandler.handleDeclareCrystallisationError(error.status, apiError.reason, user.isAgent, taxYear)
                case apiErrors: APIErrorsBodyModel =>
                  errorHandler.handleDeclareCrystallisationError(error.status, apiErrors.failures.head.reason, user.isAgent, taxYear)
              }
          }
        case _ =>
          logger.info("[DeclarationPageController][submit] No Calculation ID in session, routing user to Overview page.")
          Future.successful(Redirect(routes.OverviewPageController.show(taxYear)))
      }
  }
}
