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
import play.api.mvc.Result
import play.api.test.Helpers.{OK, status, writeableOf_AnyContentAsEmpty}
import play.api.test.{FakeRequest, Helpers}
import views.html.errors.AddressHasChangedPage

import scala.concurrent.Future

class AddressHasChangedPageControllerISpec extends IntegrationTest with ViewHelpers {

  lazy val frontendAppConfig: AppConfig = app.injector.instanceOf[AppConfig]

  val taxYear = 2022

  object Selectors {
    val headingSelector = "#main-content > div > div > header > h1"
    val addressHasChangedSelector = "#main-content > div > div > div.govuk-body > p:nth-child(1)"
    val submitYourReturnSelector = "#main-content > div > div > div.govuk-body > p:nth-child(2)"
    val incomeTaxReturnButtonSelector = "#returnToOverviewPageBtn"
    val addressHasChangedPageView: AddressHasChangedPage = app.injector.instanceOf[AddressHasChangedPage]
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
    val incomeTaxReturnButtonLink = s"http://localhost:9302/income-through-software/return/$taxYear/view"
  }

  import Selectors._
  import ExpectedResults._

  private val urlPath = s"/income-through-software/return/$taxYear/address-changed"

  "Rendering the address change error page in English" should {

    val headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck")

    "render correctly when the user is an individual" should {
      val request = FakeRequest("GET", urlPath).withHeaders(headers: _*)

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
      val request = FakeRequest("GET", urlPath).withHeaders(headers: _*)

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

  "Rendering the address change error page in Welsh" should {

    val headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck", HeaderNames.ACCEPT_LANGUAGE -> "cy")

    "render correctly when the user is an individual" should {
      val request = FakeRequest("GET", urlPath).withHeaders(headers: _*)

      lazy val result: Future[Result] = {
        authoriseIndividual()
        route(app, request).get
      }

      implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

      "returns status of OK(200)" in {
        status(result) shouldBe OK
      }

      welshToggleCheck("Welsh")
      titleCheck(titleIndividual)
      h1Check(headingIndividual, "xl")
      textOnPageCheck(addressHasChangedTextIndividual, addressHasChangedSelector)
      textOnPageCheck(submitYourReturnIndividual, submitYourReturnSelector)
      textOnPageCheck(incomeTaxReturnButtonText, incomeTaxReturnButtonSelector)
      buttonCheck(incomeTaxReturnButtonText, incomeTaxReturnButtonSelector, Some(incomeTaxReturnButtonLink))
    }

    "render correctly when the user is an agent" should {
      val request = FakeRequest("GET", urlPath).withHeaders(headers: _*)

      lazy val result: Future[Result] = {
        authoriseAgent()
        route(app, request).get
      }

      implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

      "returns status of OK(200)" in {
        status(result) shouldBe OK
      }

      welshToggleCheck("Welsh")
      titleCheck(titleAgent)
      h1Check(headingAgent, "xl")
      textOnPageCheck(addressHasChangedTextAgent, addressHasChangedSelector)
      textOnPageCheck(submitYourReturnAgent, submitYourReturnSelector)
      textOnPageCheck(incomeTaxReturnButtonText, incomeTaxReturnButtonSelector)
      buttonCheck(incomeTaxReturnButtonText, incomeTaxReturnButtonSelector, Some(incomeTaxReturnButtonLink))
    }
  }

}
