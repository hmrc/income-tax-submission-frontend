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
import play.api.mvc.Result
import play.api.test.{FakeRequest, Helpers}
import play.api.test.Helpers._
import views.html.errors.BusinessValidationRulesView

import scala.concurrent.Future


class BusinessValidationRulesControllerISpec extends IntegrationTest with ViewHelpers {

  lazy val frontendAppConfig: AppConfig = app.injector.instanceOf[AppConfig]

  val taxYear: Int = 2021

  object Selectors {
    val headingSelector = "#main-content > div > div > header > h1"
    val checkYourIncomeTaxReturnSelector = "#main-content > div > div > p"
    val incomeTaxReturnButtonSelector = "#returnToOverviewPageBtn"
    val businessValidationRulesView: BusinessValidationRulesView = app.injector.instanceOf[BusinessValidationRulesView]
  }

  object ExpectedResults {
    val title = "There’s a problem with your updates"
    val heading = "There’s a problem with your updates"
    val checkYourIncomeTaxReturnAgent = "Check your client’s Income Tax Return and submit it again."
    val checkYourIncomeTaxReturnIndividual = "Check your Income Tax Return and submit it again."
    val incomeTaxReturnButtonText = "Back to Income Tax Return"
    val incomeTaxReturnButtonLink = s"http://localhost:9302/update-and-submit-income-tax-return/$taxYear/view"
  }

  object ExpectedResultsWelsh {
    val title = "Mae problem gyda’ch diweddariadau"
    val heading = "Mae problem gyda’ch diweddariadau"
    val checkYourIncomeTaxReturnAgent = "Ewch ati i wirio Ffurflen Dreth eich cleient ar gyfer Treth Incwm a’i chyflwyno eto."
    val checkYourIncomeTaxReturnIndividual = "Ewch ati i wirio’ch Ffurflen Dreth ar gyfer Treth Incwm a’i chyflwyno eto."
    val incomeTaxReturnButtonText = "Yn ôl i hafan Ffurflen Dreth Incwm"
    val incomeTaxReturnButtonLink = s"http://localhost:9302/update-and-submit-income-tax-return/$taxYear/view"
  }

  import Selectors._

  private def urlPath(taxYear: Int = taxYear) = s"/update-and-submit-income-tax-return/$taxYear/problem-with-updates"

  "Rendering the business validation rules page in English" should {

    import ExpectedResults._

    val headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck")

    "render correctly when the user is an individual" should {

      val request = FakeRequest("GET", urlPath()).withHeaders(headers: _*)

      lazy val result: Future[Result] = {
        authoriseIndividual()
        route(app, request).get
      }

      implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

      "returns a status of UNPROCESSABLE ENTITY(422)" in {
        status(result) shouldBe UNPROCESSABLE_ENTITY
      }

      welshToggleCheck("English")
      titleCheck(title, isWelsh = false)
      h1Check(heading, size = "xl")
      textOnPageCheck(checkYourIncomeTaxReturnIndividual, checkYourIncomeTaxReturnSelector)
      buttonCheck(incomeTaxReturnButtonText, incomeTaxReturnButtonSelector, Some(incomeTaxReturnButtonLink))
    }

    "render correctly when the user is an agent" should {

      val request = FakeRequest("GET", urlPath()).withHeaders(headers: _*)

      lazy val result: Future[Result] = {
        authoriseAgent()
        route(app, request).get
      }

      implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

      "return a status of UNPROCESSABLE ENTITY(422)" in {
        status(result) shouldBe UNPROCESSABLE_ENTITY
      }

      welshToggleCheck("English")
      titleCheck(title, isWelsh = false)
      h1Check(heading, size = "xl")
      textOnPageCheck(checkYourIncomeTaxReturnAgent, checkYourIncomeTaxReturnSelector)
      buttonCheck(incomeTaxReturnButtonText, incomeTaxReturnButtonSelector, Some(incomeTaxReturnButtonLink))
    }
  }

  "Rendering the business validation rules page in Welsh" should {
    import ExpectedResultsWelsh._

    val headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck", HeaderNames.ACCEPT_LANGUAGE -> "cy")

    "render correctly when the user is an individual" should {
      val request = FakeRequest("GET", urlPath()).withHeaders(headers: _*)

      lazy val result: Future[Result] = {
        authoriseIndividual()
        route(app, request).get
      }

      implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

      "return a status of UNPROCESSABLE ENTITY(422)" in {
        status(result) shouldBe UNPROCESSABLE_ENTITY
      }

      welshToggleCheck("Welsh")
      titleCheck(title, isWelsh = true)
      h1Check(heading, "xl")
      textOnPageCheck(checkYourIncomeTaxReturnIndividual, checkYourIncomeTaxReturnSelector)
      buttonCheck(incomeTaxReturnButtonText, incomeTaxReturnButtonSelector, Some(incomeTaxReturnButtonLink))
    }

    "render correctly when the user is an agent" should {
      val request = FakeRequest("GET", urlPath()).withHeaders(headers: _*)

      lazy val result: Future[Result] = {
        authoriseAgent()
        route(app, request).get
      }

      implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

      "returns a status of UNPROCESSABLE ENTITY(422)" in {
        status(result) shouldBe UNPROCESSABLE_ENTITY
      }

      welshToggleCheck("Welsh")
      titleCheck(title, isWelsh = true)
      h1Check(heading, "xl")
      textOnPageCheck(checkYourIncomeTaxReturnAgent, checkYourIncomeTaxReturnSelector)
      buttonCheck(incomeTaxReturnButtonText, incomeTaxReturnButtonSelector, Some(incomeTaxReturnButtonLink))
    }

  }

  "Attempting to render the business validation rules error page in year" should {

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


}
