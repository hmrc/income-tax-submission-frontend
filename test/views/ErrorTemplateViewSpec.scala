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

import config.FrontendAppConfig
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.twirl.api.Html
import views.html.templates.ErrorTemplate

class ErrorTemplateViewSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  object Selectors{
    val pageTitle = "body > header > div > div.govuk-header__content > a"
    val pageHeading = "#main-content > div > div > h1"
    val paragraph = "#main-content > div > div > p"
  }
  val errorTemplateView: ErrorTemplate = app.injector.instanceOf[ErrorTemplate]

  implicit lazy val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("", "")
  implicit lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit lazy val messages: Messages = messagesApi.preferred(fakeRequest)
  implicit lazy val mockConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]

  val pageHeading = "This page can’t be found"
  val pageTitle = "Update and Submit an Income Tax Return"
  val paragraph = "Please check that you have entered the correct web address."

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

    lazy val view: Html = errorTemplateView(pageTitle,pageHeading,paragraph)(fakeRequest,messages,mockConfig)
    lazy implicit val document: Document = Jsoup.parse(view.body)

    "have the correct page title" in {
      elementText(Selectors.pageTitle) shouldBe "Update and Submit an Income Tax Return"
    }

    "have the correct page heading" in {
      elementText(Selectors.pageHeading) shouldBe "This page can’t be found"
    }

    "have the correct paragraph text" in {
      elementText(Selectors.paragraph) shouldBe "Please check that you have entered the correct web address."
    }
  }
}