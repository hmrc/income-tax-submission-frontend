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
import play.api.test.Helpers.{UNPROCESSABLE_ENTITY, status, writeableOf_AnyContentAsEmpty}
import play.api.test.{FakeRequest, Helpers}

import scala.concurrent.Future

class NoValidIncomeSourcesISpec extends IntegrationTest with ViewHelpers {

  lazy val frontendAppConfig: AppConfig = app.injector.instanceOf[AppConfig]

  val taxYear = 2021

  object Selectors {
    val headingSelector = "#main-content > div > div > header > h1"
    val p1Selector = "#main-content > div > div > div.govuk-body > p:nth-child(1)"
    val p2Selector = "#main-content > div > div > div.govuk-body > p:nth-child(2)"
    val bul1Selector = "#main-content > div > div > div.govuk-body > ul > li:nth-child(1)"
    val bul2Selector = "#main-content > div > div > div.govuk-body > ul > li:nth-child(2)"
    val goToIncomeTaxAccountSelector = "#returnToOverviewPageBtn"
    val signOutButtonSelector = "#signOutBtn"
  }

  object ExpectedResults {
    val title = "No business income sources"
    val heading = "No business income sources"
    val p1 = "You need at least one source of business income in order to complete an Income Tax Return."
    val p2 = "Business income sources include:"
    val bul1 = "Self-employment"
    val bul2 = "UK or overseas property"
    val submit = "Go to your Income Tax account"
    val signOut = "Sign out"
    
    val viewAndChangeOverviewLink = s"http://localhost:9081/report-quarterly/income-and-expenses/view"
    val viewAndChangeOverviewLinkAgent = s"http://localhost:9081/report-quarterly/income-and-expenses/view/agents/income-tax-account"
    
    val signOutLink = "/income-through-software/return/sign-out?isAgent=false"
    val signOutLinkAgent = "/income-through-software/return/sign-out?isAgent=true"
  }

  object ExpectedResultsWelsh {
    val title = "Dim ffynonellau incwm busnes"
    val heading = "Dim ffynonellau incwm busnes"
    val p1 = "Rhaid i chi gael o leiaf un ffynhonnell o incwm busnes i lenwi Ffurflen Dreth Incwm."
    val p2 = "Mae ffynonellau incwm busnes yn cynnwys:"
    val bul1 = "Hunangyflogaeth"
    val bul2 = "Eiddo yn y DU neu dramor"
    val submit = "Ewch i’ch cyfrif Treth Incwm"

    val incomeTaxReturnButtonLink = s"http://localhost:9081/report-quarterly/income-and-expenses/view"
  }

  import ExpectedResults._
  import Selectors._

  private def urlPath(taxYear: Int = taxYear) = s"/income-through-software/return/$taxYear/no-business-income"

  "Rendering the No Valid Income Source error page in English" should {

    val headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck")

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
      textOnPageCheck(p1, p1Selector)
      textOnPageCheck(p2, p2Selector)
      textOnPageCheck(bul1, bul1Selector)
      textOnPageCheck(bul2, bul2Selector)
      buttonCheck(submit, goToIncomeTaxAccountSelector, Some(viewAndChangeOverviewLink))
      buttonCheck(signOut, signOutButtonSelector, Some(signOutLink))
    }
  }

  "Rendering the No Valid Income Source error page in English as an Agent" should {

    val headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck")

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
      
      buttonCheck(submit, goToIncomeTaxAccountSelector, Some(viewAndChangeOverviewLinkAgent))
      buttonCheck(signOut, signOutButtonSelector, Some(signOutLinkAgent))
    }
  }
  
  "Attempting to Render the No Valid Income Source error page in year" should {

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

  "Rendering the tax return No Valid Income Source error page in Welsh" should {
    import ExpectedResultsWelsh._
    val headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck", HeaderNames.ACCEPT_LANGUAGE -> "cy")

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
      textOnPageCheck(p1, p1Selector)
      textOnPageCheck(p2, p2Selector)
      textOnPageCheck(bul1, bul1Selector)
      textOnPageCheck(bul2, bul2Selector)
      buttonCheck(submit, goToIncomeTaxAccountSelector, Some(incomeTaxReturnButtonLink))
    }
  }

}
