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

import audit.AuditService
import config.AppConfig
import controllers.predicates.AuthorisedAction
import itUtils.{IntegrationTest, ViewHelpers}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.http.HeaderNames
import play.api.mvc.Result
import play.api.test.Helpers.{OK, SEE_OTHER, status, writeableOf_AnyContentAsEmpty}
import play.api.test.{FakeRequest, Helpers}
import services.AuthService
import views.html.errors.AddressHasChangedPage

import scala.concurrent.Future

class AddressHasChangedPageControllerTest extends IntegrationTest with ViewHelpers {

  lazy val frontendAppConfig: AppConfig = app.injector.instanceOf[AppConfig]

  def controller: AddressHasChangedPageController = new AddressHasChangedPageController(
    app.injector.instanceOf[AuthorisedAction],
    app.injector.instanceOf[AuthService],
    app.injector.instanceOf[AddressHasChangedPage],
    app.injector.instanceOf[AuditService],
    frontendAppConfig,
    mcc,
    scala.concurrent.ExecutionContext.Implicits.global
  )

  val taxYear = 2022
  val taxYearMinusOne: Int = taxYear - 1
  val taxYearPlusOne: Int = taxYear + 1

  object ExpectedResultsEnglish {
    val titleIndividual = "Your address has changed"
    val titleAgent = "Your client’s address has changed"
    val headingIndividual = "Your address has changed"
    val headingAgent = "Your client’s address has changed"
    val addressHasChangedTextIndividual = "Your address has changed to a country with a different rate of tax."
    val addressHasChangedTextAgent = "Your client’s address has changed to a country with a different rate of tax."
    val submitYourReturnIndividual = "You must submit your Income Tax Return again to get a new tax calculation."
    val submitYourReturnAgent = "You must submit your client’s Income Tax Return again to get a new tax calculation."
    val incomeTaxReturnButtonText = "Back to Income Tax Return"
    val incomeTaxReturnButtonHref = s"/income-through-software/return/$taxYear/start"
  }

  object ExpectedResultsWelsh {
    val titleIndividual = "Your address has changed"
    val titleAgent = "Your client’s address has changed"
    val headingIndividual = "Your address has changed"
    val headingAgent = "Your client’s address has changed"
    val addressHasChangedTextIndividual = "Your address has changed to a country with a different rate of tax."
    val addressHasChangedTextAgent = "Your client’s address has changed to a country with a different rate of tax."
    val submitYourReturnIndividual = "You must submit your Income Tax Return again to get a new tax calculation."
    val submitYourReturnAgent = "You must submit your client’s Income Tax Return again to get a new tax calculation."
    val incomeTaxReturnButtonText = "Back to Income Tax Return"
    val incomeTaxReturnButtonHref = s"/income-through-software/return/$taxYear/start"
  }


  object Selectors {
    val headingSelector = "#main-content > div > div > header > h1"
    val addressHasChangedSelector = "#main-content > div > div > div.govuk-body > p:nth-child(1)"
    val submitYourReturnSelector = "#main-content > div > div > div.govuk-body > p:nth-child(2)"
    val incomeTaxReturnButtonSelector = "#main-content > div > div > form > button"
    val addressHasChangedPageView: AddressHasChangedPage = app.injector.instanceOf[AddressHasChangedPage]
  }

  import Selectors._

  private val urlPath = s"/income-through-software/return/$taxYear/address-changed"

  "Rendering the address change error page in English" should {

    import ExpectedResultsEnglish._

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
      formPostLinkCheck(incomeTaxReturnButtonHref, incomeTaxReturnButtonSelector)
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
      formPostLinkCheck(incomeTaxReturnButtonHref, incomeTaxReturnButtonSelector)
    }
  }

  "Rendering the address change error page in Welsh" should {

    import ExpectedResultsWelsh._

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
      formPostLinkCheck(incomeTaxReturnButtonHref, incomeTaxReturnButtonSelector)
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
      formPostLinkCheck(incomeTaxReturnButtonHref, incomeTaxReturnButtonSelector)
    }
  }

}