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
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import views.html.StartPage
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.twirl.api.Html
import utils.ViewTest


class StartPageViewSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with ViewTest{

  val taxYear = 2022
  val vcAgentBreadcrumbUrl = "http://localhost:9081/report-quarterly/income-and-expenses/view/client"
  val vcBreadcrumbUrl = "http://localhost:9081/report-quarterly/income-and-expenses/view"
  val vcBreadcrumb = "Income Tax"
  val startPageBreadcrumb = "Update and submit an Income Tax Return"
  val pageHeadingText = "Update and submit an Income Tax Return"
  val p1Text = "Use this service to update and submit an Income Tax Return."
  val p2Text = "This is a new service. At the moment you can only update information about:"
  val bullet1AgentText = "interest paid to your client in the UK"
  val bullet1IndividualText = "interest paid to you in the UK"
  val bullet2Text = "dividends from UK companies, trusts and open-ended investment companies"
  val p3AgentText = "To update your client’s self-employment and property income, you must use your chosen commercial software."
  val p3IndividualText = "To update your self-employment and property income, you must use your chosen commercial software."
  val continueButtonText = "Continue"
  val continueButtonHref = s"/income-through-software/return/$taxYear/view"

  object Selectors{
    val vcBreadcrumbSelector = "body > div > div.govuk-breadcrumbs > ol > li:nth-child(1) > a"
    val startPageBreadcrumbSelector = "body > div > div.govuk-breadcrumbs > ol > li:nth-child(2)"
    val pageHeading = "#main-content > div > div > header > h1"
    val p1 = "#main-content > div > div > div:nth-child(2) > p:nth-child(1)"
    val p2 = "#main-content > div > div > div:nth-child(2) > p:nth-child(2)"
    val bullet1 = "#main-content > div > div > ul > li:nth-child(1)"
    val bullet2 = "#main-content > div > div > ul > li:nth-child(2)"
    val p3 =  "#main-content > div > div > div:nth-child(4) > p"
    val continueButton = "#continue"
  }

  val startPageView: StartPage = app.injector.instanceOf[StartPage]

  "Rendering the start page when the user is an individual" should {

    lazy val view: Html = startPageView(isAgent = false, taxYear)(fakeRequest,messages,mockConfig)
    lazy implicit val document: Document = Jsoup.parse(view.body)

    s"has a view and change breadcrumb of $vcBreadcrumb" in {
      elementText(Selectors.vcBreadcrumbSelector) shouldBe vcBreadcrumb
      document.select(Selectors.vcBreadcrumbSelector).attr("href") shouldBe vcBreadcrumbUrl
    }

    s"has a start page breadcrumb of $startPageBreadcrumb" in {
      elementText(Selectors.startPageBreadcrumbSelector) shouldBe startPageBreadcrumb
    }

    s"has a title of $pageHeadingText" in {
      document.title() shouldBe s"$pageHeadingText - $serviceName - $govUkExtension"
    }

    s"have a page heading of '$pageHeadingText'" in {
      elementText(Selectors.pageHeading) shouldBe pageHeadingText
    }

    s"have a 1st paragraph of '$p1Text.'" in {
      elementText(Selectors.p1) shouldBe p1Text
    }

    s"have a 2nd paragraph of '$p2Text'" in {
      elementText(Selectors.p2) shouldBe p2Text
    }

    s"have a 1st bullet point of '$bullet1IndividualText'" in {
      elementText(Selectors.bullet1) shouldBe bullet1IndividualText
    }

    s"have a 2nd bullet point of '$bullet2Text'" in {
      elementText(Selectors.bullet2) shouldBe bullet2Text
    }

    s"have a 3rd Paragraph of '$p3IndividualText'" in {
      elementText(Selectors.p3) shouldBe p3IndividualText
    }

    s"have a continue button" which {
      s"has the text '$continueButtonText'" in {
        elementText(Selectors.continueButton) shouldBe continueButtonText
      }
      s"has a href to '$continueButtonHref'" in {
        element(Selectors.continueButton).attr("href") shouldBe continueButtonHref
      }
    }
  }

  "Rendering the start page when the user is an agent" should {

    lazy val view: Html = startPageView(isAgent = true, taxYear)(fakeRequest,messages,mockConfig)
    lazy implicit val document: Document = Jsoup.parse(view.body)

    s"has a view and change breadcrumb of $vcBreadcrumb" in {
      elementText(Selectors.vcBreadcrumbSelector) shouldBe vcBreadcrumb
      document.select(Selectors.vcBreadcrumbSelector).attr("href") shouldBe vcAgentBreadcrumbUrl
    }

    s"has a start page breadcrumb of $startPageBreadcrumb" in {
      elementText(Selectors.startPageBreadcrumbSelector) shouldBe startPageBreadcrumb
    }

    s"has a title of $pageHeadingText" in {
      document.title() shouldBe s"$pageHeadingText - $serviceName - $govUkExtension"
    }

    s"have a page heading of '$pageHeadingText'" in {
      elementText(Selectors.pageHeading) shouldBe pageHeadingText
    }

    s"have a 1st paragraph of '$p1Text.'" in {
      elementText(Selectors.p1) shouldBe p1Text
    }

    s"have a 2nd paragraph of '$p2Text'" in {
      elementText(Selectors.p2) shouldBe p2Text
    }

    s"have a 1st bullet point of '$bullet1AgentText'" in {
      elementText(Selectors.bullet1) shouldBe bullet1AgentText
    }

    s"have a 2nd bullet point of '$bullet2Text'" in {
      elementText(Selectors.bullet2) shouldBe bullet2Text
    }

    s"have a 3rd Paragraph of '$p3AgentText'" in {
      elementText(Selectors.p3) shouldBe p3AgentText
    }

    s"have a continue button" which {
      s"has the text '$continueButtonText'" in {
        elementText(Selectors.continueButton) shouldBe continueButtonText
      }
      s"has a href to '$continueButtonHref'" in {
        element(Selectors.continueButton).attr("href") shouldBe continueButtonHref
      }
    }
  }
}
