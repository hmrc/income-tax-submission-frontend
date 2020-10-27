/*
 * Copyright 2020 HM Revenue & Customs
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
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import views.html.StartPage
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.twirl.api.Html


class StartPageViewSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  object Selectors{
    val individualPageHeading = "#main-content > div > div > h1"
    val agentPageHeading = "#main-content > div > div > h1"
    val p1Individual =  "#main-content > div > div > div:nth-child(3) > p:nth-child(1)"
    val p1Agent = "#main-content > div > div > div:nth-child(3) > p:nth-child(1)"
    val p2Individual = "#main-content > div > div > div:nth-child(3) > p:nth-child(2)"
    val p2Agent = "#main-content > div > div > div:nth-child(3) > p:nth-child(2)"
  }

  val startPageView: StartPage = app.injector.instanceOf[StartPage]

  implicit lazy val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("", "")
  implicit lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit lazy val messages: Messages = messagesApi.preferred(fakeRequest)
  implicit lazy val mockConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]

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
  "Rendering the start page when the user is an individual" should {

    lazy val view: Html = startPageView(false)(fakeRequest,messages,mockConfig)
    lazy implicit val document: Document = Jsoup.parse(view.body)

    "have the correct page heading" in {

      elementText(Selectors.individualPageHeading) shouldBe "Tell HMRC about your Income Tax"
    }

    "have the correct 1st paragraph" in {
      elementText(Selectors.p1Individual) shouldBe "You can use this service to make updates to your Income Tax Self" +
        " Assessment throughout the year."
    }

    "have the correct 2nd paragraph" in {
      elementText(Selectors.p2Individual) shouldBe "We’re still working on this service, so you will only be able to " +
        "update your:"
    }
  }

  "rendering the start page when the user is an agent" should {
    lazy val view: Html = startPageView(true)(fakeRequest,messages,mockConfig)
    lazy implicit val document: Document = Jsoup.parse(view.body)

    "have the correct page heading" in {
      elementText(Selectors.agentPageHeading) shouldBe "Tell HMRC about your client’s Income Tax"
    }

    "have the correct 1st paragraph" in {
      elementText(Selectors.p1Agent) shouldBe "You can use this service to make updates to your client’s Income Tax " +
        "Self Assessment throughout the year."
    }

    "have the correct 2nd paragraph" in {
      elementText(Selectors.p2Agent) shouldBe "We’re still working on this service, so you will only be able to " +
        "update their:"
    }
  }

}
