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

import audit.{AuditService, IVHandoffAuditDetail, IVSuccessAuditDetail}
import common.SessionValues
import config.AppConfig
import controllers.predicates.AuthorisedAction
import javax.inject.{Inject, Singleton}
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.AuthService
import uk.gov.hmrc.auth.core.ConfidenceLevel
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{affinityGroup, confidenceLevel}
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.SessionDataHelper

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IVUpliftController @Inject()(implicit appConfig: AppConfig,
                                   mcc: MessagesControllerComponents,
                                   auditService: AuditService,
                                   implicit val authService: AuthService,
                                   val authorisedAction: AuthorisedAction,
                                   implicit val ec: ExecutionContext) extends FrontendController(mcc) with I18nSupport with SessionDataHelper{

  val minimumConfidenceLevel :Int = ConfidenceLevel.L250.level

  def sessionConfidenceLevel(implicit headerCarrier: HeaderCarrier): Future[(String, Int)] = {
    authService.authorised().retrieve(affinityGroup and confidenceLevel){
      case Some(affinityGroup) ~ confidenceLevel => Future.successful(affinityGroup.toString, confidenceLevel.level)
    }
  }

  def initialiseJourney: Action[AnyContent] = Action.async { implicit request =>
    sessionConfidenceLevel.map { response =>

      val handoffReason = response._1
      val confidenceLevel = response._2

      val model = IVHandoffAuditDetail(handoffReason.toLowerCase(), confidenceLevel , minimumConfidenceLevel)
      auditService.sendAudit(model.toAuditModel)
      Redirect(appConfig.ivUpliftUrl)
    }
  }

  def callback: Action[AnyContent] = authorisedAction { implicit user =>

    val model = IVSuccessAuditDetail(user.nino)
    auditService.sendAudit(model.toAuditModel)

    getSessionData[Int](SessionValues.TAX_YEAR) match {
      case Some(taxYear) => Redirect(routes.StartPageController.show(taxYear))
      case None => Redirect(routes.StartPageController.show(appConfig.defaultTaxYear))
    }
  }
}
