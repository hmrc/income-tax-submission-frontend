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

import audit.{AuditService, EnterUpdateAndSubmissionServiceAuditDetail}
import common.SessionValues
import controllers.predicates.{AuthorisedAction, InYearAction}
import config.{AppConfig, ErrorHandler}
import controllers.predicates.TaxYearAction.taxYearAction

import javax.inject.{Inject, Singleton}
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.{AuthService, ValidTaxYearListService}
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.affinityGroup
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.StartPageView

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class StartPageController @Inject()(val authorisedAction: AuthorisedAction,
                                    authService: AuthService,
                                    val startPageView: StartPageView,
                                    auditService: AuditService,
                                    inYearAction: InYearAction,
                                    implicit val appConfig: AppConfig,
                                    implicit val mcc: MessagesControllerComponents,
                                    implicit val ec: ExecutionContext,
                                    implicit val validTaxYearListService: ValidTaxYearListService,
                                    implicit val errorHandler: ErrorHandler
                                   ) extends FrontendController(mcc) with I18nSupport {

  def show(taxYear: Int): Action[AnyContent] = (authorisedAction andThen taxYearAction(taxYear, missingTaxYearReset = false)) {
    implicit user =>
      if(appConfig.tailoringPhase2Enabled) {
        Redirect(appConfig.tailorReturnStartPageUrl(taxYear)).addingToSession(SessionValues.TAX_YEAR -> taxYear.toString)
      } else {
        Ok(startPageView(isAgent = user.isAgent, taxYear, inYearAction.inYear(taxYear))).addingToSession(SessionValues.TAX_YEAR -> taxYear.toString)
      }
  }

  def submit(taxYear: Int): Action[AnyContent] = (authorisedAction andThen taxYearAction(taxYear)).async { implicit user =>
    authService.authorised().retrieve(affinityGroup) {
      case Some(retrievedAffinityGroup) =>
        auditService.sendAudit[EnterUpdateAndSubmissionServiceAuditDetail](
          EnterUpdateAndSubmissionServiceAuditDetail(retrievedAffinityGroup, user.nino).toAuditModel
        )
        Future.successful(Redirect(controllers.routes.OverviewPageController.show(taxYear)))
      case _ => Future.successful(errorHandler.handleError(INTERNAL_SERVER_ERROR))
    }
  }

}
