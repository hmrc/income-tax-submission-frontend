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

import java.util.UUID

import audit.{AuditModel, AuditService, IVFailureAuditDetail}
import config.AppConfig
import javax.inject.{Inject, Singleton}
import play.api.Logger.logger
import play.api.i18n.I18nSupport
import play.api.mvc._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.SessionDataHelper
import views.html.errors.IVFailurePage

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IVFailureController @Inject()(implicit appConfig: AppConfig,
                                    mcc: MessagesControllerComponents,
                                    view: IVFailurePage,
                                    auditService: AuditService,
                                    implicit val ec: ExecutionContext) extends FrontendController(mcc) with I18nSupport with SessionDataHelper{

  def show(journeyId: Option[String]): Action[AnyContent] = Action { implicit request =>

    lazy val sessionId: Option[String] = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session)).sessionId.map(_.value)

    if(journeyId.isEmpty){
      logger.warn(s"[IVFailureController][show] JourneyId from IV journey is empty. Defaulting journeyId for audit purposes." +
        s"${if(sessionId.isDefined) s" SessionId: ${sessionId.get}" else ""}")
    }

    val idForAuditing: String = journeyId.getOrElse(sessionId.getOrElse(UUID.randomUUID().toString))
    ivFailureAuditSubmission(IVFailureAuditDetail(idForAuditing))
    Ok(view(controllers.routes.SignOutController.signOut()))
  }

  private def ivFailureAuditSubmission(details: IVFailureAuditDetail)
                                      (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[AuditResult] = {
    val event = AuditModel("LowConfidenceLevelIvOutcomeFail", "LowConfidenceLevelIvOutcomeFail", details)
    auditService.auditModel(event)
  }
}
