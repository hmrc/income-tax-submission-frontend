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

import config.AppConfig
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.twirl.api.Html
import views.html.errors.{InternalServerErrorPage, ServiceUnavailablePage}

class ServiceUnavailableErrorPageSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  object Selectors{
    val pageTitle = "head > title"
    val pageHeading = "#main-content > div > div > header > h1"
    val paragraph = "#main-content > div > div > p"
    val paragraph2 = "#main-content > div > div > p:nth-child(3)"
    val paragraph3 = "#main-content > div > div > ul > li:nth-child(1) > p"
    val paragraph4 = "#main-content > div > div > ul > li:nth-child(2) > p"
    val link = "#govuk-income-tax-link"
    val link2 = "#main-content > div > div > ul > li:nth-child(2) > p > a"
  }
  
  val internalServerErrorPage: ServiceUnavailablePage = app.injector.instanceOf[ServiceUnavailablePage]

  implicit lazy val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("", "")
  implicit lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit lazy val messages: Messages = messagesApi.preferred(fakeRequest)
  implicit lazy val mockConfig: AppConfig = app.injector.instanceOf[AppConfig]

  def element(cssSelector: String)(implicit document: Document): Element = {
    val elements = document.select(cssSelector)

    if(elements.size == 0) {
      fail(s"No element exists with the selector '$cssSelector'")
    }

    document.select(cssSelector).first()
  }
  def elementText(selector: String)(implicit document: Document): String = {
    element(selector).text()
  }

  "Rendering the error page when there is an error" should {

    lazy val view: Html = internalServerErrorPage()(fakeRequest,messages,mockConfig)
    lazy implicit val document: Document = Jsoup.parse(view.body)

    "have the correct page title" in {
      elementText(Selectors.pageTitle) shouldBe "Sorry, the service is unavailable - Update and submit an Income Tax Return - GOV.UK"
    }

    "have the correct page heading" in {
      elementText(Selectors.pageHeading) shouldBe "Sorry, the service is unavailable"
    }

    "have the correct links" in {
      document.select(Selectors.link).attr("href") shouldBe "https://www.gov.uk/income-tax"
      document.select(Selectors.link2).attr("href") shouldBe "https://www.gov.uk/government/organisations/hm-revenue-customs/contact/self-assessment"
    }

    "have the correct paragraph text" in {
      elementText(Selectors.paragraph) shouldBe "You will be able to use the service later."
      elementText(Selectors.paragraph2) shouldBe "You can also:"
      elementText(Selectors.paragraph3) shouldBe "go to the Income Tax home page (opens in new tab) for more information"
      elementText(Selectors.paragraph4) shouldBe "use Self Assessment: general enquiries (opens in new tab) to speak to someone about your income tax"
    }
  }
}