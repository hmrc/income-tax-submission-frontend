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

import models.{DividendsModel, IncomeSourcesModel, InterestModel}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.twirl.api.Html
import views.html.OverviewPageView
import utils.ViewTest

class OverviewPageViewSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with ViewTest {

  val taxYear = 2080
  val taxYearMinusOne: Int = taxYear - 1
  val taxYearPlusOne: Int = taxYear + 1
  val vcAgentBreadcrumbUrl = "http://localhost:9081/report-quarterly/income-and-expenses/view/client"
  val vcBreadcrumbUrl = "http://localhost:9081/report-quarterly/income-and-expenses/view"
  val vcBreadcrumb = "Income Tax"
  val startPageBreadcrumb = "Update and submit an Income Tax Return"
  val overviewBreadcrumb = "Your Income Tax Return"
  val caption = s"$taxYearMinusOne to $taxYear Income Tax"
  val individualHeading = "Your Income Tax Return"
  val agentHeading = "Your client’s Income Tax Return"
  val provideUpdatesText = "1. Provide updates"
  val completeSectionsIndividualText = "Complete the sections that apply to you."
  val completeSectionsAgentText = "Complete the sections that apply to your client."
  val dividendsLinkText = "Dividends"
  val dividendsLink = s"http://localhost:9308/income-through-software/return/personal-income/$taxYear/dividends/uk-dividends"
  val dividendsLinkWithPriorData = s"http://localhost:9308/income-through-software/return/personal-income/$taxYear/dividends/check-your-answers"
  val interestsLinkText = "Interest"
  val interestsLink = s"http://localhost:9308/income-through-software/return/personal-income/$taxYear/interest/untaxed-uk-interest"
  val interestsLinkWithPriorData = s"http://localhost:9308/income-through-software/return/personal-income/$taxYear/interest/check-your-answers"
  val notStartedText = "Not started"
  val updatedText = "Updated"
  val viewTaxCalcText = "2. View Tax calculation to date"
  val provideUpdateIndividualText = "Provide at least one update before you can view your estimate."
  val provideUpdateAgentText = "Provide at least one update before you can view your client’s estimate."
  val viewEstimateLinkText = "View estimation"
  val viewEstimateLink = s"/income-through-software/return/$taxYear/calculate"
  val submitReturnText = "3. Submit return"
  val youWillBeAbleIndividualText = s"You will be able to submit your return from 6 April $taxYearPlusOne after providing your updates."
  val youWillBeAbleAgentText = s"You will be able to submit your client’s return from 6 April $taxYearPlusOne after providing your client’s updates."

  val vcBreadcrumbSelector = "body > div > div.govuk-breadcrumbs > ol > li:nth-child(1) > a"
  val startPageBreadcrumbSelector = "body > div > div.govuk-breadcrumbs > ol > li:nth-child(2) > a"
  val overviewBreadcrumbSelector = "body > div > div.govuk-breadcrumbs > ol > li:nth-child(3)"
  val captionSelector = "#main-content > div > div > header > p"
  val headerSelector = "#main-content > div > div > header > h1"
  val dividendsProvideUpdatesSelector = "#main-content > div > div > ol > li:nth-child(1) > h2"
  val completeSectionsSelector = "#main-content > div > div > ol > li:nth-child(1) > ol > span"
  val interestLinkSelector = "#interest_link"
  val interestNotStartedSelector = "#main-content > div > div > ol > li:nth-child(1) > ol > li:nth-child(3) > span.hmrc-status-tag"
  val dividendsLinkSelector = "#dividends_link"
  val dividendsNotStartedSelector = "#main-content > div > div > ol > li:nth-child(1) > ol > li:nth-child(4) > span.hmrc-status-tag"
  val viewTaxCalcSelector = "#main-content > div > div > ol > li:nth-child(2) > h2"
  val interestProvideUpdatesSelector = "#main-content > div > div > ol > li:nth-child(2) > ul > span"
  val viewEstimateSelector = "#calculation_link"
  val submitReturnSelector = "#main-content > div > div > ol > li:nth-child(3) > h2"
  val youWillBeAbleSelector = "#main-content > div > div > ol > li:nth-child(3) > ul > span"

  val overviewPageView: OverviewPageView = app.injector.instanceOf[OverviewPageView]

  lazy val agentWithNoPriorDataView: Html = overviewPageView(isAgent = true, None, taxYear)(fakeRequest,messages,mockConfig)
  lazy val individualWithNoPriorDataView: Html = overviewPageView(isAgent = false, None, taxYear)(fakeRequest,messages,mockConfig)
  lazy val individualWithPriorDataView: Html = overviewPageView(isAgent = false, incomeSourcesModel, taxYear)(fakeRequest,messages,mockConfig)

  lazy val incomeSourcesModel:Option[IncomeSourcesModel] = Some(IncomeSourcesModel(dividendsModel, interestsModel))
  lazy val dividendsModel:Option[DividendsModel] = Some(DividendsModel(Some(100.00), Some(100.00)))
  lazy val interestsModel:Option[Seq[InterestModel]] = Some(Seq(InterestModel("TestName", "TestSource", Some(100.00), Some(100.00))))

  "The Overview Page with no prior data" should {

    "Have the correct content for an individual" which {

      lazy implicit val individualWithNoPriorData: Document = Jsoup.parse(individualWithNoPriorDataView.body)

      s"has a view and change breadcrumb of $vcBreadcrumb" in {
        individualWithNoPriorData.select(vcBreadcrumbSelector).text shouldBe vcBreadcrumb
        individualWithNoPriorData.select(vcBreadcrumbSelector).attr("href") shouldBe vcBreadcrumbUrl
      }

      s"has a start page breadcrumb of $startPageBreadcrumb" in {
        individualWithNoPriorData.select(startPageBreadcrumbSelector).text shouldBe startPageBreadcrumb
      }

      s"has a overview breadcrumb of $overviewBreadcrumb" in {
        individualWithNoPriorData.select(overviewBreadcrumbSelector).text shouldBe overviewBreadcrumb
      }

      titleCheck(individualHeading)
      h1Check(individualHeading)
      textOnPageCheck(caption, captionSelector)
      textOnPageCheck(provideUpdatesText, dividendsProvideUpdatesSelector)
      textOnPageCheck(completeSectionsIndividualText, completeSectionsSelector)
      linkCheck(dividendsLinkText, dividendsLinkSelector, dividendsLink)
      textOnPageCheck(notStartedText, dividendsNotStartedSelector)
      linkCheck(interestsLinkText, interestLinkSelector, interestsLink)
      textOnPageCheck(notStartedText, interestNotStartedSelector)
      textOnPageCheck(viewTaxCalcText, viewTaxCalcSelector)
      textOnPageCheck(provideUpdateIndividualText, interestProvideUpdatesSelector)
      textOnPageCheck(submitReturnText, submitReturnSelector)
      textOnPageCheck(youWillBeAbleIndividualText, youWillBeAbleSelector)
    }

    "Have the correct content for an agent" which {

      lazy implicit val agentWithNoPriorData: Document = Jsoup.parse(agentWithNoPriorDataView.body)

      s"has a view and change breadcrumb of $vcBreadcrumb" in {
        agentWithNoPriorData.select(vcBreadcrumbSelector).text shouldBe vcBreadcrumb
        agentWithNoPriorData.select(vcBreadcrumbSelector).attr("href") shouldBe vcAgentBreadcrumbUrl
      }

      s"has a start page breadcrumb of $startPageBreadcrumb" in {
        agentWithNoPriorData.select(startPageBreadcrumbSelector).text shouldBe startPageBreadcrumb
      }

      s"has a overviewBreadcrumb of $overviewBreadcrumb" in {
        agentWithNoPriorData.select(overviewBreadcrumbSelector).text shouldBe overviewBreadcrumb
      }

      titleCheck(agentHeading)
      h1Check(agentHeading)
      textOnPageCheck(caption, captionSelector)
      textOnPageCheck(provideUpdatesText, dividendsProvideUpdatesSelector)
      textOnPageCheck(completeSectionsAgentText, completeSectionsSelector)
      linkCheck(dividendsLinkText, dividendsLinkSelector, dividendsLink)
      textOnPageCheck(notStartedText, dividendsNotStartedSelector)
      linkCheck(interestsLinkText, interestLinkSelector, interestsLink)
      textOnPageCheck(notStartedText, interestNotStartedSelector)
      textOnPageCheck(viewTaxCalcText, viewTaxCalcSelector)
      textOnPageCheck(provideUpdateAgentText, interestProvideUpdatesSelector)
      textOnPageCheck(submitReturnText, submitReturnSelector)
      textOnPageCheck(youWillBeAbleAgentText, youWillBeAbleSelector)
    }
  }

  "The Overview Page with prior data" should {

    "Have the correct content for an individual with prior data" which {

      lazy implicit val individualWithPriorData: Document = Jsoup.parse(individualWithPriorDataView.body)

      s"has a view and change breadcrumb of $vcBreadcrumb" in {
        individualWithPriorData.select(vcBreadcrumbSelector).text shouldBe vcBreadcrumb
        individualWithPriorData.select(vcBreadcrumbSelector).attr("href") shouldBe vcBreadcrumbUrl
      }

      s"has a start page breadcrumb of $startPageBreadcrumb" in {
        individualWithPriorData.select(startPageBreadcrumbSelector).text shouldBe startPageBreadcrumb
      }

      s"has a overviewBreadcrumb of $overviewBreadcrumb" in {
        individualWithPriorData.select(overviewBreadcrumbSelector).text shouldBe overviewBreadcrumb
      }

      titleCheck(individualHeading)
      h1Check(individualHeading)
      textOnPageCheck(caption, captionSelector)
      textOnPageCheck(provideUpdatesText, dividendsProvideUpdatesSelector)
      textOnPageCheck(completeSectionsIndividualText, completeSectionsSelector)
      linkCheck(dividendsLinkText, dividendsLinkSelector, dividendsLinkWithPriorData)
      textOnPageCheck(updatedText, dividendsNotStartedSelector)
      linkCheck(interestsLinkText, interestLinkSelector, interestsLinkWithPriorData)
      textOnPageCheck(updatedText, interestNotStartedSelector)
      textOnPageCheck(viewTaxCalcText, viewTaxCalcSelector)
      linkCheck(viewEstimateLinkText, viewEstimateSelector, viewEstimateLink)
      textOnPageCheck(submitReturnText, submitReturnSelector)
      textOnPageCheck(youWillBeAbleIndividualText, youWillBeAbleSelector)
    }
  }
}

