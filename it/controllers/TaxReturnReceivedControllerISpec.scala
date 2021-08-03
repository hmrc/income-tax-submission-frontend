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

import common.SessionValues
import config.AppConfig
import controllers.predicates.AuthorisedAction
import helpers.PlaySessionCookieBaker
import itUtils.{IntegrationTest, ViewHelpers}
import models.TaxReturnReceivedModel
import org.joda.time.{DateTimeZone, LocalDate}
import org.joda.time.format.DateTimeFormat
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.http.HeaderNames
import play.api.mvc.Result
import play.api.test.{FakeRequest, Helpers}
import play.api.test.Helpers.{OK, SEE_OTHER, status, writeableOf_AnyContentAsEmpty}
import views.html.TaxReturnReceivedView

import scala.concurrent.Future

class TaxReturnReceivedControllerISpec extends IntegrationTest with ViewHelpers {

  def controller: TaxReturnReceivedController = new TaxReturnReceivedController(
    app.injector.instanceOf[AuthorisedAction],
    app.injector.instanceOf[TaxReturnReceivedView],
    frontendAppConfig,
    mcc,
    scala.concurrent.ExecutionContext.Implicits.global
  )

  val timeStamp: String = DateTimeFormat.forPattern("d MMMM YYYY").print(LocalDate.now(DateTimeZone.forID("GMT")))
  val nino: String = "AA012345A"
  val mtditid: String = "1234567890"

  object ExpectedResults {
    val taxYear: Int = 2022
    val taxYearMinusOne: Int = taxYear - 1
    val panelSubheading: String = taxYearMinusOne + " to " + taxYear
    val summaryRow1Key: String = "Name"
    val summaryRow2Key: String = "Unique Tax Reference (UTR)"
    val summaryRow3Key: String = "Date submitted"
    val summaryRow4Key: String = "Income Tax and National Insurance contributions"
    val nextStepsSubheading: String = "What you need to do next"
    val nextStepsP3: String = "You can also:"
    val nextStepsPrint: String = "Print this page"
    val printLink: String = ""

    val panelSubheadingWelsh: String = taxYearMinusOne + " to " + taxYear
    val summaryRow1KeyWelsh: String = "Name"
    val summaryRow2KeyWelsh: String = "Unique Tax Reference (UTR)"
    val summaryRow3KeyWelsh: String = "Date submitted"
    val summaryRow4KeyWelsh: String = "Income Tax and National Insurance contributions"
    val nextStepsSubheadingWelsh: String = "What you need to do next"
    val nextStepsP3Welsh: String = "You can also:"
    val nextStepsPrintWelsh: String = "Print this page"

    val expectedIncomeTaxSubmissionFrontendOverviewUrl: String = s"/income-through-software/return/$taxYear/view"
  }

  object IndividualExpectedResults {

    val individualSummaryData: TaxReturnReceivedModel = TaxReturnReceivedModel(
      "John Individual", 1000, "IN12345", 321, 312, 123
    )

    val summaryRow1Value: String = "John Individual"
    val summaryRow2Value: String = "IN12345"
    val summaryRow3Value: String = timeStamp
    val summaryRow4Value: String = "£1000"

    val panelHeading: String = "We’ve received your Income Tax Return for:"

    val nextStepsP1: String = "Find out what you owe and how to pay."
    val nextStepsP2: String = "what you owe and how to pay."
    val nextStepsP2Link: String = "http://localhost:9081/report-quarterly/income-and-expenses/view/payments-owed"

    val nextStepsP4: String = "If you need to contact us about your Income Tax Return, tell us your UTR."

    val nextStepsBullet1: String = "find out when your next updates are due"
    val nextStepsBullet1Link: String = "http://localhost:9081/report-quarterly/income-and-expenses/view/obligations"

    val nextStepsBullet2: String = "view your final Income Tax and National Insurance calculation"
    val nextStepsBullet2Link: String = "http://localhost:9081/report-quarterly/income-and-expenses/view/tax-years"

    val panelHeadingWelsh: String = "We’ve received your Income Tax Return for:"
    val summaryRow1ValueWelsh: String = "John Individual"
    val summaryRow2ValueWelsh: String = "IN12345"
    val summaryRow3ValueWelsh: String = timeStamp
    val summaryRow4ValueWelsh: String = "£1000"

    val nextStepsP1Welsh: String = "Find out what you owe and how to pay."
    val nextStepsP2Welsh: String = "what you owe and how to pay."
    val nextStepsP4Welsh: String = "If you need to contact us about your Income Tax Return, tell us your UTR."

    val nextStepsBullet1Welsh: String = "find out when your next updates are due"
    val nextStepsBullet2Welsh: String = "view your final Income Tax and National Insurance calculation"
  }

  object AgentExpectedResults {

    val agentSummaryData: TaxReturnReceivedModel = TaxReturnReceivedModel(
      "Jane Agent", 750.50, "AG98765", 321, 312, 123
    )

    val summaryRow1Value: String = "Jane Agent"
    val summaryRow2Value: String = "AG98765"
    val summaryRow3Value: String = timeStamp
    val summaryRow4Value: String = "£750.50"

    val panelHeading = "We’ve received your client’s Income Tax Return for:"

    val nextStepsP1: String = "Find out what your client owes."
    val nextStepsP2: String = "what your client owes."
    val nextStepsP2Link: String = "http://localhost:9081/report-quarterly/income-and-expenses/view/agents/payments-owed"

    val nextStepsP4: String = "If you need to contact us about your client’s Income Tax Return, tell us their UTR."

    val nextStepsBullet1: String = "find out when your client’s next updates are due"
    val nextStepsBullet1Link: String = "http://localhost:9081/report-quarterly/income-and-expenses/view/agents/obligations"

    val nextStepsBullet2: String = "view your client’s final Income Tax and National Insurance calculation"
    val nextStepsBullet2Link: String = "http://localhost:9081/report-quarterly/income-and-expenses/view/agents/tax-years"

    val panelHeadingWelsh = "We’ve received your client’s Income Tax Return for:"

    val summaryRow1ValueWelsh: String = "Jane Agent"
    val summaryRow2ValueWelsh: String = "AG98765"
    val summaryRow3ValueWelsh: String = timeStamp
    val summaryRow4ValueWelsh: String = "£750.50"

    val nextStepsP1Welsh: String = "Find out what your client owes."
    val nextStepsP2Welsh: String = "what your client owes."
    val nextStepsP4Welsh: String = "If you need to contact us about your client’s Income Tax Return, tell us their UTR."

    val nextStepsBullet1Welsh: String = "find out when your client’s next updates are due"
    val nextStepsBullet2Welsh: String = "view your client’s final Income Tax and National Insurance calculation"
  }

  object Selectors {
    val panelHeadingSelector = "#main-content > div > div > div.govuk-panel.govuk-panel--confirmation > h1"
    val panelSubheadingSelector = "#main-content > div > div > div.govuk-panel.govuk-panel--confirmation > div"
    val summaryKey1Selector = "#main-content > div > div > dl > div:nth-child(1) > dt"
    val summaryValue1Selector = "#main-content > div > div > dl > div:nth-child(1) > dd"
    val summaryKey2Selector = "#main-content > div > div > dl > div:nth-child(2) > dt"
    val summaryValue2Selector = "#main-content > div > div > dl > div:nth-child(2) > dd"
    val summaryKey3Selector = "#main-content > div > div > dl > div:nth-child(3) > dt"
    val summaryValue3Selector = "#main-content > div > div > dl > div:nth-child(3) > dd"
    val summaryKey4Selector = "#main-content > div > div > dl > div:nth-child(4) > dt"
    val summaryValue4Selector = "#main-content > div > div > dl > div:nth-child(4) > dd"
    val nextStepsSubheadingSelector = "#main-content > div > div > h2"
    val nextStepsP1TextSelector = "#main-content > div > div > div:nth-child(4) > p"
    val nextStepsP2LinkSelector = "#amount_owed_link"
    val nextStepsP3TextSelector = "#main-content > div > div > div:nth-child(5) > p"
    val nextStepsBullet1Selector = "#main-content > div > div > ul > li:nth-child(1)"
    val nextStepsBullet1LinkSelector = "#main-content > div > div > ul > li:nth-child(1) > a"
    val nextStepsBullet2Selector = "#main-content > div > div > ul > li:nth-child(2)"
    val nextStepsBullet2LinkSelector = "#main-content > div > div > ul > li:nth-child(2) > a"
    val nextStepsP4TextSelector = "#main-content > div > div > div:nth-child(7) > p"
    val printLinkSelector = "#print_link"
  }

  import Selectors._
  import ExpectedResults._

  private val urlPath = s"/income-through-software/return/$taxYear/income-tax-return-received"

  lazy val frontendAppConfig: AppConfig = app.injector.instanceOf[AppConfig]

  "As an individual, the page should correctly render" when {
    import IndividualExpectedResults._

    "the language is specified as English and" should {
      lazy val playSessionCookies = PlaySessionCookieBaker.bakeSessionCookie(Map(
        SessionValues.SUMMARY_DATA -> individualSummaryData.asJsonString,
        SessionValues.TAX_YEAR -> taxYear.toString
      ))

      val headers = Seq(HeaderNames.COOKIE -> playSessionCookies, "Csrf-Token" -> "nocheck")

      "display the user's tax return submission confirmation" which {
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
        textOnPageCheck(panelHeading, panelHeadingSelector)
        textOnPageCheck(panelSubheading, panelSubheadingSelector)

        textOnPageCheck(summaryRow1Key, summaryKey1Selector)
        textOnPageCheck(summaryRow1Value, summaryValue1Selector)
        textOnPageCheck(summaryRow2Key, summaryKey2Selector)
        textOnPageCheck(summaryRow2Value, summaryValue2Selector)
        textOnPageCheck(summaryRow3Key, summaryKey3Selector)
        textOnPageCheck(summaryRow3Value, summaryValue3Selector)
        textOnPageCheck(summaryRow4Key, summaryKey4Selector)
        textOnPageCheck(summaryRow4Value, summaryValue4Selector)

        textOnPageCheck(nextStepsSubheading, nextStepsSubheadingSelector)
        textOnPageCheck(nextStepsP1, nextStepsP1TextSelector)
        linkCheck(nextStepsP2, nextStepsP2LinkSelector, nextStepsP2Link)
        textOnPageCheck(nextStepsP3, nextStepsP3TextSelector)
        linkCheck(nextStepsBullet1, nextStepsBullet1LinkSelector, nextStepsBullet1Link)
        linkCheck(nextStepsBullet2, nextStepsBullet2LinkSelector, nextStepsBullet2Link)
        textOnPageCheck(nextStepsP4, nextStepsP4TextSelector)
        linkCheck(nextStepsPrint, printLinkSelector, printLink)
      }

    }

    "the language is specified as Welsh and" should {
      lazy val playSessionCookies = PlaySessionCookieBaker.bakeSessionCookie(Map(
        SessionValues.SUMMARY_DATA -> individualSummaryData.asJsonString,
        SessionValues.TAX_YEAR -> taxYear.toString
      ))

      val headers = Seq(HeaderNames.COOKIE -> playSessionCookies, "Csrf-Token" -> "nocheck", HeaderNames.ACCEPT_LANGUAGE -> "cy")

      "display the user's tax return submission confirmation" which {
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
        textOnPageCheck(panelHeadingWelsh, panelHeadingSelector)
        textOnPageCheck(panelSubheadingWelsh, panelSubheadingSelector)

        textOnPageCheck(summaryRow1KeyWelsh, summaryKey1Selector)
        textOnPageCheck(summaryRow1ValueWelsh, summaryValue1Selector)
        textOnPageCheck(summaryRow2KeyWelsh, summaryKey2Selector)
        textOnPageCheck(summaryRow2ValueWelsh, summaryValue2Selector)
        textOnPageCheck(summaryRow3KeyWelsh, summaryKey3Selector)
        textOnPageCheck(summaryRow3ValueWelsh, summaryValue3Selector)
        textOnPageCheck(summaryRow4KeyWelsh, summaryKey4Selector)
        textOnPageCheck(summaryRow4ValueWelsh, summaryValue4Selector)

        textOnPageCheck(nextStepsSubheadingWelsh, nextStepsSubheadingSelector)
        textOnPageCheck(nextStepsP1Welsh, nextStepsP1TextSelector)
        linkCheck(nextStepsP2Welsh, nextStepsP2LinkSelector, nextStepsP2Link)
        textOnPageCheck(nextStepsP3Welsh, nextStepsP3TextSelector)
        linkCheck(nextStepsBullet1, nextStepsBullet1LinkSelector, nextStepsBullet1Link)
        linkCheck(nextStepsBullet2, nextStepsBullet2LinkSelector, nextStepsBullet2Link)
        textOnPageCheck(nextStepsP4Welsh, nextStepsP4TextSelector)
        linkCheck(nextStepsPrintWelsh, printLinkSelector, printLink)
      }
    }
  }

  "As an agent, the page should correctly render" when {
    import AgentExpectedResults._

    "the language is specified as English and" should {
      lazy val playSessionCookies = PlaySessionCookieBaker.bakeSessionCookie(Map(
        SessionValues.SUMMARY_DATA -> agentSummaryData.asJsonString,
        SessionValues.TAX_YEAR -> taxYear.toString,
        SessionValues.CLIENT_NINO -> nino,
        SessionValues.CLIENT_MTDITID -> mtditid
      ))

      val headers = Seq(HeaderNames.COOKIE -> playSessionCookies, "Csrf-Token" -> "nocheck")

      "display the user's tax return submission confirmation" which {
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
        textOnPageCheck(panelHeading, panelHeadingSelector)
        textOnPageCheck(panelSubheading, panelSubheadingSelector)

        textOnPageCheck(summaryRow1Key, summaryKey1Selector)
        textOnPageCheck(summaryRow1Value, summaryValue1Selector)
        textOnPageCheck(summaryRow2Key, summaryKey2Selector)
        textOnPageCheck(summaryRow2Value, summaryValue2Selector)
        textOnPageCheck(summaryRow3Key, summaryKey3Selector)
        textOnPageCheck(summaryRow3Value, summaryValue3Selector)
        textOnPageCheck(summaryRow4Key, summaryKey4Selector)
        textOnPageCheck(summaryRow4Value, summaryValue4Selector)

        textOnPageCheck(nextStepsSubheading, nextStepsSubheadingSelector)
        textOnPageCheck(nextStepsP1, nextStepsP1TextSelector)
        linkCheck(nextStepsP2, nextStepsP2LinkSelector, nextStepsP2Link)
        textOnPageCheck(nextStepsP3, nextStepsP3TextSelector)
        linkCheck(nextStepsBullet1, nextStepsBullet1LinkSelector, nextStepsBullet1Link)
        linkCheck(nextStepsBullet2, nextStepsBullet2LinkSelector, nextStepsBullet2Link)
        textOnPageCheck(nextStepsP4, nextStepsP4TextSelector)
        linkCheck(nextStepsPrint, printLinkSelector, printLink)
      }

    }

    "the language is specified as Welsh and" should {
      lazy val playSessionCookies = PlaySessionCookieBaker.bakeSessionCookie(Map(
        SessionValues.SUMMARY_DATA -> agentSummaryData.asJsonString,
        SessionValues.TAX_YEAR -> taxYear.toString,
        SessionValues.CLIENT_NINO -> nino,
        SessionValues.CLIENT_MTDITID -> mtditid
      ))

      val headers = Seq(HeaderNames.COOKIE -> playSessionCookies, "Csrf-Token" -> "nocheck", HeaderNames.ACCEPT_LANGUAGE -> "cy")

      "display the user's tax return submission confirmation" which {
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
        textOnPageCheck(panelHeadingWelsh, panelHeadingSelector)
        textOnPageCheck(panelSubheadingWelsh, panelSubheadingSelector)

        textOnPageCheck(summaryRow1KeyWelsh, summaryKey1Selector)
        textOnPageCheck(summaryRow1ValueWelsh, summaryValue1Selector)
        textOnPageCheck(summaryRow2KeyWelsh, summaryKey2Selector)
        textOnPageCheck(summaryRow2ValueWelsh, summaryValue2Selector)
        textOnPageCheck(summaryRow3KeyWelsh, summaryKey3Selector)
        textOnPageCheck(summaryRow3ValueWelsh, summaryValue3Selector)
        textOnPageCheck(summaryRow4KeyWelsh, summaryKey4Selector)
        textOnPageCheck(summaryRow4ValueWelsh, summaryValue4Selector)

        textOnPageCheck(nextStepsSubheadingWelsh, nextStepsSubheadingSelector)
        textOnPageCheck(nextStepsP1Welsh, nextStepsP1TextSelector)
        linkCheck(nextStepsP2Welsh, nextStepsP2LinkSelector, nextStepsP2Link)
        textOnPageCheck(nextStepsP3Welsh, nextStepsP3TextSelector)
        linkCheck(nextStepsBullet1, nextStepsBullet1LinkSelector, nextStepsBullet1Link)
        linkCheck(nextStepsBullet2, nextStepsBullet2LinkSelector, nextStepsBullet2Link)
        textOnPageCheck(nextStepsP4Welsh, nextStepsP4TextSelector)
        linkCheck(nextStepsPrintWelsh, printLinkSelector, printLink)
      }
    }
  }

  "As an individual" when {

    "there is no Summary Data in session which" should {
      lazy val playSessionCookies = PlaySessionCookieBaker.bakeSessionCookie(Map(
        SessionValues.TAX_YEAR -> taxYear.toString
      ))

      val headers = Seq(HeaderNames.COOKIE -> playSessionCookies, "Csrf-Token" -> "nocheck")

      "redirect to the overview page" which {
        val request = FakeRequest("GET", urlPath).withHeaders(headers: _*)

        lazy val result: Future[Result] = {
          authoriseIndividual()
          route(app, request).get
        }

        "returns status of SEE_OTHER(303)" in {
          status(result) shouldBe SEE_OTHER
        }

        "has the url of" in {
          redirectUrl(result) shouldBe expectedIncomeTaxSubmissionFrontendOverviewUrl
        }
      }
    }
  }

}
