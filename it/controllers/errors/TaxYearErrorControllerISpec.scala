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

import itUtils.{IntegrationTest, ViewHelpers}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.http.{HeaderNames, Status}
import play.api.libs.ws.WSResponse

class TaxYearErrorControllerISpec extends IntegrationTest with ViewHelpers with Status {

  lazy val taxYear: Int = 2022
  lazy val lastTaxYear: Int = 2021

  object ExpectedResults {
    val pageTitleText = "Page not found"
    val pageHeadingText = "Page not found"
    val specificTaxYearText = s"You can only enter information for the $lastTaxYear to $taxYear tax year."
    val checkWebAddressText = "Check that you’ve entered the correct web address."
    val selfAssessmentEnquiriesText: String = "If the website address is correct or you selected a link or button, " +
      "you can use Self Assessment: general enquiries (opens in new tab) to speak to someone about your income tax."
    val linkText = "Self Assessment: general enquiries (opens in new tab)"
    val linkHref = "https://www.gov.uk/government/organisations/hm-revenue-customs/contact/self-assessment"

    val pageTitleTextWelsh = "Heb ddod o hyd i’r dudalen"
    val pageHeadingTextWelsh = "Heb ddod o hyd i’r dudalen"
    val specificTaxYearWelsh = s"Gallwch ond nodi gwybodaeth ar gyfer blwyddyn dreth $lastTaxYear i $taxYear."
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

  val errorPageUrl = s"http://localhost:$port/update-and-submit-income-tax-return/error/wrong-tax-year"

  "an user calling GET" when {
    "language is set to ENGLISH" should {
      "return a page" which {
        lazy val result: WSResponse = {
          authoriseIndividual()
          await(wsClient.url(errorPageUrl).get())
        }

        implicit def document: () => Document = () => Jsoup.parse(result.body)

        "returns status of OK(200)" in {
          result.status shouldBe OK
        }

        titleCheck(pageTitleText, isWelsh = false)
        welshToggleCheck("English")
        h1Check(pageHeadingText, "xl")
        linkCheck(linkText, link, linkHref)
        textOnPageCheck(specificTaxYearText, specificTaxYearSelector)
        textOnPageCheck(checkWebAddressText, checkWebAddressSelector)
        textOnPageCheck(selfAssessmentEnquiriesText, selfAssessmentEnquiriesSelector)
      }
    }

    "language is set to WELSH" should {
      "return a page" which {
        lazy val result: WSResponse = {
          authoriseIndividual()
          await(wsClient.url(errorPageUrl).withHttpHeaders(HeaderNames.ACCEPT_LANGUAGE -> "cy").get())
        }

        implicit def document: () => Document = () => Jsoup.parse(result.body)

        "returns status of OK(200)" in {
          result.status shouldBe 200
        }

        titleCheck(pageTitleTextWelsh, isWelsh = true)
        welshToggleCheck("Welsh")
        h1Check(pageHeadingTextWelsh, "xl")
        linkCheck(linkTextWelsh, link, linkHref)
        textOnPageCheck(specificTaxYearWelsh, specificTaxYearSelector)
        textOnPageCheck(checkWebAddressTextWelsh, checkWebAddressSelector)
        textOnPageCheck(selfAssessmentEnquiriesWelsh, selfAssessmentEnquiriesSelector)
      }
    }
  }

}
