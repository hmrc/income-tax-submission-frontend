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

class TaxReturnPreviouslyUpdatedControllerISpec extends IntegrationTest with ViewHelpers {

  lazy val frontendAppConfig: AppConfig = app.injector.instanceOf[AppConfig]

  val taxYear = 2021

  object Selectors {
    val headingSelector = "#main-content > div > div > header > h1"
    val p1TextSelector = "#main-content > div > div > div.govuk-body > p:nth-child(1)"
    val incomeTaxReturnButtonSelector = "#returnToOverviewPageBtn"
  }

  object ExpectedResults {
    val titleIndividual = "Your Income Tax Return has been updated"
    val titleAgent = "Your client’s Income Tax Return has been updated"
    val headingIndividual = "Your Income Tax Return has been updated"
    val headingAgent = "Your client’s Income Tax Return has been updated"
    val youMustResubmitTextIndividual = "You must submit your Income Tax Return again."
    val youMustResubmitTextAgent = "You must submit your client’s Income Tax Return again."
    val incomeTaxReturnButtonText = "Back to Income Tax Return"
    val incomeTaxReturnButtonLink = s"http://localhost:9302/income-through-software/return/$taxYear/view"
  }

  object ExpectedResultsWelsh {
    val titleIndividualWelsh = "Your Income Tax Return has been updated"
    val titleAgentWelsh = "Your client’s Income Tax Return has been updated"
    val headingIndividualWelsh = "Your Income Tax Return has been updated"
    val headingAgentWelsh = "Your client’s Income Tax Return has been updated"
    val youMustResubmitTextIndividualWelsh = "You must submit your Income Tax Return again."
    val youMustResubmitTextAgentWelsh = "You must submit your client’s Income Tax Return again."
    val incomeTaxReturnButtonTextWelsh = "Back to Income Tax Return"
  }

  import ExpectedResults._
  import Selectors._

  private def urlPath(taxYear: Int = taxYear) = s"/income-through-software/return/$taxYear/income-tax-return-updated"

  "Rendering the tax return previously updated page in English" should {

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
      textOnPageCheck(youMustResubmitTextIndividual, p1TextSelector)
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
      textOnPageCheck(youMustResubmitTextAgent, p1TextSelector)
      textOnPageCheck(incomeTaxReturnButtonText, incomeTaxReturnButtonSelector)
      buttonCheck(incomeTaxReturnButtonText, incomeTaxReturnButtonSelector, Some(incomeTaxReturnButtonLink))
    }
  }
  "Attempting to Render the tax return previously error page in year" should {

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

  "Rendering the tax return previously updated page in Welsh" should {
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
      textOnPageCheck(youMustResubmitTextIndividualWelsh, p1TextSelector)
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
      textOnPageCheck(youMustResubmitTextAgentWelsh, p1TextSelector)
      textOnPageCheck(incomeTaxReturnButtonTextWelsh, incomeTaxReturnButtonSelector)
      buttonCheck(incomeTaxReturnButtonTextWelsh, incomeTaxReturnButtonSelector, Some(incomeTaxReturnButtonLink))
    }
  }

}
