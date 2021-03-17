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

import audit.{AuditModel, AuditService, IVHandoffAuditDetail}
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import common.SessionValues
import config.AppConfig
import javax.inject.{Inject, Singleton}
import play.api.i18n.I18nSupport
import play.api.mvc._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.SessionDataHelper

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IVUpliftController @Inject()(implicit appConfig: AppConfig,
                                   mcc: MessagesControllerComponents,
                                   auditService: AuditService,
                                   implicit val ec: ExecutionContext) extends FrontendController(mcc) with I18nSupport with SessionDataHelper{

  def initialiseJourney: Action[AnyContent] = Action { implicit request =>

    implicit lazy val headerCarrier: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))
    val model = IVHandoffAuditDetail("individual", 1  , 200)
    IVHandoffAuditSubmission(model)
    Redirect(appConfig.ivUpliftUrl)
  }

  def callback: Action[AnyContent] = Action { implicit request =>
    //TODO Implement success audit event
    getSessionData[Int](SessionValues.TAX_YEAR) match {
      case Some(taxYear) => Redirect(routes.StartPageController.show(taxYear))
      case None => Redirect(routes.StartPageController.show(appConfig.defaultTaxYear))
    }
  }

  private def IVHandoffAuditSubmission(details: IVHandoffAuditDetail)
                             (implicit hc: HeaderCarrier,
                              executionContext: ExecutionContext): Future[AuditResult] = {
    val event = AuditModel("LowConfidenceLevelIvHandoff", "LowConfidenceLevelIvHandoff", details)
    auditService.auditModel(event)
  }
}
