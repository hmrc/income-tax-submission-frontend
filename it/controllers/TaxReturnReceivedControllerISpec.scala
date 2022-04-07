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

package controllers

import common.SessionValues
import config.AppConfig
import helpers.PlaySessionCookieBaker
import itUtils.{IntegrationTest, ViewHelpers}
import models.TaxReturnReceivedModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.http.HeaderNames
import play.api.i18n.{Lang, Messages}
import play.api.mvc.Result
import play.api.test.Helpers.{OK, SEE_OTHER, status, writeableOf_AnyContentAsEmpty}
import play.api.test.{FakeRequest, Helpers}
import uk.gov.hmrc.play.language.LanguageUtils
import utils.ImplicitDateFormatter

import java.time.LocalDate
import java.util.Locale
import scala.concurrent.Future

class TaxReturnReceivedControllerISpec extends IntegrationTest with ImplicitDateFormatter with ViewHelpers {

  implicit val languageUtils: LanguageUtils = app.injector.instanceOf[LanguageUtils]

  def toMessages(language: String): Messages = {
    mcc.messagesApi.preferred(Seq(
      new Lang(new Locale(language))
    ))
  }

  private val timeStamp: LocalDate = LocalDate.now()
  private val nino: String = "AA012345A"
  private val mtditid: String = "1234567890"

  object ExpectedResults {
    val summaryRow1Key: String = "Name"
    val summaryRow2Key: String = "Unique Tax Reference (UTR)"
    val summaryRow3Key: String = "Date submitted"

    val summaryRow1KeyWelsh: String = "Enw"
    val summaryRow2KeyWelsh: String = "Cyfeirnod Unigryw y Trethdalwr (UTR)"
    val summaryRow3KeyWelsh: String = "Dyddiad cyflwyno"
    val nextStepsPrintWelsh: String = "Argraffu’r dudalen hon"

    val expectedIncomeTaxSubmissionFrontendOverviewUrl: String = s"/update-and-submit-income-tax-return/$taxYear/view"
  }

  object IndividualExpectedResults {
    val individualSummaryData: TaxReturnReceivedModel = TaxReturnReceivedModel(
      "John Individual", 1000, "IN12345", 321, 312, 123
    )

    val summaryRow1Value: String = "John Individual"
    val summaryRow2Value: String = "IN12345"
    val summaryRow3Value: String = timeStamp.toLongDate(toMessages("EN"))

    val panelHeading: String = "Confirmation:"
    val panelSubheading: String = s"We’ve received your Income Tax Return for $taxYearEOY to $taxYear"

    val nextStepsP1: String = "Find out what you owe and how to pay."
    val nextStepsP2: String = "what you owe and how to pay."
    val nextStepsP2Link: String = "http://localhost:9081/report-quarterly/income-and-expenses/view/payments-owed"

    val nextStepsP4: String = "If you need to contact us about your Income Tax Return, tell us your UTR."

    val panelHeadingWelsh: String = "Cadarnhad:"
    val panelSubheadingWelsh: String = s"Rydym wedi cael eich Ffurflen Dreth Incwm ar gyfer $taxYearEOY i $taxYear"
    val summaryRow1ValueWelsh: String = "John Individual"
    val summaryRow2ValueWelsh: String = "IN12345"
    val summaryRow3ValueWelsh: String = timeStamp.toLongDate(toMessages("CY"))

    val nextStepsP1Welsh: String = "Gallwch gael gwybod faint sy’n ddyledus gennych a sut i dalu."
    val nextStepsP2Welsh: String = "faint sy’n ddyledus gennych a sut i dalu."
    val nextStepsP4Welsh: String = "Os oes angen i chi gysylltu â ni am eich Ffurflen Dreth Incwm, rhowch eich UTR i ni."
  }

  object AgentExpectedResults {

    val agentSummaryData: TaxReturnReceivedModel = TaxReturnReceivedModel(
      "Jane Agent", 750.50, "AG98765", 321, 312, 123
    )

    val summaryRow1Value: String = "Jane Agent"
    val summaryRow2Value: String = "AG98765"
    val summaryRow3Value: String = timeStamp.toLongDate(toMessages("EN"))

    val panelHeading = "Confirmation:"
    val panelSubheading = s"We’ve received your client’s Income Tax Return for $taxYearEOY to $taxYear"

    val nextStepsP1: String = "Find out what your client owes and how to pay."
    val nextStepsP2: String = "what your client owes and how to pay."
    val nextStepsP2Link: String = "http://localhost:9081/report-quarterly/income-and-expenses/view/agents/payments-owed"

    val nextStepsP4: String = "If you need to contact us about your client’s Income Tax Return, tell us their UTR."

    val panelHeadingWelsh = "Cadarnhad:"
    val panelSubheadingWelsh = s"Rydym wedi cael Ffurflen Dreth Incwm eich cleient ar gyfer $taxYearEOY i $taxYear"

    val summaryRow1ValueWelsh: String = "Jane Agent"
    val summaryRow2ValueWelsh: String = "AG98765"
    val summaryRow3ValueWelsh: String = timeStamp.toLongDate(toMessages("CY"))

    val nextStepsP1Welsh: String = "Gallwch gael gwybod faint sy’n ddyledus gan eich cleient a sut i dalu."
    val nextStepsP2Welsh: String = "faint sy’n ddyledus gan eich cleient a sut i dalu."
    val nextStepsP4Welsh: String = "Os oes angen i chi gysylltu â ni am Ffurflen Dreth Incwm eich cleient rhowch ei UTR i ni."
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
    val nextStepsP1TextSelector = "#main-content > div > div > div.govuk-body > p:nth-child(1)"
    val nextStepsP2LinkSelector = "#amount_owed_link"
    val nextStepsP4TextSelector = "#main-content > div > div > div.govuk-body > p:nth-child(2)"
  }

  import ExpectedResults._
  import Selectors._

  private val urlPath = s"/update-and-submit-income-tax-return/$taxYear/income-tax-return-received"

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

        textOnPageCheck(nextStepsP1, nextStepsP1TextSelector)
        linkCheck(nextStepsP2, nextStepsP2LinkSelector, nextStepsP2Link)
        textOnPageCheck(nextStepsP4, nextStepsP4TextSelector)
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

        textOnPageCheck(nextStepsP1Welsh, nextStepsP1TextSelector)
        linkCheck(nextStepsP2Welsh, nextStepsP2LinkSelector, nextStepsP2Link)
        textOnPageCheck(nextStepsP4Welsh, nextStepsP4TextSelector)
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

        textOnPageCheck(nextStepsP1, nextStepsP1TextSelector)
        linkCheck(nextStepsP2, nextStepsP2LinkSelector, nextStepsP2Link)
        textOnPageCheck(nextStepsP4, nextStepsP4TextSelector)
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

        textOnPageCheck(nextStepsP1Welsh, nextStepsP1TextSelector)
        linkCheck(nextStepsP2Welsh, nextStepsP2LinkSelector, nextStepsP2Link)
        textOnPageCheck(nextStepsP4Welsh, nextStepsP4TextSelector)
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
