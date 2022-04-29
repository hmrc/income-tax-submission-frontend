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
import controllers.routes.OverviewPageController
import models.{IncomeSourcesModel, User}
import models.mongo.TailoringUserDataModel
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.{IncomeSourcesService, LiabilityCalculationService, TailoringSessionService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.OverviewPageView
import common.IncomeSources._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class OverviewPageController @Inject()(inYearAction: InYearAction,
                                       incomeSourcesService: IncomeSourcesService,
                                       liabilityCalculationService: LiabilityCalculationService,
                                       tailoringService: TailoringSessionService,
                                       overviewPageView: OverviewPageView,
                                       authorisedAction: AuthorisedAction,
                                       errorHandler: ErrorHandler,
                                       auditService: AuditService)
                                      (implicit appConfig: AppConfig, mcc: MessagesControllerComponents, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {

  //scalastyle:off
  def updateTailoringWithIncomeSources(tailoring: Seq[String], incomeSources: IncomeSourcesModel): Seq[String] = {
    var newTailoring: Seq[String] = tailoring
    if (incomeSources.interest.exists(accounts => accounts.exists(_.hasAmounts)) && !newTailoring.contains(INTEREST))  newTailoring = newTailoring :+ INTEREST
    if (incomeSources.dividends.isDefined && !newTailoring.contains(DIVIDENDS)) newTailoring = newTailoring :+ DIVIDENDS
    if (incomeSources.giftAid.isDefined && !newTailoring.contains(GIFT_AID)) newTailoring = newTailoring :+ GIFT_AID
    if (incomeSources.employment.isDefined && !newTailoring.contains(EMPLOYMENT)) newTailoring = newTailoring :+ EMPLOYMENT
    if (incomeSources.cis.isDefined && !newTailoring.contains(CIS)) newTailoring = newTailoring :+ CIS
    newTailoring
  }
  //scalastyle:on

  def handleTailoring(taxYear: Int, isInYear: Boolean)(implicit user: User[_]): Future[Result] = {
    tailoringService.getSessionData(taxYear).flatMap {
      case Right(tailoringData) =>
        val currentTailoring = tailoringData.getOrElse(TailoringUserDataModel(user.nino, taxYear, Seq[String]())).tailoring
        incomeSourcesService.getIncomeSources(user.nino, taxYear, user.mtditid).flatMap {
          case Right(incomeSources) =>
            val newTailoring = updateTailoringWithIncomeSources(currentTailoring, incomeSources)
            val tailoringUpdateCount: Int = (currentTailoring.size - newTailoring.size) * -1
            if (tailoringData.isDefined) {
              tailoringService.updateSessionData(newTailoring, taxYear)(InternalServerError(errorHandler.internalServerErrorTemplate))(
                Ok(overviewPageView(isAgent = user.isAgent, Some(incomeSources), taxYear, isInYear, newTailoring, tailoringUpdateCount)))
            } else {
              tailoringService.createSessionData(newTailoring, taxYear)(InternalServerError(errorHandler.internalServerErrorTemplate))(
                Ok(overviewPageView(isAgent = user.isAgent, Some(incomeSources), taxYear, isInYear, newTailoring, tailoringUpdateCount)))
            }
          case Left(error) => Future.successful(errorHandler.handleError(error.status))
        }
      case Left(error) => Future.successful(errorHandler.handleError(INTERNAL_SERVER_ERROR))
    }
  }

  def show(taxYear: Int): Action[AnyContent] = (authorisedAction andThen taxYearAction(taxYear)).async { implicit user =>
    val isInYear: Boolean = inYearAction.inYear(taxYear)
    if (isInYear) {
      handleTailoring(taxYear, isInYear)
    } else {
      Future.successful(Redirect(OverviewPageController.showCrystallisation(taxYear)))
    }
  }

  def showCrystallisation(taxYear: Int): Action[AnyContent] =
    (authorisedAction andThen taxYearAction(taxYear)).async { implicit user =>
      handleTailoring(taxYear, false)
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
