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
import views.html.errors._
import play.api.http.Status._

class ErrorHandlerSpec extends UnitTest with GuiceOneAppPerSuite {

  val serviceUnavailable: ServiceUnavailablePage = app.injector.instanceOf[ServiceUnavailablePage]
  val internalServerErrorPage: InternalServerErrorPage = app.injector.instanceOf[InternalServerErrorPage]
  val notFoundPage: NotFoundPage = app.injector.instanceOf[NotFoundPage]

  val noUpdatesProvidedPage: NoUpdatesProvidedPage = app.injector.instanceOf[NoUpdatesProvidedPage]
  val returnTaxYearExistsView: ReturnTaxYearExistsView = app.injector.instanceOf[ReturnTaxYearExistsView]

  val addressHasChangedPage: AddressHasChangedPage = app.injector.instanceOf[AddressHasChangedPage]
  val taxReturnPreviouslyUpdatedView: TaxReturnPreviouslyUpdatedView = app.injector.instanceOf[TaxReturnPreviouslyUpdatedView]
  val noValidIncomeSourcesView: NoValidIncomeSourcesView = app.injector.instanceOf[NoValidIncomeSourcesView]
  val businessValidationRulesView: BusinessValidationRulesView = app.injector.instanceOf[BusinessValidationRulesView]

  implicit val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit lazy val messages: Messages = messagesApi.preferred(FakeRequest())

  val errorHandler = new ErrorHandler(messagesApi, internalServerErrorPage, notFoundPage, serviceUnavailable,
                                      noUpdatesProvidedPage, returnTaxYearExistsView, addressHasChangedPage,
                                      noValidIncomeSourcesView, taxReturnPreviouslyUpdatedView, businessValidationRulesView)

  val taxYear: Int = 2022
  val isAgent: Boolean = false

  ".handleError" should {

    "return a 503 page for service unavailable" in {
      errorHandler.handleError(SERVICE_UNAVAILABLE)
        .header.status shouldBe SERVICE_UNAVAILABLE
    }

    "return a 500 page for internal server error" in {
      errorHandler.handleError(INTERNAL_SERVER_ERROR)
        .header.status shouldBe INTERNAL_SERVER_ERROR
    }

    "return a 404 page" in {
      errorHandler.onClientError(fakeRequest, NOT_FOUND,"")
        .map(_.header.status shouldBe NOT_FOUND)
    }
  }

  ".handleIntentToCrystalliseError" should {

    "return a 403 page for No Updates Provided" in {
      errorHandler.handleIntentToCrystalliseError(FORBIDDEN, isAgent, taxYear)
        .header.status shouldBe FORBIDDEN
    }

    "return a 409 page for Return Tax Year Exists" in {
      errorHandler.handleIntentToCrystalliseError(CONFLICT, isAgent, taxYear)
        .header.status shouldBe CONFLICT
    }

    "return a 422 page for Business Validation Rules error" in {
      errorHandler.handleIntentToCrystalliseError(UNPROCESSABLE_ENTITY, isAgent, taxYear)
    }

    "return a 503 page for service unavailable" in {
      errorHandler.handleIntentToCrystalliseError(SERVICE_UNAVAILABLE, isAgent, taxYear)
        .header.status shouldBe SERVICE_UNAVAILABLE
    }

    "return a 500 page for internal server error" in {
      errorHandler.handleIntentToCrystalliseError(INTERNAL_SERVER_ERROR, isAgent, taxYear)
        .header.status shouldBe INTERNAL_SERVER_ERROR
    }
  }

  ".handleDeclareCrystallisationError" should {

    "return a 409 page for Address has changed" in {
      errorHandler.handleDeclareCrystallisationError(CONFLICT, "RESIDENCY_CHANGED", isAgent, taxYear)
        .header.status shouldBe CONFLICT
    }

    "return a 409 page for Tax Return Previously Updated" in {
      errorHandler.handleDeclareCrystallisationError(CONFLICT, "INCOME_SOURCES_CHANGED", isAgent, taxYear)
        .header.status shouldBe CONFLICT
    }

    "return a 409 page for Recent Submissions Exist" in {
      errorHandler.handleDeclareCrystallisationError(CONFLICT, "RECENT_SUBMISSIONS_EXIST", isAgent, taxYear)
        .header.status shouldBe CONFLICT
    }

    "return a 409 page for Final Declaration Received" in {
      errorHandler.handleDeclareCrystallisationError(CONFLICT, "FINAL_DECLARATION_RECEIVED", isAgent, taxYear)
        .header.status shouldBe CONFLICT
    }

    "return a 422 page for No Valid Income Sources" in {
      errorHandler.handleDeclareCrystallisationError(UNPROCESSABLE_ENTITY, "CRYSTALLISATION_BEFORE_TAX_YEAR_END", isAgent, taxYear)
        .header.status shouldBe UNPROCESSABLE_ENTITY
    }

    "return a 503 page for service unavailable" in {
      errorHandler.handleDeclareCrystallisationError(SERVICE_UNAVAILABLE, "", isAgent, taxYear)
        .header.status shouldBe SERVICE_UNAVAILABLE
    }

    "return a 500 page for internal server error" in {
      errorHandler.handleDeclareCrystallisationError(INTERNAL_SERVER_ERROR, "", isAgent, taxYear)
        .header.status shouldBe INTERNAL_SERVER_ERROR
    }

  }

}
