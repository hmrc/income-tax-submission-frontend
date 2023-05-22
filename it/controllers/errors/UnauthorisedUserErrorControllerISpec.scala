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

import itUtils.{IntegrationTest, ViewHelpers}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.http.{HeaderNames, Status}
import play.api.libs.ws.WSResponse

class UnauthorisedUserErrorControllerISpec extends IntegrationTest with ViewHelpers with Status {

  object ExpectedResults {
    lazy val pageHeadingText = "You are not authorised to use this service"
    lazy val pageTitleText = "You are not authorised to use this service"
    lazy val youCanText = "You can:"
    lazy val goToText = "go to the"
    lazy val incomeTaxHomePageText = "Income Tax home page (opens in new tab)"
    lazy val forMoreText = "for more information"
    lazy val useText = "use"
    lazy val generalEnquiriesText = "Self Assessment: general enquiries (opens in new tab)"
    lazy val toSpeakText = "to speak to someone about your income tax"
    lazy val incomeTaxLink = "https://www.gov.uk/income-tax"
    lazy val selfAssessmentLink = "https://www.gov.uk/government/organisations/hm-revenue-customs/contact/self-assessment"

    lazy val pageHeadingTextWelsh = "Nid ydych wedi’ch awdurdodi i ddefnyddio’r gwasanaeth hwn"
    lazy val pageTitleTextWelsh = "Nid ydych wedi’ch awdurdodi i ddefnyddio’r gwasanaeth hwn"
    lazy val youCanTextWelsh = "Gallwch:"
    lazy val goToTextWelsh = "fynd i’r"
    lazy val incomeTaxHomePageTextWelsh = "Hafan Treth Incwm (yn agor tab newydd)"
    lazy val forMoreTextWelsh = "am ragor o wybodaeth"
    lazy val useTextWelsh = "defnyddio"
    lazy val generalEnquiriesTextWelsh = "Hunanasesiad: ymholiadau cyffredinol (yn agor tab newydd)"
    lazy val toSpeakTextWelsh = "i siarad â rhywun am eich Treth Incwm"
  }

  object Selectors {
    val youCanSelector = "#main-content > div > div > div.govuk-body > p"
    val incomeTaxSelector = "#main-content > div > div > ul > li:nth-child(1)"
    val selfAssessmentSelector = "#main-content > div > div > ul > li:nth-child(2)"
    val incomeTaxLinkSelector = "#govuk-income-tax-link"
    val selfAssessmentLinkSelector = "#govuk-self-assessment-link"
  }

  import ExpectedResults._
  import Selectors._

  val errorPageUrl = s"http://localhost:$port/update-and-submit-income-tax-return/error/not-authorised-to-use-service"

  "an user calling GET" when {
    "language is set to ENGLISH" should {
      "return a page" which {
        lazy val result: WSResponse = {
          authoriseIndividual()
          await(wsClient.url(errorPageUrl).get())
        }

        implicit def document: () => Document = () => Jsoup.parse(result.body)

        "returns status of UNAUTHORIZED(401)" in {
          result.status shouldBe UNAUTHORIZED
        }

        welshToggleCheck("English")
        titleCheck(pageTitleText, isWelsh = false)
        h1Check(pageHeadingText)
        textOnPageCheck(s"$youCanText", youCanSelector)
        textOnPageCheck(s"$goToText $incomeTaxHomePageText $forMoreText", incomeTaxSelector)
        textOnPageCheck(s"$useText $generalEnquiriesText $toSpeakText",selfAssessmentSelector)
        linkCheck(incomeTaxHomePageText, incomeTaxLinkSelector, incomeTaxLink)
        linkCheck(generalEnquiriesText,selfAssessmentLinkSelector,selfAssessmentLink)
      }
    }

    "language is set to WELSH" should {
      "return a page" which {
        lazy val result: WSResponse = {
          authoriseIndividual()
          await(wsClient.url(errorPageUrl).withHttpHeaders(HeaderNames.ACCEPT_LANGUAGE -> "cy").get())
        }

        implicit def document: () => Document = () => Jsoup.parse(result.body)

        "returns status of UNAUTHORIZED(401)" in {
          result.status shouldBe UNAUTHORIZED
        }

        welshToggleCheck("Welsh")
        titleCheck(pageTitleTextWelsh, isWelsh = true)
        h1Check(pageHeadingTextWelsh, "xl")
        textOnPageCheck(s"$youCanTextWelsh", youCanSelector)
        textOnPageCheck(s"$goToTextWelsh $incomeTaxHomePageTextWelsh $forMoreTextWelsh", incomeTaxSelector)
        textOnPageCheck(s"$useTextWelsh $generalEnquiriesTextWelsh $toSpeakTextWelsh", selfAssessmentSelector)
        linkCheck(incomeTaxHomePageTextWelsh, incomeTaxLinkSelector, incomeTaxLink)
        linkCheck(generalEnquiriesTextWelsh, selfAssessmentLinkSelector, selfAssessmentLink)
      }
    }
  }

}
