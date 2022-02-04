/*
 * Copyright 2022 HM Revenue & Customs
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
import play.api.test.Helpers.{CONFLICT, status, writeableOf_AnyContentAsEmpty}
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
    val incomeTaxReturnButtonLink = s"http://localhost:9302/update-and-submit-income-tax-return/$taxYear/view"
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
    val incomeTaxReturnButtonTextIndividual = "Back to your Income Tax Return"
    val incomeTaxReturnButtonTextAgent = "Back to Income Tax Return"
  }

  object ExpectedResultsWelsh {
    val titleIndividualWelsh = "Mae’ch cyfeiriad wedi’i newid"
    val titleAgentWelsh = "Mae cyfeiriad eich cleient wedi newid"
    val headingIndividualWelsh = "Mae’ch cyfeiriad wedi’i newid"
    val headingAgentWelsh = "Mae cyfeiriad eich cleient wedi newid"
    val addressHasChangedTextIndividualWelsh = "Mae eich cyfeiriad wedi newid i wlad gyda chyfradd dreth wahanol."
    val addressHasChangedTextAgentWelsh = "Mae cyfeiriad eich cleient wedi newid i wlad gyda chyfradd dreth wahanol."
    val submitYourReturnIndividualWelsh = "Rhaid i chi gyflwyno eich Ffurflen Dreth Incwm eto i gael cyfrifiad treth newydd."
    val submitYourReturnAgentWelsh = "Rhaid i chi gyflwyno Ffurflen Dreth Incwm eich cleient eto i gael cyfrifiad treth newydd."
    val incomeTaxReturnButtonTextIndividualWelsh = "Yn ôl i’ch Ffurflen Dreth ar gyfer Treth Incwm"
    val incomeTaxReturnButtonTextAgentWelsh = "Yn ôl i’r Ffurflen Dreth ar gyfer Treth Incwm"
  }

  import Selectors._

  private def urlPath(taxYear: Int = taxYear) = s"/update-and-submit-income-tax-return/$taxYear/address-changed"

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

      "returns status of CONFLICT(409)" in {
        status(result) shouldBe CONFLICT
      }

      welshToggleCheck("English")
      titleCheck(titleIndividual, isWelsh = false)
      h1Check(headingIndividual, "xl")
      textOnPageCheck(addressHasChangedTextIndividual, addressHasChangedSelector)
      textOnPageCheck(submitYourReturnIndividual, submitYourReturnSelector)
      textOnPageCheck(incomeTaxReturnButtonTextIndividual, incomeTaxReturnButtonSelector)
      buttonCheck(incomeTaxReturnButtonTextIndividual, incomeTaxReturnButtonSelector, Some(incomeTaxReturnButtonLink))
    }

    "render correctly when the user is an agent" should {
      val request = FakeRequest("GET", urlPath()).withHeaders(headers: _*)

      lazy val result: Future[Result] = {
        authoriseAgent()
        route(app, request).get
      }

      implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

      "returns status of CONFLICT(409)" in {
        status(result) shouldBe CONFLICT
      }

      welshToggleCheck("English")
      titleCheck(titleAgent, isWelsh = false)
      h1Check(headingAgent, "xl")
      textOnPageCheck(addressHasChangedTextAgent, addressHasChangedSelector)
      textOnPageCheck(submitYourReturnAgent, submitYourReturnSelector)
      textOnPageCheck(incomeTaxReturnButtonTextAgent, incomeTaxReturnButtonSelector)
      buttonCheck(incomeTaxReturnButtonTextAgent, incomeTaxReturnButtonSelector, Some(incomeTaxReturnButtonLink))
    }
  }
  "Attempting to Render the address change error page in year" should {

    val headers = Seq(HeaderNames.COOKIE -> playSessionCookies(frontendAppConfig.defaultTaxYear), "Csrf-Token" -> "nocheck")

    "fail to render and return a redirect that" should {
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

      "returns status of CONFLICT(409)" in {
        status(result) shouldBe CONFLICT
      }

      welshToggleCheck("Welsh")
      titleCheck(titleIndividualWelsh, isWelsh = true)
      h1Check(headingIndividualWelsh, "xl")
      textOnPageCheck(addressHasChangedTextIndividualWelsh, addressHasChangedSelector)
      textOnPageCheck(submitYourReturnIndividualWelsh, submitYourReturnSelector)
      textOnPageCheck(incomeTaxReturnButtonTextIndividualWelsh, incomeTaxReturnButtonSelector)
      buttonCheck(incomeTaxReturnButtonTextIndividualWelsh, incomeTaxReturnButtonSelector, Some(incomeTaxReturnButtonLink))
    }

    "render correctly when the user is an agent" should {
      val request = FakeRequest("GET", urlPath()).withHeaders(headers: _*)

      lazy val result: Future[Result] = {
        authoriseAgent()
        route(app, request).get
      }

      implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

      "returns status of CONFLICT(409)" in {
        status(result) shouldBe CONFLICT
      }

      welshToggleCheck("Welsh")
      titleCheck(titleAgentWelsh, isWelsh = true)
      h1Check(headingAgentWelsh, "xl")
      textOnPageCheck(addressHasChangedTextAgentWelsh, addressHasChangedSelector)
      textOnPageCheck(submitYourReturnAgentWelsh, submitYourReturnSelector)
      textOnPageCheck(incomeTaxReturnButtonTextAgentWelsh, incomeTaxReturnButtonSelector)
      buttonCheck(incomeTaxReturnButtonTextAgentWelsh, incomeTaxReturnButtonSelector, Some(incomeTaxReturnButtonLink))
    }
  }

}
