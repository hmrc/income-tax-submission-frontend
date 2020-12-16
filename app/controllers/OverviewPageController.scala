/*
 * Copyright 2020 HM Revenue & Customs
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

import common.SessionValues
import config.FrontendAppConfig
import controllers.predicates.AuthorisedAction
import javax.inject.{Inject, Singleton}
import play.api.i18n.I18nSupport
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.OverviewPageView
import services.IncomeSourcesService
import common.SessionValues._
import play.api.libs.json.Json

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class OverviewPageController @Inject()(
                                        appConfig: FrontendAppConfig,
                                        mcc: MessagesControllerComponents,
                                        implicit val ec: ExecutionContext,
                                        incomeSourcesService: IncomeSourcesService,
                                        overviewPageView: OverviewPageView,
                                        authorisedAction: AuthorisedAction) extends FrontendController(mcc) with I18nSupport {

  implicit val config: FrontendAppConfig = appConfig

  def show(taxYear: Int): Action[AnyContent] = authorisedAction.async { implicit user =>
    user.session.get(SessionValues.NINO) match {
      case Some(nino) =>
        incomeSourcesService.getIncomeSources(nino, taxYear, user.mtditid).map{
          case Right(incomeSources) => {
            val processedDividends = Json.toJson(incomeSources.dividends).toString()
            val processedInterests = Json.toJson(incomeSources.interests).toString()
            Ok(overviewPageView(isAgent = user.isAgent, Some(incomeSources), taxYear))
              .addingToSession(
                DIVIDENDS_PRIOR_SUB -> processedDividends,
                INTEREST_PRIOR_SUB -> processedInterests
              )
          }
          case _ => Ok(overviewPageView(isAgent = user.isAgent, None, taxYear))
        }
      case _ =>
        Future.successful(Redirect(appConfig.signInUrl))
    }
  }


}

