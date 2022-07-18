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

import common.SessionValues
import helpers.PlaySessionCookieBaker
import itUtils.{IntegrationTest, ViewHelpers}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.http.HeaderNames
import play.api.http.Status._
import play.api.mvc.Result
import play.api.test.Helpers.{status, writeableOf_AnyContentAsEmpty}
import play.api.test.{FakeRequest, Helpers}
import uk.gov.hmrc.http.SessionKeys

import scala.concurrent.Future

class TaxYearErrorControllerISpec extends IntegrationTest with ViewHelpers {

  object ExpectedResults {
    val pageTitleText = "Page not found"
    val pageHeadingText = "Page not found"
    val specificTaxYearText = s"You can only enter information for the tax years $taxYearEndOfYearMinusOne to $taxYear."
    val specificTaxYearTextSingle = "You can only enter information for a valid tax year."
    val checkWebAddressText = "Check that you’ve entered the correct web address."
    val selfAssessmentEnquiriesText: String = "If the website address is correct or you selected a link or button, " +
      "you can use Self Assessment: general enquiries (opens in new tab) to speak to someone about your income tax."
    val linkText = "Self Assessment: general enquiries (opens in new tab)"
    val linkHref = "https://www.gov.uk/government/organisations/hm-revenue-customs/contact/self-assessment"

    val pageTitleTextWelsh = "Heb ddod o hyd i’r dudalen"
    val pageHeadingTextWelsh = "Heb ddod o hyd i’r dudalen"
    val specificTaxYearWelsh = s"Dim ond gwybodaeth ar gyfer y blynyddoedd treth $taxYearEndOfYearMinusOne i $taxYear y gallwch ei nodi."
    val specificTaxYearTextSingleWelsh = "Dim ond gwybodaeth ar gyfer blwyddyn dreth ddilys y gallwch ei nodi."
    val checkWebAddressTextWelsh = "Gwiriwch eich bod wedi nodi’r cyfeiriad gwe cywir."
    val selfAssessmentEnquiriesWelsh: String = "Os yw’r cyfeiriad gwe yn gywir neu os ydych wedi dewis cysylltiad neu fotwm, " +
      "gallwch wneud y canlynol: Hunanasesiad: ymholiadau cyffredinol (yn agor tab newydd) i siarad â rhywun am eich Treth Incwm."
    val linkTextWelsh = "Hunanasesiad: ymholiadau cyffredinol (yn agor tab newydd)"
  }

  object Selectors {
    val link = "#govuk-income-tax-link"
    val specificTaxYearSelector = "#main-content > div > div > div.govuk-body > p:nth-child(1)"
    val checkWebAddressSelector = "#main-content > div > div > div.govuk-body > p:nth-child(2)"
    val selfAssessmentEnquiriesSelector = "#main-content > div > div > div.govuk-body > p:nth-child(3)"
  }

  import ExpectedResults._
  import Selectors._

  val errorPageUrl = "/update-and-submit-income-tax-return/error/wrong-tax-year"

  "an user calling GET" when {
    "language is set to ENGLISH" should {
      "return a page with multiple tax years in session" which {
        lazy val playSessionCookies = PlaySessionCookieBaker.bakeSessionCookie(Map(
          SessionKeys.authToken -> "mock-bearer-token",
          SessionValues.TAX_YEAR -> taxYear.toString,
          SessionValues.VALID_TAX_YEARS -> validTaxYearList.mkString(",")
        ))

        val errorPageHeaders = Seq(HeaderNames.COOKIE -> playSessionCookies, "Csrf-Token" -> "nocheck")
        val request = FakeRequest("GET", errorPageUrl).withHeaders(errorPageHeaders: _*)

        lazy val result: Future[Result] = {
          authoriseIndividual()
          route(app, request).get
        }

        implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

        "returns status of OK(200)" in {
          status(result) shouldBe OK
        }

        titleCheck(pageTitleText, isWelsh = false)
        welshToggleCheck("English")
        h1Check(pageHeadingText, "xl")
        linkCheck(linkText, link, linkHref)
        textOnPageCheck(specificTaxYearText, specificTaxYearSelector)
        textOnPageCheck(checkWebAddressText, checkWebAddressSelector)
        textOnPageCheck(selfAssessmentEnquiriesText, selfAssessmentEnquiriesSelector)
      }

      "return a page with a single tax year in session" which {
        lazy val playSessionCookies = PlaySessionCookieBaker.bakeSessionCookie(Map(
          SessionKeys.authToken -> "mock-bearer-token",
          SessionValues.TAX_YEAR -> taxYear.toString,
          SessionValues.VALID_TAX_YEARS -> singleValidTaxYear.mkString(",")
        ))

        val errorPageHeaders = Seq(HeaderNames.COOKIE -> playSessionCookies, "Csrf-Token" -> "nocheck")
        val request = FakeRequest("GET", errorPageUrl).withHeaders(errorPageHeaders: _*)

        lazy val result: Future[Result] = {
          authoriseIndividual()
          route(app, request).get
        }

        implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

        "returns status of OK(200)" in {
          status(result) shouldBe OK
        }

        titleCheck(pageTitleText, isWelsh = false)
        welshToggleCheck("English")
        textOnPageCheck(specificTaxYearTextSingle, specificTaxYearSelector)
      }
    }

    "language is set to WELSH" should {
      "return a page with multiple tax years in session" which {
        lazy val playSessionCookies = PlaySessionCookieBaker.bakeSessionCookie(Map(
          SessionKeys.authToken -> "mock-bearer-token",
          SessionValues.TAX_YEAR -> taxYear.toString,
          SessionValues.VALID_TAX_YEARS -> validTaxYearList.mkString(",")
        ))

        val errorPageHeaders = Seq(HeaderNames.COOKIE -> playSessionCookies, "Csrf-Token" -> "nocheck")
        val request = FakeRequest("GET", errorPageUrl).withHeaders(errorPageHeaders: _*)

        lazy val result: Future[Result] = {
          authoriseIndividual()
          route(app, request, isWelsh = true).get
        }

        implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

        "returns status of OK(200)" in {
          status(result) shouldBe 200
        }

        titleCheck(pageTitleTextWelsh, isWelsh = true)
        welshToggleCheck("Welsh")
        h1Check(pageHeadingTextWelsh, "xl")
        linkCheck(linkTextWelsh, link, linkHref)
        textOnPageCheck(specificTaxYearWelsh, specificTaxYearSelector)
        textOnPageCheck(checkWebAddressTextWelsh, checkWebAddressSelector)
        textOnPageCheck(selfAssessmentEnquiriesWelsh, selfAssessmentEnquiriesSelector)
      }

      "return a page with a single tax year in session" which {
        lazy val playSessionCookies = PlaySessionCookieBaker.bakeSessionCookie(Map(
          SessionKeys.authToken -> "mock-bearer-token",
          SessionValues.TAX_YEAR -> taxYear.toString,
          SessionValues.VALID_TAX_YEARS -> singleValidTaxYear.mkString(",")
        ))

        val errorPageHeaders = Seq(HeaderNames.COOKIE -> playSessionCookies, "Csrf-Token" -> "nocheck")
        val request = FakeRequest("GET", errorPageUrl).withHeaders(errorPageHeaders: _*)

        lazy val result: Future[Result] = {
          authoriseIndividual()
          route(app, request, isWelsh = true).get
        }

        implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

        "returns status of OK(200)" in {
          status(result) shouldBe OK
        }

        titleCheck(pageTitleTextWelsh, isWelsh = true)
        welshToggleCheck("Welsh")
        textOnPageCheck(specificTaxYearTextSingleWelsh, specificTaxYearSelector)
      }
    }
  }
}
