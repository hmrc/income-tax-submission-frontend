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
import play.api.http.{HeaderNames, Status}
import play.api.mvc.Result
import play.api.test.{FakeRequest, Helpers}
import play.api.test.Helpers.{status, writeableOf_AnyContentAsEmpty}

import scala.concurrent.Future

class ReturnTaxYearExistsControllerISpec extends IntegrationTest with ViewHelpers with Status {

  lazy val frontendAppConfig: AppConfig = app.injector.instanceOf[AppConfig]

  lazy val taxYear: Int = 2022
  lazy val lastTaxYear: Int = 2021

  object ExpectedResults {
    lazy val expectedTitleText: String = "We already have an Income Tax Return for that tax year"
    lazy val expectedHeadingText: String = "We already have an Income Tax Return for that tax year"
    lazy val expectedP1Text: String = s"We have an Income Tax Return for the $lastTaxYear to $taxYear tax year."
    lazy val expectedP2TextIndividual: String = "You can go to your Income Tax account to see your Income Tax Returns."
    lazy val expectedP2TextAgent: String = "You can go to your client’s Income Tax account to see their Income Tax Returns."
    lazy val expectedReturnToTaxAccountButtonText: String = "Back to Income Tax account"
    lazy val expectedReturnToTaxAccountButtonLink: String = s"http://localhost:9302/income-through-software/return/$taxYear/view"
    lazy val expectedSignOutButtonText: String = "Sign out"
    lazy val expectedSignOutButtonLinkIndividual: String = "/income-through-software/return/sign-out?isAgent=false"
    lazy val expectedSignOutButtonLinkAgent: String = "/income-through-software/return/sign-out?isAgent=true"
  }

  object ExpectedResultsWelsh {
    lazy val expectedTitleTextWelsh: String = "We already have an Income Tax Return for that tax year"
    lazy val expectedHeadingTextWelsh: String = "We already have an Income Tax Return for that tax year"
    lazy val expectedP1TextWelsh: String = s"We have an Income Tax Return for the $lastTaxYear to $taxYear tax year."
    lazy val expectedP2TextIndividualWelsh: String = "You can go to your Income Tax account to see your Income Tax Returns."
    lazy val expectedP2TextAgentWelsh: String = "You can go to your client’s Income Tax account to see their Income Tax Returns."
    lazy val expectedReturnToTaxAccountButtonTextWelsh: String = "Back to Income Tax account"
    lazy val expectedSignOutButtonTextWelsh: String = "Sign out"
  }

  object Selectors {
    lazy val headingSelector: String = "#main-content > div > div > header > h1"
    lazy val p1TextSelector: String = "#main-content > div > div > div.govuk-body > p:nth-child(1)"
    lazy val p2TextSelector: String = "#main-content > div > div > div.govuk-body > p:nth-child(2)"
    lazy val returnToTaxAccountButtonSelector: String = "#returnToOverviewPageBtn"
    lazy val signOutButtonSelector: String = "#signOutButton"
  }

  import ExpectedResults._
  import Selectors._

  val pageUrl = s"/income-through-software/return/$taxYear/already-have-income-tax-return"

  "when the language is set to ENGLISH, the page" should {
    val headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck")

    "as an individual user, with a previously submitted return to that tax year, the page" should {

      "return the page" which {

        val request = FakeRequest("GET", pageUrl).withHeaders(headers: _*)

        lazy val result: Future[Result] = {
          authoriseIndividual()
          route(app, request).get
        }

        implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

        "returns status of OK(200)" in {
          status(result) shouldBe OK
        }

        welshToggleCheck("English")
        titleCheck(expectedTitleText)
        h1Check(expectedHeadingText, "xl")
        textOnPageCheck(expectedP1Text, p1TextSelector)
        textOnPageCheck(expectedP2TextIndividual, p2TextSelector)
        buttonCheck(expectedReturnToTaxAccountButtonText, returnToTaxAccountButtonSelector, Some(expectedReturnToTaxAccountButtonLink))
        buttonCheck(expectedSignOutButtonText, signOutButtonSelector, Some(expectedSignOutButtonLinkIndividual))
      }
    }

    "as an agent user, with a previously submitted return to that tax year, the page" should {


      val request = FakeRequest("GET", pageUrl).withHeaders(headers: _*)

      lazy val result: Future[Result] = {
        authoriseAgent()
        route(app, request).get
      }

      implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

      "returns status of OK(200)" in {
        status(result) shouldBe OK
      }
      textOnPageCheck(expectedP2TextAgent, p2TextSelector)
      buttonCheck(expectedSignOutButtonText, signOutButtonSelector, Some(expectedSignOutButtonLinkAgent))
    }
  }


  "when the language is set to WELSH" should {
    import ExpectedResultsWelsh._

    val headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck", HeaderNames.ACCEPT_LANGUAGE -> "cy")

    "as an individual user, with a previously submitted return to that tax year, the page" should {
      "return the page" which {

        val request = FakeRequest("GET", pageUrl).withHeaders(headers: _*)

        lazy val result: Future[Result] = {
          authoriseIndividual()
          route(app, request).get
        }

        implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

        "returns status of OK(200)" in {
          status(result) shouldBe OK
        }

        welshToggleCheck("Welsh")
        titleCheck(expectedTitleTextWelsh)
        h1Check(expectedHeadingTextWelsh, "xl")
        textOnPageCheck(expectedP1TextWelsh, p1TextSelector)
        textOnPageCheck(expectedP2TextIndividualWelsh, p2TextSelector)
        buttonCheck(expectedReturnToTaxAccountButtonTextWelsh, returnToTaxAccountButtonSelector, Some(expectedReturnToTaxAccountButtonLink))
        buttonCheck(expectedSignOutButtonTextWelsh, signOutButtonSelector, Some(expectedSignOutButtonLinkIndividual))
      }
    }

    "as an agent user, with a previously submitted return to that tax year, the page" should {

      "return the page" which {
        val request = FakeRequest("GET", pageUrl).withHeaders(headers: _*)

        lazy val result: Future[Result] = {
          authoriseAgent()
          route(app, request).get
        }

        implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

        "returns status of OK(200)" in {
          status(result) shouldBe OK
        }

        welshToggleCheck("Welsh")
        textOnPageCheck(expectedP2TextAgentWelsh, p2TextSelector)
        buttonCheck(expectedSignOutButtonTextWelsh, signOutButtonSelector, Some(expectedSignOutButtonLinkAgent))
      }
    }

  }
}