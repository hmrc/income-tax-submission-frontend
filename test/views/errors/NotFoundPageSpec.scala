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

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.twirl.api.Html
import utils.ViewTest
import views.html.errors.NotFoundPage

class NotFoundPageSpec extends AnyWordSpec with Matchers with ViewTest{

  val link = "#govuk-income-tax-link"
  val paragraph = "#main-content > div > div > div.govuk-body > p:nth-child(1)"
  val paragraph2 = "#main-content > div > div > div.govuk-body > p:nth-child(2)"
  val paragraph3 = "#main-content > div > div > div.govuk-body > p:nth-child(3)"

  val pageTitleText = "Page not found"
  val pageHeaderText = "Page not found"
  val ifYouTypedText = "If you typed the web address, check it is correct."
  val ifYouUsedText = "If you used ‘copy and paste’ to enter the web address, check you copied the full address."
  val ifTheWebsiteText: String = "If the website address is correct or you selected a link or button, " +
    "you can use Self Assessment: general enquiries (opens in new tab) to speak to someone about your income tax."
  val linkText = "Self Assessment: general enquiries (opens in new tab)"

  val linkHref = "https://www.gov.uk/government/organisations/hm-revenue-customs/contact/self-assessment"

  val notFoundPage: NotFoundPage = app.injector.instanceOf[NotFoundPage]

  "The NotFoundPage when called in English" should {
    "render correctly" should {

      lazy val view: Html = notFoundPage()(fakeRequest, messages, mockConfig)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      titleCheck(pageTitleText)
      welshToggleCheck("English")
      h1Check(pageHeaderText)
      linkCheck(linkText, link, linkHref)
      textOnPageCheck(ifYouTypedText, paragraph)
      textOnPageCheck(ifYouUsedText, paragraph2)
      textOnPageCheck(ifTheWebsiteText, paragraph3)
    }
  }

  "The NotFoundPage when called in Welsh" should {
    "render correctly" should {

      lazy val view: Html = notFoundPage()(fakeRequest, welshMessages, mockConfig)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      titleCheck(pageTitleText)
      welshToggleCheck("Welsh")
      h1Check(pageHeaderText)
      linkCheck(linkText, link, linkHref)
      textOnPageCheck(ifYouTypedText, paragraph)
      textOnPageCheck(ifYouUsedText, paragraph2)
      textOnPageCheck(ifTheWebsiteText, paragraph3)
    }
  }
}