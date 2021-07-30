
package controllers.errors

import itUtils.{IntegrationTest, ViewHelpers}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.http.{HeaderNames, Status}
import play.api.libs.ws.WSResponse

class ReturnTaxYearExistsControllerISpec extends IntegrationTest with ViewHelpers with Status {

  val taxYear: Int = 2022
  val lastTaxYear: Int = taxYear - 1

  object ExpectedResults {
    lazy val expectedHeadingText = "We already have an Income Tax Return for that tax year"
    lazy val expectedP1Text = s"We have an Income Tax Return for the $lastTaxYear to $taxYear tax year."
    lazy val expectedP2TextIndividual = "You can go to your Income Tax account to see your Income Tax Returns."
    lazy val expectedP2TextAgent = "You can go to your client’s Income Tax account to see their Income Tax Returns."
    lazy val expectedReturnToTaxAccountButtonText = "Back to Income Tax account"
    lazy val expectedReturnToTaxAccountButtonLink = s"http://localhost:9302/income-through-software/return/$taxYear/view"
    lazy val expectedSignOutButtonText = "Sign Out"
    lazy val expectedSignOutButtonLinkIndividual = "http://localhost:9302/income-through-software/return/sign-out?isAgent=false"
    lazy val expectedSignOutButtonLinkAgent = "http://localhost:9302/income-through-software/return/sign-out?isAgent=true"
  }

  object Selectors {

  }

  import ExpectedResults._
  import Selectors._

  val pageUrl = s"http://localhost:$port/income-through-software/return/$taxYear/already-have-income-tax-return "

}

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
    lazy val incomeTaxLinkText = "Income Tax home page (opens in new tab)"
    lazy val selfAssessmentLinkText = "Self Assessment: general enquiries (opens in new tab)"
    lazy val incomeTaxLink = "https://www.gov.uk/income-tax"
    lazy val selfAssessmentLink = "https://www.gov.uk/government/organisations/hm-revenue-customs/contact/self-assessment"
  }

  object Selectors {
    val pageHeading = "#main-content > div > div > header > h1"
    val youCanSelector = "#main-content > div > div > div.govuk-body > p"
    val incomeTaxSelector = "#main-content > div > div > ul > li:nth-child(1)"
    val selfAssessmentSelector = "#main-content > div > div > ul > li:nth-child(2)"
    val incomeTaxLinkSelector = "#govuk-income-tax-link"
    val selfAssessmentLinkSelector = "#govuk-self-assessment-link"
  }

  import ExpectedResults._
  import Selectors._

  val errorPageUrl = s"http://localhost:$port/income-through-software/return/error/not-authorised-to-use-service"

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
        titleCheck(pageTitleText)
        h1Check(pageHeadingText, "xl")
        textOnPageCheck(s"$youCanText", youCanSelector)
        textOnPageCheck(s"$goToText $incomeTaxHomePageText $forMoreText", incomeTaxSelector)
        textOnPageCheck(s"$useText $generalEnquiriesText $toSpeakText",selfAssessmentSelector)
        linkCheck(incomeTaxLinkText, incomeTaxLinkSelector, incomeTaxLink)
        linkCheck(selfAssessmentLinkText,selfAssessmentLinkSelector,selfAssessmentLink)
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
        titleCheck(pageTitleText)
        h1Check(pageHeadingText, "xl")
        textOnPageCheck(s"$youCanText", youCanSelector)
        textOnPageCheck(s"$goToText $incomeTaxHomePageText $forMoreText", incomeTaxSelector)
        textOnPageCheck(s"$useText $generalEnquiriesText $toSpeakText",selfAssessmentSelector)
        linkCheck(incomeTaxLinkText, incomeTaxLinkSelector, incomeTaxLink)
        linkCheck(selfAssessmentLinkText,selfAssessmentLinkSelector,selfAssessmentLink)
      }
    }
  }

}
