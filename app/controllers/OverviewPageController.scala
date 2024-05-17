/*
 * Copyright 2024 HM Revenue & Customs
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
import common.IncomeSources._
import common.SessionValues._
import config.{AppConfig, ErrorHandler}
import controllers.predicates.TaxYearAction.taxYearAction
import controllers.predicates.{AuthorisedAction, InYearAction}
import models.{ClearExcludedJourneysRequestModel, IncomeSourcesModel, OverviewTailoringModel, User}
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc._
import repositories.TailoringUserDataRepository
import services.{ExcludedJourneysService, IncomeSourcesService, LiabilityCalculationService, ValidTaxYearListService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.ShaHashHelper
import views.html.OverviewPageView

import java.util.UUID
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
                                       auditService: AuditService,
                                       excludedJourneysService: ExcludedJourneysService)
                                      (implicit appConfig: AppConfig, mcc: MessagesControllerComponents, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport with ShaHashHelper with Logging{


  private lazy val OverviewPageControllerRoute: ReverseOverviewPageController = controllers.routes.OverviewPageController

  //TODO revisit this to check duplication of correlationId creation
  private def getCorrelationId(implicit hc:HeaderCarrier): Serializable = hc.extraHeaders.find(_._1 == "CorrelationId").getOrElse(UUID.randomUUID())

  private def handleGetIncomeSources(taxYear: Int, isInYear: Boolean)(implicit user: User[_]): Future[Result] = {
    tailoringUserDataRepository.find(taxYear).flatMap {
      case Right(sessionData) =>
        incomeSourcesService.getIncomeSources(user.nino, taxYear, user.mtditid).flatMap {
          case Right(incomeSources) =>
            handleExclusion(taxYear, incomeSources).map { excludedJourneys =>
              val tailoringSources = sessionData.map(_.tailoring).getOrElse(Seq.empty[String])
              val overviewTailoringData = OverviewTailoringModel(tailoringSources, incomeSources)
              logger.info(s"$getCorrelationId::[OverviewPageController][handleGetIncomeSources] returning overview page")
              Ok(overviewPageView(isAgent = user.isAgent, Some(incomeSources), overviewTailoringData, taxYear, isInYear, excludedJourneys))
            }
          case Left(error) =>
            logger.error(s"$getCorrelationId::[OverviewPageController][handleGetIncomeSources] incomeSourcesService.getIncomeSources returned error $error")
            Future.successful(errorHandler.handleError(error.status))
        }
      case Left(e) =>
        logger.error(s"$getCorrelationId::[OverviewPageController][handleGetIncomeSources] tailoringUserDataRepository.find returned error $e")
        Future.successful(InternalServerError(errorHandler.internalServerErrorTemplate))
    }
  }

  private def handleExclusion(taxYear: Int, incomeSourcesModel: IncomeSourcesModel)(implicit user: User[_]): Future[Seq[String]] = {
    val dividendsRemove = incomeSourcesModel.dividends.exists(dividends => dividends.hasNonZeroData)
    val giftAidRemove = incomeSourcesModel.giftAid.exists(giftAid => giftAid.hasNonZeroData)
    val interestRemove = incomeSourcesModel.interest.exists(interests => interests.exists(_.hasNonZeroData))
    val employmentRemove = incomeSourcesModel.employment.nonEmpty
    val cisRemove = incomeSourcesModel.cis.exists(_.contractorCISDeductions.exists(_.hasNonZeroData)) ||
      incomeSourcesModel.cis.exists(_.customerCISDeductions.exists(_.hasNonZeroData))
    val pensionsRemove = incomeSourcesModel.pensions.nonEmpty
    val stateBenefitsRemove = incomeSourcesModel.stateBenefits.nonEmpty
    val selfEmploymentRemove = incomeSourcesModel.selfEmployment.nonEmpty
    val interestSavingsRemove = incomeSourcesModel.interestSavings.nonEmpty
    val gainsRemove = incomeSourcesModel.gains.nonEmpty
    val stockDividendsRemove = incomeSourcesModel.stockDividends.exists(stockDividends => stockDividends.hasNonZeroData)

    excludedJourneysService.getExcludedJourneys(taxYear, user.nino, user.mtditid).map {
      case Right(data) =>
        val giftAidHash = incomeSourcesModel.giftAid.exists { model =>
          val nonUkCharitiesCharityNames = model.giftAidPayments.flatMap(_.nonUkCharitiesCharityNames).getOrElse(Seq.empty)
          val investmentsNonUkCharitiesCharityNames = model.gifts.flatMap(_.investmentsNonUkCharitiesCharityNames).getOrElse(Seq.empty)
          val giftAidAccounts = (nonUkCharitiesCharityNames ++ investmentsNonUkCharitiesCharityNames).sorted

          data.journeys.find(_.journey == GIFT_AID).exists(_.hash.exists(_ != sha256Hash(giftAidAccounts.mkString(","))))
        }

        val interestHash = incomeSourcesModel.interest.exists { model =>
          val interestAccounts = model.map(_.accountName).sorted

          data.journeys.find(_.journey == INTEREST).exists(_.hash.exists(_ != sha256Hash(interestAccounts.mkString(","))))
        }

        val newExclude: Seq[String] = Seq(
          (dividendsRemove || stockDividendsRemove, DIVIDENDS),
          (cisRemove, CIS),
          (employmentRemove, EMPLOYMENT),
          (pensionsRemove, PENSIONS),
          (stateBenefitsRemove, STATE_BENEFITS),
          (selfEmploymentRemove, SELF_EMPLOYMENT),
          (giftAidRemove || giftAidHash, GIFT_AID),
          (interestRemove || interestHash, INTEREST),
          (interestSavingsRemove, INTEREST_SAVINGS),
          (gainsRemove, GAINS)
        ).filter(_._1).map(_._2)

        val newData = data.journeys.filter(excludedModels => newExclude.contains(excludedModels.journey)).map(_.journey)
        excludedJourneysService.clearExcludedJourneys(taxYear, user.nino, user.mtditid, ClearExcludedJourneysRequestModel(newData))

        data.journeys.map(_.journey).filterNot(newExclude.contains)
    }
  }

  def show(taxYear: Int): Action[AnyContent] = (authorisedAction andThen taxYearAction(taxYear)).async { implicit user =>
    val isInYear: Boolean = inYearAction.inYear(taxYear)
    if (isInYear) {
      handleGetIncomeSources(taxYear, isInYear = true)
    } else {
      logger.info(s"$getCorrelationId::[OverviewPageController][show] not in year")
      Future.successful(Redirect(OverviewPageControllerRoute.showCrystallisation(taxYear)))
    }
  }

  def showCrystallisation(taxYear: Int): Action[AnyContent] = (authorisedAction andThen taxYearAction(taxYear)).async { implicit user =>
    inYearAction.notInYear(taxYear)(handleGetIncomeSources(taxYear, isInYear = false))
  }

  def finalCalculation(taxYear: Int): Action[AnyContent] = authorisedAction.async { implicit user =>
    inYearAction.notInYear(taxYear) {
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
  }

  def inYearEstimate(taxYear: Int): Action[AnyContent] = authorisedAction.async { implicit user =>

    val isInYear: Boolean = inYearAction.inYear(taxYear)

    if (isInYear) {
      val userTypeString = if (user.isAgent) "agent" else "individual"
      auditService.sendAudit(CreateInYearTaxEstimate(taxYear, userTypeString, user.nino, user.mtditid).toAuditModel)

      Future.successful(
        if (user.isAgent) {
          Redirect(appConfig.viewAndChangeViewInYearEstimateUrlAgent)
        } else {
          Redirect(appConfig.viewAndChangeViewInYearEstimateUrl)
        }
      )
    } else {
      Future.successful(Redirect(OverviewPageControllerRoute.showCrystallisation(taxYear)))
    }
  }
}
