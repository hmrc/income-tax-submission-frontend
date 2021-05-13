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
  val vcAgentBreadcrumbUrl = "http://localhost:9081/report-quarterly/income-and-expenses/view/agents/income-tax-account"
  val vcBreadcrumbUrl = "http://localhost:9081/report-quarterly/income-and-expenses/view"
  val vcBreadcrumb = "Income Tax"
  val startPageBreadcrumb = "Update and submit an Income Tax Return"
  val startPageBreadcrumbUrl = s"/income-through-software/return/$taxYear/start"
  val overviewBreadcrumb = "Your Income Tax Return"
  val caption = s"6 April $taxYearMinusOne to 5 April $taxYear"
  val individualHeading = "Your Income Tax Return"
  val agentHeading = "Your client’s Income Tax Return"
  val provideUpdatesText = "1. Provide updates"
  val completeSectionsIndividualText = "Complete the sections that apply to you."
  val completeSectionsAgentText = "Complete the sections that apply to your client."
  val updatedText = "Updated"
  val notStartedText = "Not started"
  val underMaintenance = "Under maintenance"
  val dividendsLinkText = "Dividends"
  val dividendsLink = s"http://localhost:9308/income-through-software/return/personal-income/$taxYear/dividends/dividends-from-uk-companies"
  val dividendsLinkWithPriorData = s"http://localhost:9308/income-through-software/return/personal-income/$taxYear/dividends/check-income-from-dividends"
  val interestsLinkText = "Interest"
  val interestsLink = s"http://localhost:9308/income-through-software/return/personal-income/$taxYear/interest/untaxed-uk-interest"
  val interestsLinkWithPriorData = s"http://localhost:9308/income-through-software/return/personal-income/$taxYear/interest/check-your-answers"
  val employmentLinkText = "Employment"
  val employmentLink = s"http://localhost:9302/income-through-software/return/$taxYear/view"
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
  val completeSectionsSelector = "#main-content > div > div > ol > li:nth-child(1) > ol > p"
  val interestLinkSelector = "#interest_link"
  val interestStatusSelector = "#main-content > div > div > ol > li:nth-child(1) > ol > li:nth-child(3) > span.hmrc-status-tag"
  val dividendsLinkSelector = "#dividends_link"
  val dividendsStatusSelector = "#main-content > div > div > ol > li:nth-child(1) > ol > li:nth-child(4) > span.hmrc-status-tag"
  val employmentLinkSelector = "#employment_link"
  val employmentStatusSelector = "#main-content > div > div > ol > li:nth-child(1) > ol > li:nth-child(5) > span.hmrc-status-tag"
  val viewTaxCalcSelector = "#main-content > div > div > ol > li:nth-child(2) > h2"
  val interestProvideUpdatesSelector = "#main-content > div > div > ol > li.app-task-list__items > p"
  val viewEstimateSelector = "#calculation_link"
  val submitReturnSelector = "#main-content > div > div > ol > li:nth-child(4) > h2"
  val youWillBeAbleSelector = "#main-content > div > div > ol > li:nth-child(4) > ul > p"

  val overviewPageView: OverviewPageView = app.injector.instanceOf[OverviewPageView]
  
  lazy val incomeSourcesModel:Option[IncomeSourcesModel] = Some(IncomeSourcesModel(dividendsModel, interestsModel))
  lazy val dividendsModel:Option[DividendsModel] = Some(DividendsModel(Some(100.00), Some(100.00)))
  lazy val interestsModel:Option[Seq[InterestModel]] = Some(Seq(InterestModel("TestName", "TestSource", Some(100.00), Some(100.00))))

  "The Overview page should render correctly in English" should {

    "Have the correct content when sources are off" which {

      lazy val individualWithNoPriorDataView: Html = overviewPageView(isAgent = false, None, taxYear)(fakeRequest,messages,mockAppConfig)
      lazy implicit val individualWithNoPriorData: Document = Jsoup.parse(individualWithNoPriorDataView.body)

      welshToggleCheck("English")
      linkCheck(vcBreadcrumb, vcBreadcrumbSelector, vcBreadcrumbUrl)
      linkCheck(startPageBreadcrumb, startPageBreadcrumbSelector, startPageBreadcrumbUrl)
      textOnPageCheck(overviewBreadcrumb, overviewBreadcrumbSelector)

      titleCheck(individualHeading)
      h1Check(individualHeading)
      textOnPageCheck(caption, captionSelector)
      textOnPageCheck(provideUpdatesText, dividendsProvideUpdatesSelector)
      textOnPageCheck(completeSectionsIndividualText, completeSectionsSelector)

      "have a dividends section that says under maintenance" which {
        textOnPageCheck(underMaintenance, dividendsStatusSelector)
      }

      "have an interest section that says under maintenance" which {
        textOnPageCheck(underMaintenance, interestStatusSelector)
      }

      "have an employment section that says under maintenance" which {
        textOnPageCheck(underMaintenance, employmentStatusSelector)
      }
      textOnPageCheck(viewTaxCalcText, viewTaxCalcSelector)
      textOnPageCheck(provideUpdateIndividualText, interestProvideUpdatesSelector)
      textOnPageCheck(submitReturnText, submitReturnSelector)
      textOnPageCheck(youWillBeAbleIndividualText, youWillBeAbleSelector)
    }

    "render correctly with no prior data" should {

      "Have the correct content for an individual" which {

        lazy val individualWithNoPriorDataView: Html = overviewPageView(isAgent = false, None, taxYear)(fakeRequest,messages,mockConfig)
        lazy implicit val individualWithNoPriorData: Document = Jsoup.parse(individualWithNoPriorDataView.body)

        welshToggleCheck("English")
        linkCheck(vcBreadcrumb, vcBreadcrumbSelector, vcBreadcrumbUrl)
        linkCheck(startPageBreadcrumb, startPageBreadcrumbSelector, startPageBreadcrumbUrl)
        textOnPageCheck(overviewBreadcrumb, overviewBreadcrumbSelector)

        titleCheck(individualHeading)
        h1Check(individualHeading)
        textOnPageCheck(caption, captionSelector)
        textOnPageCheck(provideUpdatesText, dividendsProvideUpdatesSelector)
        textOnPageCheck(completeSectionsIndividualText, completeSectionsSelector)

        "has a dividends section" which {
          linkCheck(dividendsLinkText, dividendsLinkSelector, dividendsLink)
          textOnPageCheck(notStartedText, dividendsStatusSelector)
        }

        "has an interest section" which {
          linkCheck(interestsLinkText, interestLinkSelector, interestsLink)
          textOnPageCheck(notStartedText, interestStatusSelector)
        }

        "has an employment section" which {
          linkCheck(employmentLinkText, employmentLinkSelector, employmentLink)
          textOnPageCheck(notStartedText, employmentStatusSelector)
        }
        textOnPageCheck(viewTaxCalcText, viewTaxCalcSelector)
        textOnPageCheck(provideUpdateIndividualText, interestProvideUpdatesSelector)
        textOnPageCheck(submitReturnText, submitReturnSelector)
        textOnPageCheck(youWillBeAbleIndividualText, youWillBeAbleSelector)
      }

      "Have the correct content for an agent" which {

        lazy val agentWithNoPriorDataView: Html = overviewPageView(isAgent = true, None, taxYear)(fakeRequest,messages,mockConfig)
        lazy implicit val agentWithNoPriorData: Document = Jsoup.parse(agentWithNoPriorDataView.body)

        welshToggleCheck("English")
        linkCheck(vcBreadcrumb, vcBreadcrumbSelector, vcAgentBreadcrumbUrl)
        linkCheck(startPageBreadcrumb, startPageBreadcrumbSelector, startPageBreadcrumbUrl)
        textOnPageCheck(overviewBreadcrumb, overviewBreadcrumbSelector)

        titleCheck(agentHeading)
        h1Check(agentHeading)
        textOnPageCheck(caption, captionSelector)
        textOnPageCheck(provideUpdatesText, dividendsProvideUpdatesSelector)
        textOnPageCheck(completeSectionsAgentText, completeSectionsSelector)

        "has a dividends section" which {
          linkCheck(dividendsLinkText, dividendsLinkSelector, dividendsLink)
          textOnPageCheck(notStartedText, dividendsStatusSelector)
        }

        "has an interest section" which {
          linkCheck(interestsLinkText, interestLinkSelector, interestsLink)
          textOnPageCheck(notStartedText, interestStatusSelector)
        }

        "has an employment section" which {
          linkCheck(employmentLinkText, employmentLinkSelector, employmentLink)
          textOnPageCheck(notStartedText, employmentStatusSelector)
        }
        textOnPageCheck(viewTaxCalcText, viewTaxCalcSelector)
        textOnPageCheck(provideUpdateAgentText, interestProvideUpdatesSelector)
        textOnPageCheck(submitReturnText, submitReturnSelector)
        textOnPageCheck(youWillBeAbleAgentText, youWillBeAbleSelector)
      }
    }

    "render correctly with prior data" should {

      "Have the correct content for an individual with prior data" which {

        lazy val individualWithPriorDataView: Html = overviewPageView(isAgent = false, incomeSourcesModel, taxYear)(fakeRequest,messages,mockConfig)
        lazy implicit val individualWithPriorData: Document = Jsoup.parse(individualWithPriorDataView.body)

        welshToggleCheck("English")
        linkCheck(vcBreadcrumb, vcBreadcrumbSelector, vcBreadcrumbUrl)
        linkCheck(startPageBreadcrumb, startPageBreadcrumbSelector, startPageBreadcrumbUrl)
        textOnPageCheck(overviewBreadcrumb, overviewBreadcrumbSelector)

        titleCheck(individualHeading)
        h1Check(individualHeading)
        textOnPageCheck(caption, captionSelector)
        textOnPageCheck(provideUpdatesText, dividendsProvideUpdatesSelector)
        textOnPageCheck(completeSectionsIndividualText, completeSectionsSelector)

        "has a dividends section" which {
          linkCheck(dividendsLinkText, dividendsLinkSelector, dividendsLinkWithPriorData)
          textOnPageCheck(updatedText, dividendsStatusSelector)
        }

        "has an interest section" which {
          linkCheck(interestsLinkText, interestLinkSelector, interestsLinkWithPriorData)
          textOnPageCheck(updatedText, interestStatusSelector)
        }

        "has an employment section" which {
          linkCheck(employmentLinkText, employmentLinkSelector, employmentLink)
          textOnPageCheck(updatedText, employmentStatusSelector)
        }

        textOnPageCheck(viewTaxCalcText, viewTaxCalcSelector)
        linkCheck(viewEstimateLinkText, viewEstimateSelector, viewEstimateLink)
        textOnPageCheck(submitReturnText, submitReturnSelector)
        textOnPageCheck(youWillBeAbleIndividualText, youWillBeAbleSelector)
      }
    }
  }

  "The Overview page should render correctly in Welsh" should {

    "render correctly with no prior data" should {

      "Have the correct content for an individual" which {

        lazy val individualWithNoPriorDataView: Html = overviewPageView(isAgent = false, None, taxYear)(fakeRequest,welshMessages,mockConfig)
        lazy implicit val individualWithNoPriorData: Document = Jsoup.parse(individualWithNoPriorDataView.body)

        welshToggleCheck("Welsh")
        linkCheck(vcBreadcrumb, vcBreadcrumbSelector, vcBreadcrumbUrl)
        linkCheck(startPageBreadcrumb, startPageBreadcrumbSelector, startPageBreadcrumbUrl)
        textOnPageCheck(overviewBreadcrumb, overviewBreadcrumbSelector)

        titleCheck(individualHeading)
        h1Check(individualHeading)
        textOnPageCheck(caption, captionSelector)
        textOnPageCheck(provideUpdatesText, dividendsProvideUpdatesSelector)
        textOnPageCheck(completeSectionsIndividualText, completeSectionsSelector)

        "has a dividends section" which {
          linkCheck(dividendsLinkText, dividendsLinkSelector, dividendsLink)
          textOnPageCheck(notStartedText, dividendsStatusSelector)
        }

        "has an interest section" which {
          linkCheck(interestsLinkText, interestLinkSelector, interestsLink)
          textOnPageCheck(notStartedText, interestStatusSelector)
        }

        "has an employment section" which {
          linkCheck(employmentLinkText, employmentLinkSelector, employmentLink)
          textOnPageCheck(notStartedText, employmentStatusSelector)
        }
        textOnPageCheck(viewTaxCalcText, viewTaxCalcSelector)
        textOnPageCheck(provideUpdateIndividualText, interestProvideUpdatesSelector)
        textOnPageCheck(submitReturnText, submitReturnSelector)
        textOnPageCheck(youWillBeAbleIndividualText, youWillBeAbleSelector)
      }

      "Have the correct content for an agent" which {

        lazy val agentWithNoPriorDataView: Html = overviewPageView(isAgent = true, None, taxYear)(fakeRequest,welshMessages,mockConfig)
        lazy implicit val agentWithNoPriorData: Document = Jsoup.parse(agentWithNoPriorDataView.body)

        welshToggleCheck("Welsh")
        linkCheck(vcBreadcrumb, vcBreadcrumbSelector, vcAgentBreadcrumbUrl)
        linkCheck(startPageBreadcrumb, startPageBreadcrumbSelector, startPageBreadcrumbUrl)
        textOnPageCheck(overviewBreadcrumb, overviewBreadcrumbSelector)

        titleCheck(agentHeading)
        h1Check(agentHeading)
        textOnPageCheck(caption, captionSelector)
        textOnPageCheck(provideUpdatesText, dividendsProvideUpdatesSelector)
        textOnPageCheck(completeSectionsAgentText, completeSectionsSelector)

        "has a dividends section" which {
          linkCheck(dividendsLinkText, dividendsLinkSelector, dividendsLink)
          textOnPageCheck(notStartedText, dividendsStatusSelector)
        }

        "has an interest section" which {
          linkCheck(interestsLinkText, interestLinkSelector, interestsLink)
          textOnPageCheck(notStartedText, interestStatusSelector)
        }

        "has an employment section" which {
          linkCheck(employmentLinkText, employmentLinkSelector, employmentLink)
          textOnPageCheck(notStartedText, employmentStatusSelector)
        }
        textOnPageCheck(viewTaxCalcText, viewTaxCalcSelector)
        textOnPageCheck(provideUpdateAgentText, interestProvideUpdatesSelector)
        textOnPageCheck(submitReturnText, submitReturnSelector)
        textOnPageCheck(youWillBeAbleAgentText, youWillBeAbleSelector)
      }
    }

    "render correctly with prior data" should {

      "Have the correct content for an individual with prior data" which {

        lazy val individualWithPriorDataView: Html = overviewPageView(isAgent = false, incomeSourcesModel, taxYear)(fakeRequest,welshMessages,mockConfig)
        lazy implicit val individualWithPriorData: Document = Jsoup.parse(individualWithPriorDataView.body)

        welshToggleCheck("Welsh")
        linkCheck(vcBreadcrumb, vcBreadcrumbSelector, vcBreadcrumbUrl)
        linkCheck(startPageBreadcrumb, startPageBreadcrumbSelector, startPageBreadcrumbUrl)
        textOnPageCheck(overviewBreadcrumb, overviewBreadcrumbSelector)

        titleCheck(individualHeading)
        h1Check(individualHeading)
        textOnPageCheck(caption, captionSelector)
        textOnPageCheck(provideUpdatesText, dividendsProvideUpdatesSelector)
        textOnPageCheck(completeSectionsIndividualText, completeSectionsSelector)

        "has a dividends section" which {
          linkCheck(dividendsLinkText, dividendsLinkSelector, dividendsLinkWithPriorData)
          textOnPageCheck(updatedText, dividendsStatusSelector)
        }

        "has an interest section" which {
          linkCheck(interestsLinkText, interestLinkSelector, interestsLinkWithPriorData)
          textOnPageCheck(updatedText, interestStatusSelector)
        }

        "has an employment section" which {
          linkCheck(employmentLinkText, employmentLinkSelector, employmentLink)
          textOnPageCheck(updatedText, employmentStatusSelector)
        }

        textOnPageCheck(viewTaxCalcText, viewTaxCalcSelector)
        linkCheck(viewEstimateLinkText, viewEstimateSelector, viewEstimateLink)
        textOnPageCheck(submitReturnText, submitReturnSelector)
        textOnPageCheck(youWillBeAbleIndividualText, youWillBeAbleSelector)
      }
    }
  }
}

