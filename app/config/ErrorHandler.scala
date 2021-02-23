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

package config

import connectors.httpparsers.IncomeSourcesHttpParser.{IncomeSourcesError, IncomeSourcesServiceUnavailableError}
import javax.inject.{Inject, Singleton}
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.Results._
import play.api.mvc.{Request, RequestHeader, Result}
import play.twirl.api.Html
import uk.gov.hmrc.play.bootstrap.frontend.http.FrontendErrorHandler
import views.html.errors.{InternalServerErrorPage, NotFoundPage, ServiceUnavailablePage}

import scala.concurrent.Future

@Singleton
class ErrorHandler @Inject()(val messagesApi: MessagesApi,
                             internalServerErrorPage: InternalServerErrorPage, notFoundPage: NotFoundPage,
                             serviceUnavailablePage: ServiceUnavailablePage)(implicit appConfig: AppConfig)

  extends FrontendErrorHandler {

  override def standardErrorTemplate(pageTitle: String, heading: String, message: String)(implicit request: Request[_]): Html =
    internalServerErrorPage()

  override def notFoundTemplate(implicit request: Request[_]): Html = notFoundPage()

  override def internalServerErrorTemplate(implicit request: Request[_]): Html = internalServerErrorPage()

  def handleError(error: IncomeSourcesError)(implicit request: Request[_]): Result = {
    error match {
      case IncomeSourcesServiceUnavailableError => ServiceUnavailable(serviceUnavailablePage())
      case _ => InternalServerError(internalServerErrorPage())
    }
  }

  override def onClientError(requestHeader: RequestHeader, statusCode: Int, message: String): Future[Result] = Future.successful {
    statusCode match {
      case play.mvc.Http.Status.NOT_FOUND =>
        NotFound(notFoundTemplate(requestHeader.withBody("")))
      case _ =>
        InternalServerError(internalServerErrorPage()(requestHeader.withBody(""), messagesApi.preferred(requestHeader), appConfig))
    }
  }
}
