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
import controllers.predicates.AuthorisedAction
import itUtils.{IntegrationTest, ViewHelpers}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.http.{HeaderNames, Status}
import play.api.libs.ws.WSResponse
import play.api.test.FakeRequest
import views.html.errors.ReturnTaxYearExistsView

class ReturnTaxYearExistsControllerISpec extends IntegrationTest with ViewHelpers with Status {

  lazy val frontendAppConfig: AppConfig = app.injector.instanceOf[AppConfig]

  def controller: ReturnTaxYearExistsController = new ReturnTaxYearExistsController(
    app.injector.instanceOf[AuthorisedAction],
    mcc,
    app.injector.instanceOf[ReturnTaxYearExistsView],
    frontendAppConfig
  )

  val taxYear: Int = 2022
  val lastTaxYear: Int = taxYear - 1

  object ExpectedResults {
    lazy val expectedTitleText: String = "We already have an Income Tax Return for that tax year"
    lazy val expectedHeadingText: String = "We already have an Income Tax Return for that tax year"
    lazy val expectedP1Text: String = s"We have an Income Tax Return for the $lastTaxYear to $taxYear tax year."
    lazy val expectedP2TextIndividual: String = "You can go to your Income Tax account to see your Income Tax Returns."
    lazy val expectedP2TextAgent: String = "You can go to your client’s Income Tax account to see their Income Tax Returns."
    lazy val expectedReturnToTaxAccountButtonText: String = "Back to Income Tax account"
    lazy val expectedReturnToTaxAccountButtonLink: String = s"http://localhost:9302/income-through-software/return/$taxYear/view"
    lazy val expectedSignOutButtonText: String = "Sign Out"
    lazy val expectedSignOutButtonLinkIndividual: String = "http://localhost:9302/income-through-software/return/sign-out?isAgent=false"
    lazy val expectedSignOutButtonLinkAgent: String = "http://localhost:9302/income-through-software/return/sign-out?isAgent=true"
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

  val pageUrl = s"http://localhost:$port/income-through-software/return/$taxYear/already-have-income-tax-return"

  "as an individual user, with a previously submitted return to that tax year, the page" should {

    val headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck")

    "when the language is set to ENGLISH" should {
      "return the page" which {

        val request = FakeRequest("GET", pageUrl).withHeaders(headers: _*)

        lazy val result: WSResponse = {
          authoriseIndividual()
          route(app, request).get
        }

        implicit def document: () => Document = () => Jsoup.parse(result.body)

        "returns status of OK(200)" in {
          result.status shouldBe OK
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

    "when the language is set to WELSH" should {
      "return a page" which {
        lazy val result: WSResponse = {
          authoriseIndividual()
          await(wsClient.url(pageUrl).withHttpHeaders(HeaderNames.ACCEPT_LANGUAGE -> "cy").get())
        }

        implicit def document: () => Document = () => Jsoup.parse(result.body)

        "returns status of UNAUTHORIZED(401)" in {
          result.status shouldBe UNAUTHORIZED
        }

        welshToggleCheck("Welsh")
        titleCheck(expectedTitleText)
        h1Check(expectedHeadingText, "xl")
        textOnPageCheck(expectedP1Text, p1TextSelector)
        textOnPageCheck(expectedP2TextIndividual, p2TextSelector)
        buttonCheck(expectedReturnToTaxAccountButtonText, returnToTaxAccountButtonSelector, Some(expectedReturnToTaxAccountButtonLink))
        buttonCheck(expectedSignOutButtonText, signOutButtonSelector, Some(expectedSignOutButtonLinkIndividual))
      }
    }
  }

  "as an agent user, with a previously submitted return to that tax year, the page" should {
    "when the language is set to ENGLISH" should {
      "return the page" which {
        lazy val result: WSResponse = {
          authoriseAgent()
          await(wsClient.url(pageUrl).get())
        }

        implicit def document: () => Document = () => Jsoup.parse(result.body)

        "returns status of UNAUTHORIZED(401)" in {
          result.status shouldBe UNAUTHORIZED
        }
        textOnPageCheck(expectedP2TextAgent, p2TextSelector)
        buttonCheck(expectedSignOutButtonText, signOutButtonSelector, Some(expectedSignOutButtonLinkAgent))
      }
    }

    "when the language is set to WELSH" should {
      "return a page" which {
        lazy val result: WSResponse = {
          authoriseAgent()
          await(wsClient.url(pageUrl).withHttpHeaders(HeaderNames.ACCEPT_LANGUAGE -> "cy").get())
        }

        implicit def document: () => Document = () => Jsoup.parse(result.body)

        "returns status of UNAUTHORIZED(401)" in {
          result.status shouldBe UNAUTHORIZED
        }

        welshToggleCheck("Welsh")
        titleCheck(expectedTitleText)
        h1Check(expectedHeadingText, "xl")
        textOnPageCheck(expectedP1Text, p1TextSelector)
        textOnPageCheck(expectedP2TextAgent, p2TextSelector)
        buttonCheck(expectedReturnToTaxAccountButtonText, returnToTaxAccountButtonSelector, Some(expectedReturnToTaxAccountButtonLink))
        buttonCheck(expectedSignOutButtonText, signOutButtonSelector, Some(expectedSignOutButtonLinkAgent))
      }
    }
  }

}
