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
import play.api.test.Helpers.{FORBIDDEN, status, writeableOf_AnyContentAsEmpty}
import play.api.test.{FakeRequest, Helpers}
import views.html.errors.NoUpdatesProvidedPage

import scala.concurrent.Future

class NoUpdatesProvidedPageControllerISpec extends IntegrationTest with ViewHelpers {

  lazy val frontendAppConfig: AppConfig = app.injector.instanceOf[AppConfig]

  object Selectors {
    val headingSelector = "#main-content > div > div > header > h1"
    val youNeedToProvideSelector = "#main-content > div > div > div.govuk-body > p:nth-child(1)"
    val incomeTaxReturnButtonSelector = "#returnToOverviewPageBtn"
    val noUpdatesProvidedPageView: NoUpdatesProvidedPage = app.injector.instanceOf[NoUpdatesProvidedPage]
  }

  object ExpectedResults {
    val title = "No updates provided"
    val heading = "No updates provided"
    val youNeedToProvideTextIndividual = "You need to provide at least one update before you can submit your Income Tax Return."
    val youNeedToProvideTextAgent = "You need to provide at least one update before you can submit your client’s Income Tax Return."
    val incomeTaxReturnButtonTextIndividual = "Back to your Income Tax Return"
    val incomeTaxReturnButtonTextAgent = "Back to Income Tax Return"
    val incomeTaxReturnButtonLink = s"http://localhost:9302/update-and-submit-income-tax-return/$taxYearEOY/view"
  }

  object ExpectedResultsWelsh {
    val title = "Dim diweddariad wedi’i roi"
    val heading = "Dim diweddariad wedi’i roi"
    val youNeedToProvideTextIndividual = "Mae angen i chi roi o leiaf un diweddariad cyn i chi allu cyflwyno eich Ffurflen Dreth Incwm."
    val youNeedToProvideTextAgent = "Mae angen i chi roi o leiaf un diweddariad cyn i chi allu cyflwyno Ffurflen Dreth Incwm eich cleient."
    val incomeTaxReturnButtonTextIndividual = "Yn ôl i’ch Ffurflen Dreth ar gyfer Treth Incwm"
    val incomeTaxReturnButtonTextAgent = "Yn ôl i’r Ffurflen Dreth ar gyfer Treth Incwm"
    val incomeTaxReturnButtonLink = s"http://localhost:9302/update-and-submit-income-tax-return/$taxYearEOY/view"
  }

  import Selectors._

  private def urlPath(taxYear: Int = taxYearEOY) = s"/update-and-submit-income-tax-return/$taxYear/no-updates-provided"

  "Rendering the no updates provided error page in English" should {
    import ExpectedResults._

    val headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYearEOY, validTaxYearList), "Csrf-Token" -> "nocheck")

    "render correctly when the user is an individual" should {
      val request = FakeRequest("GET", urlPath()).withHeaders(headers: _*)

      lazy val result: Future[Result] = {
        authoriseIndividual()
        route(app, request).get
      }

      implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

      "returns status of FORBIDDEN(200)" in {
        status(result) shouldBe FORBIDDEN
      }

      welshToggleCheck("English")
      titleCheck(title, isWelsh = false)
      h1Check(heading, "xl")
      textOnPageCheck(youNeedToProvideTextIndividual, youNeedToProvideSelector)
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

      "returns status of FORBIDDEN(200)" in {
        status(result) shouldBe FORBIDDEN
      }

      welshToggleCheck("English")
      titleCheck(title, isWelsh = false)
      h1Check(heading, "xl")
      textOnPageCheck(youNeedToProvideTextAgent, youNeedToProvideSelector)
      textOnPageCheck(incomeTaxReturnButtonTextAgent, incomeTaxReturnButtonSelector)
      buttonCheck(incomeTaxReturnButtonTextAgent, incomeTaxReturnButtonSelector, Some(incomeTaxReturnButtonLink))
    }
  }
  "Attempting to Render the no updates provided error page in year" should {

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

  "Rendering the no updates provided error page in Welsh" should {
    import ExpectedResultsWelsh._

    val headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYearEOY, validTaxYearList), "Csrf-Token" -> "nocheck", HeaderNames.ACCEPT_LANGUAGE -> "cy")

    "render correctly when the user is an individual" should {
      val request = FakeRequest("GET", urlPath()).withHeaders(headers: _*)

      lazy val result: Future[Result] = {
        authoriseIndividual()
        route(app, request).get
      }

      implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

      "returns status of FORBIDDEN(403)" in {
        status(result) shouldBe FORBIDDEN
      }

      welshToggleCheck("Welsh")
      titleCheck(title, isWelsh = true)
      h1Check(heading, "xl")
      textOnPageCheck(youNeedToProvideTextIndividual, youNeedToProvideSelector)
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

      "returns status of FORBIDDEN(403)" in {
        status(result) shouldBe FORBIDDEN
      }

      welshToggleCheck("Welsh")
      titleCheck(title, isWelsh = true)
      h1Check(heading, "xl")
      textOnPageCheck(youNeedToProvideTextAgent, youNeedToProvideSelector)
      textOnPageCheck(incomeTaxReturnButtonTextAgent, incomeTaxReturnButtonSelector)
      buttonCheck(incomeTaxReturnButtonTextAgent, incomeTaxReturnButtonSelector, Some(incomeTaxReturnButtonLink))
    }
  }

}
