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

package views.errors

import itUtils.ViewTest
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.twirl.api.Html
import views.html.errors.InternalServerErrorPage

class InternalServerErrorPageSpec extends AnyWordSpec with Matchers with ViewTest {

  val paragraph = "#main-content > div > div > div.govuk-body > p:nth-child(1)"
  val paragraph2 = "#main-content > div > div > div.govuk-body > p:nth-child(2)"
  val paragraph3 = "#main-content > div > div > ul > li:nth-child(1)"
  val paragraph4 = "#main-content > div > div > ul > li:nth-child(2)"
  val link = "#govuk-income-tax-link"
  val link2 = "#govuk-self-assessment-link"

  val pageTitleText = "Sorry, there is a problem with the service"
  val pageHeaderText = "Sorry, there is a problem with the service"
  val tryAgainText = "Try again later."
  val youCanAlsoText = "You can also:"
  val goToTheText = "go to the Income Tax home page (opens in new tab) for more information"
  val useSelfAssesText = "use Self Assessment: general enquiries (opens in new tab) to speak to someone about your income tax"
  val link1Text = "Income Tax home page (opens in new tab)"
  val link2Text = "Self Assessment: general enquiries (opens in new tab)"

  val pageTitleTextWelsh = "Mae’n ddrwg gennym – mae problem gyda’r gwasanaeth"
  val pageHeaderTextWelsh = "Mae’n ddrwg gennym – mae problem gyda’r gwasanaeth"
  val tryAgainTextWelsh = "Rhowch gynnig arall arni yn nes ymlaen."
  val youCanAlsoTextWelsh = "Gallwch hefyd wneud y canlynol:"
  val goToTheTextWelsh = "neu ewch i’r Hafan Treth Incwm (agor mewn tab newydd) am ragor o wybodaeth"
  val useSelfAssesTextWelsh = "defnyddiwch Hunanasesiad: ymholiadau cyffredinol (yn agor tab newydd) i siarad â rhywun am eich Treth Incwm"
  val link1TextWelsh = "Hafan Treth Incwm (agor mewn tab newydd)"
  val link2TextWelsh = "Hunanasesiad: ymholiadau cyffredinol (yn agor tab newydd)"

  val link1Href = "https://www.gov.uk/income-tax"
  val link2Href = "https://www.gov.uk/government/organisations/hm-revenue-customs/contact/self-assessment"

  val internalServerErrorPage: InternalServerErrorPage = app.injector.instanceOf[InternalServerErrorPage]

  "The InternalServerErrorPageSpec when called in English" should {
    "render correctly" should {

      lazy val view: Html = internalServerErrorPage()(fakeRequest, messages, appConfig)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      titleCheck(pageTitleText, isWelsh = false)
      welshToggleCheck("English")
      h1Check(pageHeaderText)
      linkCheck(link1Text, link, link1Href)
      linkCheck(link2Text, link2, link2Href)
      textOnPageCheck(tryAgainText, paragraph)
      textOnPageCheck(youCanAlsoText, paragraph2)
      textOnPageCheck(goToTheText, paragraph3)
      textOnPageCheck(useSelfAssesText, paragraph4)
    }
  }

  "The InternalServerErrorPageSpec when called in Welsh" should {
    "render correctly" should {

      lazy val view: Html = internalServerErrorPage()(fakeRequest, welshMessages, appConfig)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      titleCheck(pageTitleTextWelsh, isWelsh = true)
      welshToggleCheck("Welsh")
      h1Check(pageHeaderTextWelsh)
      linkCheck(link1TextWelsh, link, link1Href)
      linkCheck(link2TextWelsh, link2, link2Href)
      textOnPageCheck(tryAgainTextWelsh, paragraph)
      textOnPageCheck(youCanAlsoTextWelsh, paragraph2)
      textOnPageCheck(goToTheTextWelsh, paragraph3)
      textOnPageCheck(useSelfAssesTextWelsh, paragraph4)
    }
  }
}
