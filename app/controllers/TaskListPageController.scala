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

import common.SessionValues
import config.{AppConfig, ErrorHandler}
import controllers.predicates.TaxYearAction.taxYearAction
import controllers.predicates.{AuthorisedAction, InYearAction}
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.{TaskListService, ValidTaxYearListService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewmodels.TaskListPageViewModel
import views.html.TaskListPageView

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class TaskListPageController @Inject()(inYearAction: InYearAction,
                                       taskListPageView: TaskListPageView,
                                       taskListService: TaskListService,
                                       authorisedAction: AuthorisedAction,
                                       implicit val validTaxYearListService: ValidTaxYearListService,
                                       implicit val errorHandler: ErrorHandler)
                                      (implicit appConfig: AppConfig, mcc: MessagesControllerComponents, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport with Logging {

  def show(taxYear: Int): Action[AnyContent] = (authorisedAction andThen taxYearAction(taxYear)).async { implicit user =>
    val isInYear: Boolean = inYearAction.inYear(taxYear)
    taskListService.getTaskList(user.nino, taxYear)(hc.withExtraHeaders("MTDITID" -> user.mtditid)).map {
      case Left(error) =>
        errorHandler.handleError(error.status)
      case Right(None) =>
        Redirect(appConfig.tailorReturnAddSectionsPageUrl(taxYear)).addingToSession(SessionValues.TAX_YEAR -> taxYear.toString)
      case Right(taskListData) =>
        val prefix = if (user.isAgent) "taskList.agent." else "taskList."
        val vm = TaskListPageViewModel(taskListData, prefix)
        Ok(taskListPageView(user.isAgent, taxYear, isInYear, vm))
    }
  }
}
