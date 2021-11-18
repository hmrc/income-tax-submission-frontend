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
import views.html.errors.NotFoundPage

class NotFoundPageSpec extends AnyWordSpec with Matchers with ViewTest {

  val link = "#govuk-income-tax-link"
  val paragraph = "#main-content > div > div > div.govuk-body > p:nth-child(1)"
  val paragraph2 = "#main-content > div > div > div.govuk-body > p:nth-child(2)"
  val paragraph3 = "#main-content > div > div > div.govuk-body > p:nth-child(3)"

  val pageTitleText = "Page not found"
  val pageHeaderText = "Page not found"
  val ifYouTypedAddressText = "If you typed the web address, check it is correct."
  val ifYouUsedCopyPasteText = "If you used ‘copy and paste’ to enter the web address, check you copied the full address."
  val ifAddressCorrectText: String = "If the website address is correct or you selected a link or button, " +
    "you can use Self Assessment: general enquiries (opens in new tab) to speak to someone about your income tax."
  val linkText = "Self Assessment: general enquiries (opens in new tab)"

  val pageTitleTextWelsh = "Heb ddod o hyd i’r dudalen"
  val pageHeaderTextWelsh = "Heb ddod o hyd i’r dudalen"
  val ifYouTypedAddressTextWelsh = "Os gwnaethoch deipio’r cyfeiriad gwe, gwiriwch ei fod yn gywir."
  val ifYouUsedCopyPasteTextWelsh = "Os ydych wedi copïo a gludo’r cyfeiriad gwe, sicrhewch eich bod wedi copïo’r cyfeiriad llawn."
  val ifAddressCorrectTextWelsh: String = "Os yw’r cyfeiriad gwe yn gywir neu os ydych wedi dewis cysylltiad neu fotwm," +
    " gallwch wneud y canlynol: Hunanasesiad: ymholiadau cyffredinol (yn agor tab newydd) i siarad â rhywun am eich Treth Incwm."
  val linkTextWelsh = "Hunanasesiad: ymholiadau cyffredinol (yn agor tab newydd)"

  val linkHref = "https://www.gov.uk/government/organisations/hm-revenue-customs/contact/self-assessment"

  val notFoundPage: NotFoundPage = app.injector.instanceOf[NotFoundPage]

  "The NotFoundPage when called in English" should {
    "render correctly" should {

      lazy val view: Html = notFoundPage()(fakeRequest, messages, appConfig)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      titleCheck(pageTitleText, isWelsh = false)
      welshToggleCheck("English")
      h1Check(pageHeaderText)
      linkCheck(linkText, link, linkHref)
      textOnPageCheck(ifYouTypedAddressText, paragraph)
      textOnPageCheck(ifYouUsedCopyPasteText, paragraph2)
      textOnPageCheck(ifAddressCorrectText, paragraph3)
    }
  }

  "The NotFoundPage when called in Welsh" should {
    "render correctly" should {

      lazy val view: Html = notFoundPage()(fakeRequest, welshMessages, appConfig)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      titleCheck(pageTitleTextWelsh, isWelsh = true)
      welshToggleCheck("Welsh")
      h1Check(pageHeaderTextWelsh)
      linkCheck(linkTextWelsh, link, linkHref)
      textOnPageCheck(ifYouTypedAddressTextWelsh, paragraph)
      textOnPageCheck(ifYouUsedCopyPasteTextWelsh, paragraph2)
      textOnPageCheck(ifAddressCorrectTextWelsh, paragraph3)
    }
  }
}
