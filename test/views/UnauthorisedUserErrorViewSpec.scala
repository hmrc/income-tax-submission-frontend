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

package views

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import utils.ViewTest
import views.html.authErrorPages.UnauthorisedUserErrorView

class UnauthorisedUserErrorViewSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with ViewTest{

    val pageHeading = "#main-content > div > div > header > h1"
    val youCanSelector = "#main-content > div > div > div.govuk-body > p"
    val incomeTaxSelector = "#main-content > div > div > ul > li:nth-child(1)"
    val selfAssessmentSelector = "#main-content > div > div > ul > li:nth-child(2)"
    val incomeTaxLinkSelector = "#govuk-income-tax-link"
    val selfAssessmentLinkSelector = "#govuk-self-assessment-link"

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

    val unauthorisedUserErrorView: UnauthorisedUserErrorView = app.injector.instanceOf[UnauthorisedUserErrorView]
    lazy implicit val document: Document = Jsoup.parse(unauthorisedUserErrorView().body)

    "UnauthorisedUserErrorView" should {
      "Correctly render" which {
        titleCheck(pageTitleText)
        h1Check(pageHeadingText)
        textOnPageCheck(s"$youCanText", youCanSelector)
        textOnPageCheck(s"$goToText $incomeTaxHomePageText $forMoreText", incomeTaxSelector)
        textOnPageCheck(s"$useText $generalEnquiriesText $toSpeakText",selfAssessmentSelector)
        linkCheck(incomeTaxLinkText, incomeTaxLinkSelector, incomeTaxLink)
        linkCheck(selfAssessmentLinkText,selfAssessmentLinkSelector,selfAssessmentLink)
      }
  }
}
