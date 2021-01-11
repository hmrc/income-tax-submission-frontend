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

import common.SessionValues
import common.SessionValues._
import config.FrontendAppConfig
import controllers.predicates.AuthorisedAction
import javax.inject.{Inject, Singleton}
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc._
import services.IncomeSourcesService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.OverviewPageView

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
            var result = Ok(overviewPageView(isAgent = user.isAgent, Some(incomeSources), taxYear))

            if (incomeSources.dividends.isDefined){
              result = result.addingToSession(DIVIDENDS_PRIOR_SUB -> Json.toJson(incomeSources.dividends).toString())
            }
            if (incomeSources.interest.isDefined){
              result = result.addingToSession(INTEREST_PRIOR_SUB -> Json.toJson(incomeSources.interest).toString())
            }

            result
          }
          case _ => Ok(overviewPageView(isAgent = user.isAgent, None, taxYear))
        }
      case _ =>
        Future.successful(Redirect(appConfig.signInUrl))
    }
  }

  def getCalculation(taxYear: Int): Action[AnyContent] = authorisedAction.async { implicit user =>
    Future.successful(Redirect(appConfig.viewAndChangeCalculationUrl(taxYear)).addingToSession(CALCULATION_ID -> ""))
  }


}

