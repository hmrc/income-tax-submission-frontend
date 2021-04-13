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
import audit.{AuditService, EnterUpdateAndSubmissionServiceDetail}
import services.AuthService
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.affinityGroup
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


@Singleton
class StartPageController @Inject()(
                                     val authorisedAction: AuthorisedAction,
                                     authService: AuthService,
                                     val startPageView: StartPage,
                                     auditService: AuditService,
                                     implicit val appConfig: AppConfig,
                                     implicit val mcc: MessagesControllerComponents
                                   ) extends FrontendController(mcc) with I18nSupport {

  def show(taxYear: Int): Action[AnyContent] = (authorisedAction andThen taxYearAction(taxYear)).async { implicit user =>
    Future.successful(Ok(startPageView(isAgent = user.isAgent, taxYear)))
  }

  def submit(taxYear: Int): Action[AnyContent] = (authorisedAction andThen taxYearAction(taxYear)).async { implicit user =>
    authService.authorised.retrieve(affinityGroup) {
      case Some(retrievedAffinityGroup) =>
        auditService.sendAudit[EnterUpdateAndSubmissionServiceDetail](EnterUpdateAndSubmissionServiceDetail(retrievedAffinityGroup).toAuditModel)
        Future.successful(Redirect(controllers.routes.OverviewPageController.show(taxYear)))
    }
  }

}
