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
import views.html.errors.NoUpdatesProvidedPage

import scala.concurrent.Future

class NoUpdatesProvidedPageControllerISpec extends IntegrationTest with ViewHelpers {

  lazy val frontendAppConfig: AppConfig = app.injector.instanceOf[AppConfig]

  val taxYear = 2022

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
    val incomeTaxReturnButtonText = "Back to Income Tax Return"
    val incomeTaxReturnButtonLink = s"http://localhost:9302/income-through-software/return/$taxYear/view"
  }

  object ExpectedResultsWelsh {
    val title = "No updates provided"
    val heading = "No updates provided"
    val youNeedToProvideTextIndividual = "You need to provide at least one update before you can submit your Income Tax Return."
    val youNeedToProvideTextAgent = "You need to provide at least one update before you can submit your client’s Income Tax Return."
    val incomeTaxReturnButtonText = "Back to Income Tax Return"
    val incomeTaxReturnButtonLink = s"http://localhost:9302/income-through-software/return/$taxYear/view"
  }

  import Selectors._

  private val urlPath = s"/income-through-software/return/$taxYear/no-updates-provided"

  "Rendering the no updates provided error page in English" should {
    import ExpectedResults._

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
      titleCheck(title)
      h1Check(heading, "xl")
      textOnPageCheck(youNeedToProvideTextIndividual, youNeedToProvideSelector)
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
      titleCheck(title)
      h1Check(heading, "xl")
      textOnPageCheck(youNeedToProvideTextAgent, youNeedToProvideSelector)
      textOnPageCheck(incomeTaxReturnButtonText, incomeTaxReturnButtonSelector)
      buttonCheck(incomeTaxReturnButtonText, incomeTaxReturnButtonSelector, Some(incomeTaxReturnButtonLink))
    }
  }

  "Rendering the no updates provided error page in Welsh" should {
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
      titleCheck(title)
      h1Check(heading, "xl")
      textOnPageCheck(youNeedToProvideTextIndividual, youNeedToProvideSelector)
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
      titleCheck(title)
      h1Check(heading, "xl")
      textOnPageCheck(youNeedToProvideTextAgent, youNeedToProvideSelector)
      textOnPageCheck(incomeTaxReturnButtonText, incomeTaxReturnButtonSelector)
      buttonCheck(incomeTaxReturnButtonText, incomeTaxReturnButtonSelector, Some(incomeTaxReturnButtonLink))
    }
  }

}
