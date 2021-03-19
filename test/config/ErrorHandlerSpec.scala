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

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Messages, MessagesApi}
import play.api.test.FakeRequest
import utils.UnitTest
import views.html.errors.{InternalServerErrorPage, NotFoundPage, ServiceUnavailablePage}
import play.api.http.Status._

class ErrorHandlerSpec extends UnitTest with GuiceOneAppPerSuite {

  val serviceUnavailable: ServiceUnavailablePage = app.injector.instanceOf[ServiceUnavailablePage]
  val internalServerErrorPage: InternalServerErrorPage = app.injector.instanceOf[InternalServerErrorPage]
  val notFoundPage: NotFoundPage = app.injector.instanceOf[NotFoundPage]

  implicit val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit lazy val messages: Messages = messagesApi.preferred(FakeRequest())

  val errorHandler = new ErrorHandler(messagesApi, internalServerErrorPage, notFoundPage, serviceUnavailable)

  ".handleError" should {

    "return a 503 page for service unavailable" in {

      errorHandler.handleError(SERVICE_UNAVAILABLE).header.status shouldBe SERVICE_UNAVAILABLE
    }

    "return a 500 page for internal server error" in {

      errorHandler.handleError(INTERNAL_SERVER_ERROR).header.status shouldBe INTERNAL_SERVER_ERROR
    }
    "return a 404 page" in {

      errorHandler.onClientError(fakeRequest, NOT_FOUND,"").map(_.header.status shouldBe NOT_FOUND)
    }
  }

}
