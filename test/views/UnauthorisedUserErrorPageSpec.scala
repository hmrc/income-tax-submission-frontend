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

class UnauthorisedUserErrorPageSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with ViewTest{

  object Selectors {

    val pageHeading = "#main-content > div > div > header > h1"
    val p1 = "#main-content > div > div > div.govuk-body > p"
    val li1 = "#main-content > div > div > ul > li:nth-child(1)"
    val li2 = "#main-content > div > div > ul > li:nth-child(2)"
    val link1 = "#govuk-income-tax-link"
    val link2 = "#govuk-self-assessment-link"
  }

  lazy val pText1 = "You can:"
  lazy val listText1 = "go to the Income Tax home page (opens in new tab) for more information"
  lazy val listText2 = "use Self Assessment: general enquiries (opens in new tab) to speak to someone about your income tax"
  lazy val linkText1 = "Income Tax home page (opens in new tab)"
  lazy val linkText2 = "Self Assessment: general enquiries (opens in new tab)"
  lazy val href1 = "https://www.gov.uk/income-tax"
  lazy val href2 = "https://www.gov.uk/government/organisations/hm-revenue-customs/contact/self-assessment"


  "UnauthorisedUserErrorPage " should {
     lazy val view  = unauthorisedUserErrorPage()
    lazy implicit val document: Document = Jsoup.parse(view.body)

    "have a page heading of" in {
      elementText(Selectors.pageHeading) shouldBe "You are not authorised to use this service"
    }

    "have text of " in {
      elementText(Selectors.p1) shouldBe pText1
      elementText(Selectors.li1) shouldBe listText1
      elementText(Selectors.li2) shouldBe listText2
    }

    linkCheck(linkText1, Selectors.link1, href1)
    linkCheck(linkText2, Selectors.link2, href2)
  }
}
