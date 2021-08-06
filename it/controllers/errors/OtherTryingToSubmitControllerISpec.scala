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

import scala.concurrent.Future

class OtherTryingToSubmitControllerISpec extends IntegrationTest with ViewHelpers {

  lazy val frontendAppConfig: AppConfig = app.injector.instanceOf[AppConfig]

  val taxYear = 2022

  object Selectors {
    val headingSelector = "#main-content > div > div > header > h1"
    val p1TextSelectorAgent = "#main-content > div > div > div:nth-child(2) > p"
    val bullet1SelectorAgent = "#main-content > div > div > ul > li:nth-child(1)"
    val bullet2SelectorAgent = "#main-content > div > div > ul > li:nth-child(2)"
    val p2TextSelectorAgent = "#main-content > div > div > div:nth-child(4) > p"
    val p2TextSelectorIndividual = "#main-content > div > div > div.govuk-body > p"
    val incomeTaxReturnButtonSelector = "#returnToOverviewPageBtn"
  }

  object ExpectedResults {
    val titleIndividual = "Your agent is trying to submit this Income Tax Return"
    val titleAgent = "Someone else is trying to submit this Income Tax Return"
    val headingIndividual = "Your agent is trying to submit this Income Tax Return"
    val headingAgent = "Someone else is trying to submit this Income Tax Return"
    val expectedP1TextAgent = "This could be:"
    val expectedBullet1TextAgent = "your client"
    val expectedBullet2TextAgent = "a work colleague"
    val expectedP2TextIndividual = "You must go back to your Income Tax Return and submit it again."
    val expectedP2TextAgent = "You must go back to your client’s Income Tax Return and submit it again."
    val incomeTaxReturnButtonText = "Back to Income Tax Return"
    val incomeTaxReturnButtonLink = s"http://localhost:9302/income-through-software/return/$taxYear/view"
  }

  object ExpectedResultsWelsh {
    val titleIndividualWelsh = "Your agent is trying to submit this Income Tax Return"
    val titleAgentWelsh = "Someone else is trying to submit this Income Tax Return"
    val headingIndividualWelsh = "Your agent is trying to submit this Income Tax Return"
    val headingAgentWelsh = "Someone else is trying to submit this Income Tax Return"
    val expectedP1TextAgentWelsh = "This could be:"
    val expectedBullet1TextAgentWelsh = "your client"
    val expectedBullet2TextAgentWelsh = "a work colleague"
    val expectedP2TextIndividualWelsh = "You must go back to your Income Tax Return and submit it again."
    val expectedP2TextAgentWelsh = "You must go back to your client’s Income Tax Return and submit it again."
    val incomeTaxReturnButtonTextWelsh = "Back to Income Tax Return"
  }

  import ExpectedResults._
  import Selectors._

  private val urlPath = s"/income-through-software/return/$taxYear/someone-else-submitting-income-tax-return"

  "Rendering the Some else is trying to submit page in English" should {

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
      textOnPageCheck(expectedP2TextIndividual, p2TextSelectorIndividual)
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
      h1Check(headingAgent, "l")
      textOnPageCheck(expectedP1TextAgent, p1TextSelectorAgent)
      textOnPageCheck(expectedBullet1TextAgent, bullet1SelectorAgent)
      textOnPageCheck(expectedBullet2TextAgent, bullet2SelectorAgent)
      textOnPageCheck(expectedP2TextAgent, p2TextSelectorAgent)
      textOnPageCheck(incomeTaxReturnButtonText, incomeTaxReturnButtonSelector)
      buttonCheck(incomeTaxReturnButtonText, incomeTaxReturnButtonSelector, Some(incomeTaxReturnButtonLink))
    }
  }

  "Rendering the tax return previously updated page in Welsh" should {
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
      titleCheck(titleIndividualWelsh)
      h1Check(headingIndividualWelsh, "xl")
      textOnPageCheck(expectedP2TextIndividualWelsh, p2TextSelectorIndividual)
      textOnPageCheck(incomeTaxReturnButtonTextWelsh, incomeTaxReturnButtonSelector)
      buttonCheck(incomeTaxReturnButtonTextWelsh, incomeTaxReturnButtonSelector, Some(incomeTaxReturnButtonLink))
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
      titleCheck(titleAgentWelsh)
      h1Check(headingAgentWelsh, "l")
      textOnPageCheck(expectedP1TextAgentWelsh, p1TextSelectorAgent)
      textOnPageCheck(expectedBullet1TextAgentWelsh, bullet1SelectorAgent)
      textOnPageCheck(expectedBullet2TextAgentWelsh, bullet2SelectorAgent)
      textOnPageCheck(expectedP2TextAgentWelsh, p2TextSelectorAgent)
      textOnPageCheck(incomeTaxReturnButtonTextWelsh, incomeTaxReturnButtonSelector)
      buttonCheck(incomeTaxReturnButtonTextWelsh, incomeTaxReturnButtonSelector, Some(incomeTaxReturnButtonLink))
    }
  }

}
