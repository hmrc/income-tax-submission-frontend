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

import audit.{AuditService, CreateInYearTaxEstimate, IntentToCrystalliseDetail}
import common.SessionValues._
import config.{AppConfig, ErrorHandler}
import controllers.predicates.TaxYearAction.taxYearAction
import controllers.predicates.{AuthorisedAction, InYearAction}
import models.mongo.TailoringUserDataModel
import models.{OverviewTailoringModel, User}
import play.api.i18n.I18nSupport
import play.api.mvc._
import repositories.TailoringUserDataRepository
import services.{IncomeSourcesService, LiabilityCalculationService, ValidTaxYearListService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.OverviewPageView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class OverviewPageController @Inject()(inYearAction: InYearAction,
                                       incomeSourcesService: IncomeSourcesService,
                                       liabilityCalculationService: LiabilityCalculationService,
                                       tailoringUserDataRepository: TailoringUserDataRepository,
                                       overviewPageView: OverviewPageView,
                                       authorisedAction: AuthorisedAction,
                                       implicit val validTaxYearListService: ValidTaxYearListService,
                                       implicit val errorHandler: ErrorHandler,
                                       auditService: AuditService)
                                      (implicit appConfig: AppConfig, mcc: MessagesControllerComponents, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {

  private lazy val OverviewPageControllerRoute: ReverseOverviewPageController = controllers.routes.OverviewPageController

  def handleGetIncomeSources(taxYear: Int, isInYear: Boolean)(implicit user: User[_]): Future[Result] = {
    tailoringUserDataRepository.find(taxYear).flatMap {
      case Right(sessionData) =>
        incomeSourcesService.getIncomeSources(user.nino, taxYear, user.mtditid).map {
          case Right(incomeSources) =>
            val tailoringSources = sessionData.map(_.tailoring).getOrElse(Seq.empty[String])
            val overviewTailoringData = OverviewTailoringModel(tailoringSources, incomeSources)
            Ok(overviewPageView(isAgent = user.isAgent, Some(incomeSources), overviewTailoringData, taxYear, isInYear))
          case Left(error) => errorHandler.handleError(error.status)
        }
      case Left(_) =>
        Future.successful(InternalServerError(errorHandler.internalServerErrorTemplate))
    }
  }

  def show(taxYear: Int): Action[AnyContent] = (authorisedAction andThen taxYearAction(taxYear)).async { implicit user =>
    val isInYear: Boolean = inYearAction.inYear(taxYear)

    if (isInYear) {
      handleGetIncomeSources(taxYear, isInYear = true)
    } else {
      Future.successful(Redirect(OverviewPageControllerRoute.showCrystallisation(taxYear)))
    }
  }

  def showCrystallisation(taxYear: Int): Action[AnyContent] = (authorisedAction andThen taxYearAction(taxYear)).async { implicit user =>
    handleGetIncomeSources(taxYear, isInYear = false)
  }

  def finalCalculation(taxYear: Int): Action[AnyContent] = authorisedAction.async { implicit user =>

    liabilityCalculationService.getIntentToCrystallise(user.nino, taxYear, user.mtditid).map {
      case Right(calculationId) =>

        val userTypeString = if (user.isAgent) "agent" else "individual"
        auditService.sendAudit(IntentToCrystalliseDetail(taxYear, userTypeString, user.nino, user.mtditid).toAuditModel)

        if (user.isAgent) {
          Redirect(appConfig.viewAndChangeFinalCalculationUrlAgent(taxYear)).addingToSession(CALCULATION_ID -> calculationId.id)
        } else {
          Redirect(appConfig.viewAndChangeFinalCalculationUrl(taxYear)).addingToSession(CALCULATION_ID -> calculationId.id)
        }
      case Left(error) => errorHandler.handleIntentToCrystalliseError(error.status, taxYear)
    }
  }

  def inYearEstimate(taxYear: Int): Action[AnyContent] = authorisedAction.async { implicit user =>

    val userTypeString = if (user.isAgent) "agent" else "individual"
    auditService.sendAudit(CreateInYearTaxEstimate(taxYear, userTypeString, user.nino, user.mtditid).toAuditModel)

    Future.successful(
      if (user.isAgent) {
        Redirect(appConfig.viewAndChangeViewInYearEstimateUrlAgent)
      } else {
        Redirect(appConfig.viewAndChangeViewInYearEstimateUrl)
      }
    )
  }
}
