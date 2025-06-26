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

package config

import common.StatusMessage
import play.api.http.Status._
import play.api.i18n.MessagesApi
import play.api.mvc.Results._
import play.api.mvc.{RequestHeader, Result}
import play.twirl.api.Html
import uk.gov.hmrc.play.bootstrap.frontend.http.FrontendErrorHandler
import views.html.errors._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ErrorHandler @Inject()(val messagesApi: MessagesApi,
                             internalServerErrorPage: InternalServerErrorPage,
                             notFoundPage: NotFoundPage,
                             serviceUnavailablePage: ServiceUnavailablePage
                            )(implicit appConfig: AppConfig, val ec: ExecutionContext) extends FrontendErrorHandler {

  override def standardErrorTemplate(pageTitle: String, heading: String, message: String)(implicit request: RequestHeader): Future[Html] =
    Future.successful(internalServerErrorPage())

  override def notFoundTemplate(implicit request: RequestHeader): Future[Html] =
    Future.successful(notFoundPage())

  override def internalServerErrorTemplate(implicit request: RequestHeader): Future[Html] =
    Future.successful(internalServerErrorPage())

  def internalServerError()(implicit request: RequestHeader): Future[Result] =
    internalServerErrorTemplate.map(InternalServerError(_))

  def handleError(status: Int)(implicit request: RequestHeader): Result = {
    status match {
      case SERVICE_UNAVAILABLE => ServiceUnavailable(serviceUnavailablePage())
      case _ => InternalServerError(internalServerErrorPage())
    }
  }

  def handleIntentToCrystalliseError(status: Int, taxYear: Int)(implicit request: RequestHeader): Result = {
    status match {
      case FORBIDDEN => Redirect(controllers.errors.routes.NoUpdatesProvidedPageController.show(taxYear))
      case CONFLICT => Redirect(controllers.errors.routes.ReturnTaxYearExistsController.show(taxYear))
      case UNPROCESSABLE_ENTITY => Redirect(controllers.errors.routes.BusinessValidationRulesController.show(taxYear))
      case SERVICE_UNAVAILABLE => ServiceUnavailable(serviceUnavailablePage())
      case _ => InternalServerError(internalServerErrorPage())
    }
  }

  def handleDeclareCrystallisationError(status: Int, errorMessage: String, taxYear: Int)(implicit request: RequestHeader): Result = {
    (status, errorMessage) match {
      case (CONFLICT, StatusMessage.residencyChanged) => Redirect(controllers.errors.routes.AddressHasChangedPageController.show(taxYear))
      case (CONFLICT, StatusMessage.finalDeclarationReceived) => Redirect(controllers.errors.routes.ReturnTaxYearExistsController.show(taxYear))
      case (CONFLICT, _) => Redirect(controllers.errors.routes.TaxReturnPreviouslyUpdatedController.show(taxYear))
      case (UNPROCESSABLE_ENTITY, _) => Redirect(controllers.errors.routes.NoValidIncomeSourcesController.show(taxYear))
      case (SERVICE_UNAVAILABLE, _) => ServiceUnavailable(serviceUnavailablePage())
      case _ => InternalServerError(internalServerErrorPage())
    }
  }

  override def onClientError(requestHeader: RequestHeader, statusCode: Int, message: String): Future[Result] = {
    statusCode match {
      case NOT_FOUND =>
        notFoundTemplate(requestHeader).map(NotFound(_))
      case _ =>
        internalServerError()(requestHeader)
    }
  }
}
