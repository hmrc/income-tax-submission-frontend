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
  val vcAgentBreadcrumbUrl = "http://localhost:9081/report-quarterly/income-and-expenses/view/agents"
  val vcBreadcrumbUrl = "http://localhost:9081/report-quarterly/income-and-expenses/view"
  val vcBreadcrumb = "Income Tax"
  val startPageBreadcrumb = "Update and submit an Income Tax Return"
  val pageTitleText = "Update and submit an Income Tax Return"
  val pageHeadingText = "Update and submit an Income Tax Return"
  val caption = s"6 April ${taxYear - 1} to 5 April $taxYear"
  val useThisServiceText = "Use this service to update and submit an Income Tax Return."
  val newServiceText = "This is a new service. At the moment you can only update information about:"
  val bullet1InterestPaidAgentText = "interest paid to your client in the UK"
  val bullet1InterestPaidIndividualText = "interest paid to you in the UK"
  val bullet2DividendsFromUKText = "dividends from UK companies, trusts and open-ended investment companies"
  val toUpdateIncomeAgentText = "To update your client’s self-employment and property income, you must use your chosen commercial software."
  val toUpdateIncomeIndividualText = "To update your self-employment and property income, you must use your chosen commercial software."
  val continueButtonText = "Continue"
  val continueButtonHref = s"/income-through-software/return/$taxYear/view"

  object Selectors{
    val vcBreadcrumbSelector = "body > div > div.govuk-breadcrumbs > ol > li:nth-child(1) > a"
    val startPageBreadcrumbSelector = "body > div > div.govuk-breadcrumbs > ol > li:nth-child(2)"
    val pageHeading = "#main-content > div > div > header > h1"
    val caption = "#main-content > div > div > header > p"
    val p1 = "#main-content > div > div > div:nth-child(2) > p:nth-child(1)"
    val p2 = "#main-content > div > div > div:nth-child(2) > p:nth-child(2)"
    val bullet1 = "#main-content > div > div > ul > li:nth-child(1)"
    val bullet2 = "#main-content > div > div > ul > li:nth-child(2)"
    val p3 =  "#main-content > div > div > div:nth-child(4) > p"
    val continueButton = "#continue"
  }

  val startPageView: StartPage = app.injector.instanceOf[StartPage]
  
  "Rendering the start page in English" should {

    "render correctly when the user is an individual" should {

      lazy val view: Html = startPageView(isAgent = false, taxYear)(fakeRequest, messages, mockConfig)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      welshToggleCheck("English")
      linkCheck(vcBreadcrumb, Selectors.vcBreadcrumbSelector, vcBreadcrumbUrl)
      textOnPageCheck(startPageBreadcrumb, Selectors.startPageBreadcrumbSelector)
      titleCheck(pageTitleText)
      h1Check(pageHeadingText)
      textOnPageCheck(caption, Selectors.caption)
      textOnPageCheck(useThisServiceText, Selectors.p1)
      textOnPageCheck(newServiceText, Selectors.p2)
      textOnPageCheck(bullet1InterestPaidIndividualText, Selectors.bullet1)
      textOnPageCheck(bullet2DividendsFromUKText, Selectors.bullet2)
      textOnPageCheck(toUpdateIncomeIndividualText, Selectors.p3)
      buttonCheck(continueButtonText, Selectors.continueButton, Some(continueButtonHref))
    }

    "render correctly when the user is an agent" should {

      lazy val view: Html = startPageView(isAgent = true, taxYear)(fakeRequest, messages, mockConfig)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      welshToggleCheck("English")
      linkCheck(vcBreadcrumb, Selectors.vcBreadcrumbSelector, vcAgentBreadcrumbUrl)
      textOnPageCheck(startPageBreadcrumb, Selectors.startPageBreadcrumbSelector)

      titleCheck(pageTitleText)
      h1Check(pageHeadingText)
      textOnPageCheck(caption, Selectors.caption)
      textOnPageCheck(useThisServiceText, Selectors.p1)
      textOnPageCheck(newServiceText, Selectors.p2)
      textOnPageCheck(bullet1InterestPaidAgentText, Selectors.bullet1)
      textOnPageCheck(bullet2DividendsFromUKText, Selectors.bullet2)
      textOnPageCheck(toUpdateIncomeAgentText, Selectors.p3)
      buttonCheck(continueButtonText, Selectors.continueButton, Some(continueButtonHref))
    }
  }

  "Rendering the start page in Welsh" should {

    "render correctly when the user is an individual" should {

      lazy val view: Html = startPageView(isAgent = false, taxYear)(fakeRequest, welshMessages, mockConfig)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      welshToggleCheck("Welsh")
      linkCheck(vcBreadcrumb, Selectors.vcBreadcrumbSelector, vcBreadcrumbUrl)
      textOnPageCheck(startPageBreadcrumb, Selectors.startPageBreadcrumbSelector)
      titleCheck(pageTitleText)
      h1Check(pageHeadingText)
      textOnPageCheck(caption, Selectors.caption)
      textOnPageCheck(useThisServiceText, Selectors.p1)
      textOnPageCheck(newServiceText, Selectors.p2)
      textOnPageCheck(bullet1InterestPaidIndividualText, Selectors.bullet1)
      textOnPageCheck(bullet2DividendsFromUKText, Selectors.bullet2)
      textOnPageCheck(toUpdateIncomeIndividualText, Selectors.p3)
      buttonCheck(continueButtonText, Selectors.continueButton, Some(continueButtonHref))
    }

    "render correctly when the user is an agent" should {

      lazy val view: Html = startPageView(isAgent = true, taxYear)(fakeRequest, welshMessages, mockConfig)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      welshToggleCheck("Welsh")
      linkCheck(vcBreadcrumb, Selectors.vcBreadcrumbSelector, vcAgentBreadcrumbUrl)
      textOnPageCheck(startPageBreadcrumb, Selectors.startPageBreadcrumbSelector)

      titleCheck(pageTitleText)
      h1Check(pageHeadingText)
      textOnPageCheck(caption, Selectors.caption)
      textOnPageCheck(useThisServiceText, Selectors.p1)
      textOnPageCheck(newServiceText, Selectors.p2)
      textOnPageCheck(bullet1InterestPaidAgentText, Selectors.bullet1)
      textOnPageCheck(bullet2DividendsFromUKText, Selectors.bullet2)
      textOnPageCheck(toUpdateIncomeAgentText, Selectors.p3)
      buttonCheck(continueButtonText, Selectors.continueButton, Some(continueButtonHref))
    }
  }

}
