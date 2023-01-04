/*
 * Copyright 2023 HM Revenue & Customs
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
import play.api.test.Helpers.{UNPROCESSABLE_ENTITY, status, writeableOf_AnyContentAsEmpty}
import play.api.test.{FakeRequest, Helpers}

import scala.concurrent.Future

class NoValidIncomeSourcesISpec extends IntegrationTest with ViewHelpers {

  lazy val frontendAppConfig: AppConfig = app.injector.instanceOf[AppConfig]

  object Selectors {
    val headingSelector = "#main-content > div > div > header > h1"
    val p1Selector = "#main-content > div > div > div.govuk-body > p:nth-child(1)"
    val p2Selector = "#main-content > div > div > div.govuk-body > p:nth-child(2)"
    val bul1Selector = "#main-content > div > div > div.govuk-body > ul > li:nth-child(1)"
    val bul2Selector = "#main-content > div > div > div.govuk-body > ul > li:nth-child(2)"
    val goToIncomeTaxAccountSelector = "#returnToOverviewPageBtn"
  }

  object ExpectedResults {
    val title = "No business income sources"
    val heading = "No business income sources"
    val p1Individual = "You need at least one source of business income in order to complete an Income Tax Return."
    val p1Agent = "Your client needs at least one source of business income in order to complete an Income Tax Return."
    val p2 = "Business income sources include:"
    val bul1 = "Self-employment"
    val bul2 = "UK or overseas property"
    val submitIndividual = "Back to your Income Tax Return"
    val submitAgent = "Back to Income Tax Return"

    val viewAndChangeOverviewLink = s"http://localhost:9081/report-quarterly/income-and-expenses/view$vcPtaNavBarOrigin"
    val viewAndChangeOverviewLinkAgent = s"http://localhost:9081/report-quarterly/income-and-expenses/view/agents"

  }

  object ExpectedResultsWelsh {
    val title = "Dim ffynonellau incwm busnes"
    val heading = "Dim ffynonellau incwm busnes"
    val p1Individual = "Rhaid i chi gael o leiaf un ffynhonnell o incwm busnes i lenwi Ffurflen Dreth Incwm."
    val p1Agent = "Mae angen o leiaf un ffynhonnell incwm busnes ar eich cleient er mwyn llenwi Ffurflen Dreth ar gyfer Treth Incwm."
    val p2 = "Mae ffynonellau incwm busnes yn cynnwys:"
    val bul1 = "Hunangyflogaeth"
    val bul2 = "Eiddo yn y DU neu dramor"
    val submitIndividual = "Yn ôl i’ch Ffurflen Dreth ar gyfer Treth Incwm"
    val submitAgent = "Yn ôl i’r Ffurflen Dreth ar gyfer Treth Incwm"

    val viewAndChangeOverviewLink = s"http://localhost:9081/report-quarterly/income-and-expenses/view$vcPtaNavBarOrigin"
    val viewAndChangeOverviewLinkAgent = s"http://localhost:9081/report-quarterly/income-and-expenses/view/agents"

  }

  import ExpectedResults._
  import Selectors._

  private def urlPath(taxYear: Int = taxYearEOY) = s"/update-and-submit-income-tax-return/$taxYear/no-business-income"

  "Rendering the No Valid Income Source error page in English" should {

    val headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYearEOY, validTaxYearList), "Csrf-Token" -> "nocheck")

    "render correctly when the user is an individual" should {
      val request = FakeRequest("GET", urlPath()).withHeaders(headers: _*)

      lazy val result: Future[Result] = {
        authoriseIndividual()
        route(app, request).get
      }

      implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

      "returns status of UNPROCESSABLE_ENTITY(422)" in {
        status(result) shouldBe UNPROCESSABLE_ENTITY
      }

      welshToggleCheck("English")
      titleCheck(title, isWelsh = false)
      h1Check(heading, "xl")
      textOnPageCheck(p1Individual, p1Selector)
      textOnPageCheck(p2, p2Selector)
      textOnPageCheck(bul1, bul1Selector)
      textOnPageCheck(bul2, bul2Selector)
      buttonCheck(submitIndividual, goToIncomeTaxAccountSelector, Some(viewAndChangeOverviewLink))
    }
  }

  "Rendering the No Valid Income Source error page in English as an Agent" should {

    val headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYearEOY, validTaxYearList), "Csrf-Token" -> "nocheck")

    "render the button with the correct url" should {
      val request = FakeRequest("GET", urlPath()).withHeaders(headers: _*)

      lazy val result: Future[Result] = {
        authoriseAgent()
        route(app, request).get
      }

      implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

      "returns status of UNPROCESSABLE_ENTITY(422)" in {
        status(result) shouldBe UNPROCESSABLE_ENTITY
      }

      welshToggleCheck("English")
      titleCheck(title, isWelsh = false)
      h1Check(heading, "xl")
      textOnPageCheck(p1Agent, p1Selector)
      textOnPageCheck(p2, p2Selector)
      textOnPageCheck(bul1, bul1Selector)
      textOnPageCheck(bul2, bul2Selector)
      buttonCheck(submitAgent, goToIncomeTaxAccountSelector, Some(viewAndChangeOverviewLinkAgent))
    }
  }
  
  "Attempting to Render the No Valid Income Source error page in year" should {

    val headers = Seq(HeaderNames.COOKIE -> playSessionCookies(frontendAppConfig.defaultTaxYear, validTaxYearList), "Csrf-Token" -> "nocheck")

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

  "Rendering the tax return No Valid Income Source error page in Welsh" should {
    import ExpectedResultsWelsh._
    val headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYearEOY, validTaxYearList), "Csrf-Token" -> "nocheck", HeaderNames.ACCEPT_LANGUAGE -> "cy")

    "render correctly when the user is an individual" should {
      val request = FakeRequest("GET", urlPath()).withHeaders(headers: _*)

      lazy val result: Future[Result] = {
        authoriseIndividual()
        route(app, request).get
      }

      implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

      "returns status of UNPROCESSABLE_ENTITY(422)" in {
        status(result) shouldBe UNPROCESSABLE_ENTITY
      }

      welshToggleCheck("Welsh")
      titleCheck(title, isWelsh = true)
      h1Check(heading, "xl")
      textOnPageCheck(p1Individual, p1Selector)
      textOnPageCheck(p2, p2Selector)
      textOnPageCheck(bul1, bul1Selector)
      textOnPageCheck(bul2, bul2Selector)
      buttonCheck(submitIndividual, goToIncomeTaxAccountSelector, Some(viewAndChangeOverviewLink))
    }

    "render correctly when the user is an agent" should {
      val request = FakeRequest("GET", urlPath()).withHeaders(headers: _*)

      lazy val result: Future[Result] = {
        authoriseAgent()
        route(app, request).get
      }

      implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

      "returns status of UNPROCESSABLE_ENTITY(422)" in {
        status(result) shouldBe UNPROCESSABLE_ENTITY
      }

      welshToggleCheck("Welsh")
      titleCheck(title, isWelsh = true)
      h1Check(heading, "xl")
      textOnPageCheck(p1Agent, p1Selector)
      textOnPageCheck(p2, p2Selector)
      textOnPageCheck(bul1, bul1Selector)
      textOnPageCheck(bul2, bul2Selector)
      buttonCheck(submitAgent, goToIncomeTaxAccountSelector, Some(viewAndChangeOverviewLinkAgent))
    }
  }

}
