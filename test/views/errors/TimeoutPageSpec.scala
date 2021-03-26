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

import controllers.routes
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.twirl.api.Html
import utils.ViewTest
import views.html.errors.TimeoutPage

class TimeoutPageSpec extends AnyWordSpec with Matchers with ViewTest{

  val taxYear = 2022
  val pageTitleText = "For your security, we signed you out"
  val pageHeadingText = "For your security, we signed you out"
  val p1Text = "We did not save your answers."
  val buttonText = "Sign in"

  val p1 = "#main-content > div > div > div:nth-child(2) > p:nth-child(1)"
  val continueButtonSelector = "#continue"

  val timeoutPage: TimeoutPage = app.injector.instanceOf[TimeoutPage]

  "The TimeoutPage when called in English" should {
    "render correctly" should {
      lazy val view: Html = timeoutPage(routes.StartPageController.show(taxYear))(fakeRequest, messages, mockConfig)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      titleCheck(pageTitleText)
      welshToggleCheck("English")
      h1Check(pageHeadingText)
      textOnPageCheck(p1Text, p1)
      buttonCheck(buttonText, continueButtonSelector)
    }
  }

  "The TimeoutPage when called in Welsh" should {
    "render correctly" should {
      lazy val view: Html = timeoutPage(routes.StartPageController.show(taxYear))(fakeRequest, welshMessages, mockConfig)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      titleCheck(pageTitleText)
      welshToggleCheck("Welsh")
      h1Check(pageHeadingText)
      textOnPageCheck(p1Text, p1)
      buttonCheck(buttonText, continueButtonSelector)
    }
  }

}