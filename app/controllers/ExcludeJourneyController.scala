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

import common.IncomeSources._
import config.{AppConfig, ErrorHandler}
import controllers.predicates.AuthorisedAction
import controllers.predicates.TaxYearAction.taxYearAction
import models.mongo.ExclusionUserDataModel
import models.{ExcludeJourneyModel, IncomeSourcesModel}
import play.api.Logging
import play.api.data.Form
import play.api.data.Forms.text
import play.api.i18n.I18nSupport
import play.api.mvc._
import repositories.ExclusionUserDataRepository
import services.{IncomeSourcesService, ValidTaxYearListService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.ShaHashHelper

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ExcludeJourneyController @Inject()(authorisedAction: AuthorisedAction,
                                         incomeSourcesService: IncomeSourcesService,
                                        implicit val validTaxYearListService: ValidTaxYearListService,
                                        implicit val errorHandler: ErrorHandler,
                                        exclusionUserDataRepository: ExclusionUserDataRepository
                                       )
                                       (implicit appConfig: AppConfig, mcc: MessagesControllerComponents, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport with Logging with ShaHashHelper {

  val allJourneys: Seq[String] = Seq(INTEREST, DIVIDENDS, GIFT_AID, EMPLOYMENT, CIS)

  def excludeJourney(taxYear: Int): Action[AnyContent] = (authorisedAction andThen taxYearAction(taxYear)).async {
    implicit user =>
      if(appConfig.tailoringEnabled) {
        Form("Journey" -> text).bindFromRequest().fold({
          _ =>
            logger.info("[ExcludeJourneyController][excludeJourney] Submitted form is invalid")
            Future.successful(Redirect(controllers.routes.OverviewPageController.show(taxYear)))
        }, {
          result =>
            if (allJourneys.contains(result)){
              incomeSourcesService.getIncomeSources(user.nino, taxYear, user.mtditid).map {
                case Right(incomeSources) =>
                  exclusionUserDataRepository.find(taxYear).flatMap{
                    case Right(exclusionData) =>
                      exclusionData.map(_.exclusionModel).fold{
                        exclusionUserDataRepository.create(ExclusionUserDataModel(user.nino, taxYear, Seq(toExcludeJourneyModel(result, incomeSources))))
                      }
                      {currentExclusionData =>
                          if (!currentExclusionData.map(journeys => journeys.journey).contains(result)) {
                            exclusionUserDataRepository.update(ExclusionUserDataModel(user.nino, taxYear,
                            currentExclusionData :+ toExcludeJourneyModel(result, incomeSources)))
                          } else{
                            Future.successful(Right(false))
                          }
                      }
                      Future.successful(Redirect(controllers.routes.OverviewPageController.show(taxYear)))
                    case Left(error) => Future.successful(InternalServerError(errorHandler.internalServerErrorTemplate))
                  }
                  Redirect(controllers.routes.OverviewPageController.show(taxYear))
                case Left(error) => errorHandler.handleError(error.status)
              }
            }
            else{
              logger.error("[ExcludeJourneyController][excludeJourney] Journey does not exist")
              Future.successful(InternalServerError(errorHandler.internalServerErrorTemplate))
            }

        })
      }
      else {
        Future.successful(Redirect(controllers.routes.OverviewPageController.show(taxYear)))
      }
  }

  def toExcludeJourneyModel(journey: String, incomeSources: IncomeSourcesModel): ExcludeJourneyModel ={
    journey match {
      case GIFT_AID =>
        ExcludeJourneyModel(journey, incomeSources.giftAid.map(data => sha256Hash(data.toString)))
      case INTEREST =>
        ExcludeJourneyModel(journey, incomeSources.interest.map(data => sha256Hash(data.toString)))
      case _ => ExcludeJourneyModel(journey, None)
    }
  }
}
