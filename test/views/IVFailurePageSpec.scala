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
import play.twirl.api.Html
import utils.ViewTest
import views.html.StartPage
import views.html.errors.IVFailurePage


class IVFailurePageSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with ViewTest{

  val pageTitleText = "There’s a problem"
  val pageHeadingText = "There’s a problem"

  val p1 = "You cannot access this service. This may be because:"
  val li1 = "you took too long to enter information and the service has timed out"
  val li2 = "you have failed to answer enough questions correctly"
  val li3 = "we could not match your details to our system"
  val signOutText = "Sign out"

  object Selectors{
    val pageHeading = "#main-content > div > div > header > h1"
    val p1 = "#main-content > div > div > div:nth-child(2) > p:nth-child(1)"
    val bullet1 = "#main-content > div > div > ul > li:nth-child(1)"
    val bullet2 = "#main-content > div > div > ul > li:nth-child(2)"
    val bullet3 = "#main-content > div > div > ul > li:nth-child(3)"
    val button = "#continue"
  }

  val iVFailurePage: IVFailurePage = app.injector.instanceOf[IVFailurePage]

  "Rendering the start page when the user is an individual" should {

    lazy val view: Html = iVFailurePage(controllers.routes.SignOutController.signOut())(fakeRequest,messages,mockConfig)
    lazy implicit val document: Document = Jsoup.parse(view.body)

    titleCheck(pageTitleText)
    h1Check(pageHeadingText)

    textOnPageCheck(p1, Selectors.p1)
    textOnPageCheck(li1, Selectors.bullet1)
    textOnPageCheck(li2, Selectors.bullet2)
    textOnPageCheck(li3, Selectors.bullet3)
    buttonCheck(signOutText, Selectors.button)
  }
}
