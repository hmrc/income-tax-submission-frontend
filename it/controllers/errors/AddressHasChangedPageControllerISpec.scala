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

package controllers.errors

import config.AppConfig
import itUtils.{IntegrationTest, ViewHelpers}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.http.HeaderNames
import play.api.http.Status.SEE_OTHER
import play.api.mvc.Result
import play.api.test.Helpers.{OK, status, writeableOf_AnyContentAsEmpty}
import play.api.test.{FakeRequest, Helpers}

import scala.concurrent.Future

class AddressHasChangedPageControllerISpec extends IntegrationTest with ViewHelpers {

  lazy val frontendAppConfig: AppConfig = app.injector.instanceOf[AppConfig]

  val taxYear = 2021

  object Selectors {
    val headingSelector = "#main-content > div > div > header > h1"
    val addressHasChangedSelector = "#main-content > div > div > div.govuk-body > p:nth-child(1)"
    val submitYourReturnSelector = "#main-content > div > div > div.govuk-body > p:nth-child(2)"
    val incomeTaxReturnButtonSelector = "#returnToOverviewPageBtn"
    val incomeTaxReturnButtonLink = s"http://localhost:9302/income-through-software/return/$taxYear/view"
  }

  object ExpectedResults {
    val titleIndividual = "Your address has changed"
    val titleAgent = "Your client’s address has changed"
    val headingIndividual = "Your address has changed"
    val headingAgent = "Your client’s address has changed"
    val addressHasChangedTextIndividual = "Your address has changed to a country with a different rate of tax."
    val addressHasChangedTextAgent = "Your client’s address has changed to a country with a different rate of tax."
    val submitYourReturnIndividual = "You must submit your Income Tax Return again to get a new tax calculation."
    val submitYourReturnAgent = "You must submit your client’s Income Tax Return again to get a new tax calculation."
    val incomeTaxReturnButtonText = "Back to Income Tax Return"
  }

  object ExpectedResultsWelsh {
    val titleIndividualWelsh = "Your address has changed"
    val titleAgentWelsh = "Your client’s address has changed"
    val headingIndividualWelsh = "Your address has changed"
    val headingAgentWelsh = "Your client’s address has changed"
    val addressHasChangedTextIndividualWelsh = "Your address has changed to a country with a different rate of tax."
    val addressHasChangedTextAgentWelsh = "Your client’s address has changed to a country with a different rate of tax."
    val submitYourReturnIndividualWelsh = "You must submit your Income Tax Return again to get a new tax calculation."
    val submitYourReturnAgentWelsh = "You must submit your client’s Income Tax Return again to get a new tax calculation."
    val incomeTaxReturnButtonTextWelsh = "Back to Income Tax Return"
  }

  import Selectors._

  private def urlPath(taxYear: Int = taxYear) = s"/income-through-software/return/$taxYear/address-changed"

  "Rendering the address change error page in English" should {
    import ExpectedResults._

    val headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck")

    "render correctly when the user is an individual" should {
      val request = FakeRequest("GET", urlPath()).withHeaders(headers: _*)

      lazy val result: Future[Result] = {
        authoriseIndividual()
        route(app, request).get
      }

      implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

      "returns status of OK(200)" in {
        status(result) shouldBe OK
      }

      welshToggleCheck("English")
      titleCheck(titleIndividual)
      h1Check(headingIndividual, "xl")
      textOnPageCheck(addressHasChangedTextIndividual, addressHasChangedSelector)
      textOnPageCheck(submitYourReturnIndividual, submitYourReturnSelector)
      textOnPageCheck(incomeTaxReturnButtonText, incomeTaxReturnButtonSelector)
      buttonCheck(incomeTaxReturnButtonText, incomeTaxReturnButtonSelector, Some(incomeTaxReturnButtonLink))
    }

    "render correctly when the user is an agent" should {
      val request = FakeRequest("GET", urlPath()).withHeaders(headers: _*)

      lazy val result: Future[Result] = {
        authoriseAgent()
        route(app, request).get
      }

      implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

      "returns status of OK(200)" in {
        status(result) shouldBe OK
      }

      welshToggleCheck("English")
      titleCheck(titleAgent)
      h1Check(headingAgent, "xl")
      textOnPageCheck(addressHasChangedTextAgent, addressHasChangedSelector)
      textOnPageCheck(submitYourReturnAgent, submitYourReturnSelector)
      textOnPageCheck(incomeTaxReturnButtonText, incomeTaxReturnButtonSelector)
      buttonCheck(incomeTaxReturnButtonText, incomeTaxReturnButtonSelector, Some(incomeTaxReturnButtonLink))
    }
  }
  "Attempting to Render the address change error page in year" should {

    val headers = Seq(HeaderNames.COOKIE -> playSessionCookies(frontendAppConfig.defaultTaxYear), "Csrf-Token" -> "nocheck")

    "render correctly when the user is an individual" should {
      val request = FakeRequest("GET", urlPath(frontendAppConfig.defaultTaxYear)).withHeaders(headers: _*)

      lazy val result: Future[Result] = {
        authoriseIndividual()
        route(app, request).get
      }

      "returns status of SEE_OTHER(303) and the overview page" in {
        status(result) shouldBe SEE_OTHER
        redirectUrl(result) shouldBe appConfig.overviewUrl(frontendAppConfig.defaultTaxYear)
      }
    }
  }

  "Rendering the address change error page in Welsh" should {
    import ExpectedResultsWelsh._

    val headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck", HeaderNames.ACCEPT_LANGUAGE -> "cy")

    "render correctly when the user is an individual" should {
      val request = FakeRequest("GET", urlPath()).withHeaders(headers: _*)

      lazy val result: Future[Result] = {
        authoriseIndividual()
        route(app, request).get
      }

      implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

      "returns status of OK(200)" in {
        status(result) shouldBe OK
      }

      welshToggleCheck("Welsh")
      titleCheck(titleIndividualWelsh)
      h1Check(headingIndividualWelsh, "xl")
      textOnPageCheck(addressHasChangedTextIndividualWelsh, addressHasChangedSelector)
      textOnPageCheck(submitYourReturnIndividualWelsh, submitYourReturnSelector)
      textOnPageCheck(incomeTaxReturnButtonTextWelsh, incomeTaxReturnButtonSelector)
      buttonCheck(incomeTaxReturnButtonTextWelsh, incomeTaxReturnButtonSelector, Some(incomeTaxReturnButtonLink))
    }

    "render correctly when the user is an agent" should {
      val request = FakeRequest("GET", urlPath()).withHeaders(headers: _*)

      lazy val result: Future[Result] = {
        authoriseAgent()
        route(app, request).get
      }

      implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

      "returns status of OK(200)" in {
        status(result) shouldBe OK
      }

      welshToggleCheck("Welsh")
      titleCheck(titleAgentWelsh)
      h1Check(headingAgentWelsh, "xl")
      textOnPageCheck(addressHasChangedTextAgentWelsh, addressHasChangedSelector)
      textOnPageCheck(submitYourReturnAgentWelsh, submitYourReturnSelector)
      textOnPageCheck(incomeTaxReturnButtonTextWelsh, incomeTaxReturnButtonSelector)
      buttonCheck(incomeTaxReturnButtonTextWelsh, incomeTaxReturnButtonSelector, Some(incomeTaxReturnButtonLink))
    }
  }

}
