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

import config.AppConfig
import controllers.predicates.{AuthorisedAction, TaxYearAction}
import javax.inject.{Inject, Singleton}
import play.api.i18n.I18nSupport
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.StartPage
import TaxYearAction.taxYearAction
import audit.{AuditService, EnterUpdateAndSubmissionServiceAuditDetail}
import common.SessionValues
import common.SessionValues.{DIVIDENDS_CYA, DIVIDENDS_PRIOR_SUB, EMPLOYMENT_CYA, EMPLOYMENT_PRIOR_SUB, GIFT_AID_CYA, GIFT_AID_PRIOR_SUB, INTEREST_CYA, INTEREST_PRIOR_SUB}
import services.{AuthService, IncomeTaxUserDataService}
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.affinityGroup

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class StartPageController @Inject()(val authorisedAction: AuthorisedAction,
                                    authService: AuthService,
                                    val startPageView: StartPage,
                                    auditService: AuditService,
                                    incomeTaxUserDataService: IncomeTaxUserDataService,
                                    implicit val appConfig: AppConfig,
                                    implicit val mcc: MessagesControllerComponents,
                                    implicit val ec: ExecutionContext
                                   ) extends FrontendController(mcc) with I18nSupport {

  def show(taxYear: Int): Action[AnyContent] = (authorisedAction andThen taxYearAction(taxYear, missingTaxYearReset = false)).async {
    implicit user =>

      user.session.

      incomeTaxUserDataService.

      Future.successful(
        Ok(startPageView(isAgent = user.isAgent, taxYear))
          .addingToSession(SessionValues.TAX_YEAR -> taxYear.toString)
          .removingFromSession(
            DIVIDENDS_CYA, INTEREST_CYA, GIFT_AID_CYA, EMPLOYMENT_CYA,
            DIVIDENDS_PRIOR_SUB, INTEREST_PRIOR_SUB, GIFT_AID_PRIOR_SUB, EMPLOYMENT_PRIOR_SUB
          ) //TODO Remove when year selection is available
      )
  }

  def submit(taxYear: Int): Action[AnyContent] = (authorisedAction andThen taxYearAction(taxYear)).async { implicit user =>
    authService.authorised.retrieve(affinityGroup) {
      case Some(retrievedAffinityGroup) =>
        auditService.sendAudit[EnterUpdateAndSubmissionServiceAuditDetail](
          EnterUpdateAndSubmissionServiceAuditDetail(retrievedAffinityGroup, user.nino).toAuditModel
        )
        Future.successful(Redirect(controllers.routes.OverviewPageController.show(taxYear)))
    }
  }

}
