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

package controllers

import audit.{AuditService, SourcesDetail, TailorAddIncomeSourcesDetail}
import com.google.inject.Inject
import config.{AppConfig, ErrorHandler}
import controllers.predicates.AuthorisedAction
import controllers.predicates.TaxYearAction.taxYearAction
import forms.AddSectionsForm
import forms.AddSectionsForm.addSectionsForm
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.{IncomeSourcesService, NrsService, TailoringSessionService, ValidTaxYearListService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.AddSectionsToIncomeTaxReturnView
import common.IncomeSources._

import scala.concurrent.{ExecutionContext, Future}

class AddSectionsToIncomeTaxReturnController @Inject()(
                                                        authorisedAction: AuthorisedAction,
                                                        view: AddSectionsToIncomeTaxReturnView,
                                                        incomeSourcesService: IncomeSourcesService,
                                                        tailoringSessionService: TailoringSessionService,
                                                        implicit val validTaxYearListService: ValidTaxYearListService,
                                                        implicit val errorHandler: ErrorHandler,
                                                        auditService: AuditService,
                                                        nrsService: NrsService,
                                                        implicit val appConfig: AppConfig,
                                                        implicit val ec: ExecutionContext,
                                                        implicit val mcc: MessagesControllerComponents
                                                      ) extends FrontendController(mcc) with I18nSupport {

  val allJourneys: Seq[String] = Seq(INTEREST, DIVIDENDS, GIFT_AID, EMPLOYMENT, GAINS, CIS, PENSIONS, PROPERTY, STATE_BENEFITS, STOCK_DIVIDENDS)

  def show(taxYear: Int): Action[AnyContent] = (authorisedAction andThen taxYearAction(taxYear)).async {
    implicit user =>
      if(appConfig.tailoringEnabled) {
        incomeSourcesService.getIncomeSources(user.nino, taxYear, user.mtditid).flatMap {
          case Left(_) => Future.successful(InternalServerError(errorHandler.internalServerErrorTemplate))
          case Right(incomeSources) =>
            tailoringSessionService.getSessionData(taxYear).flatMap {
              case Left(_) => Future.successful(InternalServerError(errorHandler.internalServerErrorTemplate))
              case Right(value) => value.fold(
                Future.successful(Ok(view(taxYear, user.isAgent, AddSectionsForm.addSectionsForm(incomeSources), allJourneys, incomeSources)))
              )(
                tailoringData =>
                  Future.successful(Ok(view(taxYear, user.isAgent, AddSectionsForm.addSectionsForm(incomeSources),
                    allJourneys.filter(!tailoringData.tailoring.contains(_)), incomeSources)))
              )
            }
        }
      }
      else {
        Future.successful(Redirect(controllers.routes.OverviewPageController.show(taxYear)))
      }
  }

  def submit(taxYear: Int): Action[AnyContent] = (authorisedAction andThen taxYearAction(taxYear)).async {
    implicit user =>
      if(appConfig.tailoringEnabled) {
        val userAffinity = if (user.isAgent) "agent" else "individual"
        incomeSourcesService.getIncomeSources(user.nino, taxYear, user.mtditid).flatMap {
          case Left(_) => Future.successful(InternalServerError(errorHandler.internalServerErrorTemplate))
          case Right(incomeSources) => addSectionsForm(incomeSources).bindFromRequest().fold({
            form =>
              Future.successful(Ok(view(taxYear, user.isAgent, form, allJourneys, incomeSources)))
          }, {
            result =>
              tailoringSessionService.getSessionData(taxYear).flatMap {
                case Right(data) =>
                  data.map(_.tailoring).fold {
                    val auditResult = result.addSections.filter(journey => result.addSectionsRaw.contains(journey))
                    if (auditResult.nonEmpty) {
                      auditService.sendAudit(TailorAddIncomeSourcesDetail(
                        user.nino, user.mtditid, userAffinity, taxYear, SourcesDetail(auditResult)).toAuditModel)
                    }
                    if (appConfig.nrsEnabled) {
                      nrsService.submit(user.nino, SourcesDetail(auditResult), user.mtditid, "itsa-personal-income-submission")
                    }
                    tailoringSessionService.createSessionData(result.addSections, taxYear)(errorHandler.handleError(INTERNAL_SERVER_ERROR))(
                      Redirect(controllers.routes.OverviewPageController.show(taxYear)))
                  } {
                    currentData =>
                      var newData: Seq[String] = currentData
                      result.addSections.foreach(journey => if (!currentData.contains(journey)) newData = newData :+ journey)
                      if (!newData.equals(currentData)) {
                        auditService.sendAudit(TailorAddIncomeSourcesDetail(
                          user.nino, user.mtditid, userAffinity, taxYear,
                          SourcesDetail(newData.filter(!currentData.contains(_)))).toAuditModel)
                      }
                      tailoringSessionService.updateSessionData(newData, taxYear)(errorHandler.handleError(INTERNAL_SERVER_ERROR))(
                        Redirect(controllers.routes.OverviewPageController.show(taxYear)))
                  }
                case Left(_) => Future.successful(Redirect(controllers.routes.OverviewPageController.show(taxYear)))
              }
          })
        }
      }
      else {
        Future.successful(Redirect(controllers.routes.OverviewPageController.show(taxYear)))
      }
  }
}
