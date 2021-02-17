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

import config.AppConfig
import models.{DividendsModel, IncomeSourcesModel, InterestModel}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.twirl.api.Html
import views.html.OverviewPageView

class OverviewPageViewSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

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
  val task1Heading = "1. Provide updates"
  val task1p1Individual = "Complete the sections that apply to you."
  val task1p1Agent = "Complete the sections that apply to your client."
  val task1DividendsLinkText = "Dividends"
  val task1DividendsLink = s"http://localhost:9308/income-through-software/return/personal-income/$taxYear/dividends/uk-dividends"
  val task1DividendsLinkWithPriorData = s"http://localhost:9308/income-through-software/return/personal-income/$taxYear/dividends/check-your-answers"
  val task1InterestsLinkText = "Interest"
  val task1InterestsLink = s"http://localhost:9308/income-through-software/return/personal-income/$taxYear/interest/untaxed-uk-interest"
  val task1InterestsLinkWithPriorData = s"http://localhost:9308/income-through-software/return/personal-income/$taxYear/interest/check-your-answers"
  val task1NotStarted = "Not started"
  val task1Updated = "Updated"
  val task2Heading = "2. View Tax calculation to date"
  val task2p1Individual = "Provide at least one update before you can view your estimate."
  val task2p1Agent = "Provide at least one update before you can view your client’s estimate."
  val task2ViewEstimateLinkText = "View estimation"
  val task2ViewEstimateLink = s"/income-through-software/return/$taxYear/calculate"
  val task3Heading = "3. Submit return"
  val task3p1Individual = s"You will be able to submit your return from 6 April $taxYearPlusOne after providing your updates."
  val task3p1Agent = s"You will be able to submit your client’s return from 6 April $taxYearPlusOne after providing your client’s updates."

  val vcBreadcrumbSelector = "body > div > div.govuk-breadcrumbs > ol > li:nth-child(1) > a"
  val startPageBreadcrumbSelector = "body > div > div.govuk-breadcrumbs > ol > li:nth-child(2) > a"
  val overviewBreadcrumbSelector = "body > div > div.govuk-breadcrumbs > ol > li:nth-child(3) > a"
  val captionSelector = "#main-content > div > div > span"
  val headerSelector = "#main-content > div > div > h1"
  val task1HeaderSelector = "#main-content > div > div > ol > li:nth-child(1) > h2"
  val task1p1Selector = "#main-content > div > div > ol > li:nth-child(1) > ol > span"
  val task1InterestsSelector = "#interest_link"
  val task1InterestsP2Selector = "#main-content > div > div > ol > li:nth-child(1) > ol > li:nth-child(3) > span.hmrc-status-tag"
  val task1DividendsSelector = "#dividends_link"
  val task1DividendsP2Selector = "#main-content > div > div > ol > li:nth-child(1) > ol > li:nth-child(4) > span.hmrc-status-tag"
  val task2HeaderSelector = "#main-content > div > div > ol > li:nth-child(2) > h2"
  val task2p1Selector = "#main-content > div > div > ol > li:nth-child(2) > ul > span"
  val task2p1ViewEstimateSelector = "#calculation_link"
  val task3HeaderSelector = "#main-content > div > div > ol > li:nth-child(3) > h2"
  val task3p1Selector = "#main-content > div > div > ol > li:nth-child(3) > ul > span"

  val overviewPageView: OverviewPageView = app.injector.instanceOf[OverviewPageView]

  implicit lazy val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("", "")
  implicit lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit lazy val messages: Messages = messagesApi.preferred(fakeRequest)
  implicit lazy val mockConfig: AppConfig = app.injector.instanceOf[AppConfig]

  lazy val agentWithNoPriorDataView: Html = overviewPageView(isAgent = true, None, taxYear)(fakeRequest,messages,mockConfig)
  lazy implicit val agentWithNoPriorData: Document = Jsoup.parse(agentWithNoPriorDataView.body)

  lazy val individualWithNoPriorDataView: Html = overviewPageView(isAgent = false, None, taxYear)(fakeRequest,messages,mockConfig)
  lazy implicit val individualWithNoPriorData: Document = Jsoup.parse(individualWithNoPriorDataView.body)

  lazy val incomeSourcesModel:Option[IncomeSourcesModel] = Some(IncomeSourcesModel(dividendsModel, interestsModel))
  lazy val dividendsModel:Option[DividendsModel] = Some(DividendsModel(Some(100.00), Some(100.00)))
  lazy val interestsModel:Option[Seq[InterestModel]] = Some(Seq(InterestModel("TestName", "TestSource", Some(100.00), Some(100.00))))

  lazy val individualWithPriorDataView: Html = overviewPageView(isAgent = false, incomeSourcesModel, taxYear)(fakeRequest,messages,mockConfig)
  lazy implicit val individualWithPriorData: Document = Jsoup.parse(individualWithPriorDataView.body)

  "The Overview Page with no prior data" should {

    "Have the correct content for an individual" which {

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

      s"has a header of $individualHeading" in {
        individualWithNoPriorData.select(headerSelector).text shouldBe individualHeading
      }

      s"has a caption of $caption" in {
        individualWithNoPriorData.select(captionSelector).text shouldBe caption
      }

      s"has a task 1 heading of $task1Heading" in {
        individualWithNoPriorData.select(task1HeaderSelector).text shouldBe task1Heading
      }

      s"has a task 1 paragraph of $task1p1Individual" in {
        individualWithNoPriorData.select(task1p1Selector).text shouldBe task1p1Individual
      }

      s"has a link for $task1DividendsLinkText" which {
        s"has the text of $task1DividendsLinkText" in {
          individualWithNoPriorData.select(task1DividendsSelector).text shouldBe task1DividendsLinkText
        }

        s"has a href of $task1DividendsLink" in {
          individualWithNoPriorData.select(task1DividendsSelector).attr("href") shouldBe task1DividendsLink
        }
      }

      s"has a paragraph of $task1NotStarted for dividends" in {
        individualWithNoPriorData.select(task1DividendsP2Selector).text shouldBe task1NotStarted
      }

      s"has a link for $task1InterestsLinkText" which {
        s"has the text of $task1InterestsLinkText" in {
          individualWithNoPriorData.select(task1InterestsSelector).text shouldBe task1InterestsLinkText
        }

        s"has a href of $task1InterestsLink" in {
          individualWithNoPriorData.select(task1InterestsSelector).attr("href") shouldBe task1InterestsLink
        }
      }

      s"has a paragraph of $task1NotStarted for interests" in {
        individualWithNoPriorData.select(task1InterestsP2Selector).text shouldBe task1NotStarted
      }

      s"has a task 2 heading of $task2Heading" in {
        individualWithNoPriorData.select(task2HeaderSelector).text shouldBe task2Heading
      }

      s"has a task 2 paragraph of $task2p1Individual" in {
        individualWithNoPriorData.select(task2p1Selector).text shouldBe task2p1Individual
      }

      s"has a task 3 heading of $task3Heading" in {
        individualWithNoPriorData.select(task3HeaderSelector).text shouldBe task3Heading
      }

      s"has a task 3 paragraph of $task3p1Individual" in {
        individualWithNoPriorData.select(task3p1Selector).text shouldBe task3p1Individual
      }
    }

    "Have the correct content for an agent" which {

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

      s"has a header of $agentHeading" in {
        agentWithNoPriorData.select(headerSelector).text shouldBe agentHeading
      }

      s"has a caption of $caption" in {
        agentWithNoPriorData.select(captionSelector).text shouldBe caption
      }

      s"has a task 1 heading of $task1Heading" in {
        agentWithNoPriorData.select(task1HeaderSelector).text shouldBe task1Heading
      }

      s"has a task 1 paragraph of $task1p1Agent" in {
        agentWithNoPriorData.select(task1p1Selector).text shouldBe task1p1Agent
      }

      s"has a link for $task1DividendsLinkText" which {
        s"has the text of $task1DividendsLinkText" in {
          agentWithNoPriorData.select(task1DividendsSelector).text shouldBe task1DividendsLinkText
        }

        s"has a href of $task1DividendsLink" in {
          agentWithNoPriorData.select(task1DividendsSelector).attr("href") shouldBe task1DividendsLink
        }
      }

      s"has a paragraph of $task1NotStarted for dividends" in {
        agentWithNoPriorData.select(task1DividendsP2Selector).text shouldBe task1NotStarted
      }

      s"has a link for $task1InterestsLinkText" which {
        s"has the text of $task1InterestsLinkText" in {
          agentWithNoPriorData.select(task1InterestsSelector).text shouldBe task1InterestsLinkText
        }

        s"has a href of $task1InterestsLink" in {
          agentWithNoPriorData.select(task1InterestsSelector).attr("href") shouldBe task1InterestsLink
        }
      }

      s"has a paragraph of $task1NotStarted for interests" in {
        agentWithNoPriorData.select(task1InterestsP2Selector).text shouldBe task1NotStarted
      }

      s"has a task 2 heading of $task2Heading" in {
        agentWithNoPriorData.select(task2HeaderSelector).text shouldBe task2Heading
      }

      s"has a task 2 paragraph of $task2p1Agent" in {
        agentWithNoPriorData.select(task2p1Selector).text shouldBe task2p1Agent
      }

      s"has a task 3 heading of $task3Heading" in {
        agentWithNoPriorData.select(task3HeaderSelector).text shouldBe task3Heading
      }

      s"has a task 3 paragraph of $task3p1Agent" in {
        agentWithNoPriorData.select(task3p1Selector).text shouldBe task3p1Agent
      }
    }
  }

  "The Overview Page with prior data" should {

    "Have the correct content for an individual with prior data" which {

      s"has a view and change breadcrumb of $vcBreadcrumb" in {
        individualWithNoPriorData.select(vcBreadcrumbSelector).text shouldBe vcBreadcrumb
      }

      s"has a start page breadcrumb of $startPageBreadcrumb" in {
        individualWithNoPriorData.select(startPageBreadcrumbSelector).text shouldBe startPageBreadcrumb
      }

      s"has a overviewBreadcrumb of $overviewBreadcrumb" in {
        individualWithPriorData.select(overviewBreadcrumbSelector).text shouldBe overviewBreadcrumb
      }

      s"has a header of $individualHeading" in {
        individualWithPriorData.select(headerSelector).text shouldBe individualHeading
      }

      s"has a caption of $caption" in {
        individualWithPriorData.select(captionSelector).text shouldBe caption
      }

      s"has a task 1 heading of $task1Heading" in {
        individualWithPriorData.select(task1HeaderSelector).text shouldBe task1Heading
      }

      s"has a task 1 paragraph of $task1p1Individual" in {
        individualWithPriorData.select(task1p1Selector).text shouldBe task1p1Individual
      }

      s"has a link for $task1DividendsLinkText" which {
        s"has the text of $task1DividendsLinkText" in {
          individualWithPriorData.select(task1DividendsSelector).text shouldBe task1DividendsLinkText
        }

        s"has a href of $task1DividendsLinkWithPriorData" in {
          individualWithPriorData.select(task1DividendsSelector).attr("href") shouldBe task1DividendsLinkWithPriorData
        }
      }

      s"has a paragraph of $task1Updated for dividends" in {
        individualWithPriorData.select(task1DividendsP2Selector).text shouldBe task1Updated
      }

      s"has a link for $task1InterestsLinkText" which {
        s"has the text of $task1InterestsLinkText" in {
          individualWithPriorData.select(task1InterestsSelector).text shouldBe task1InterestsLinkText
        }

        s"has a href of $task1InterestsLinkWithPriorData" in {
          individualWithPriorData.select(task1InterestsSelector).attr("href") shouldBe task1InterestsLinkWithPriorData
        }
      }

      s"has a paragraph of $task1Updated for interests" in {
        individualWithPriorData.select(task1InterestsP2Selector).text shouldBe task1Updated
      }

      s"has a task 2 heading of $task2Heading" in {
        individualWithPriorData.select(task2HeaderSelector).text shouldBe task2Heading
      }

      s"has a link for $task2ViewEstimateLinkText" which {
        s"has the text of $task2ViewEstimateLinkText" in {
          individualWithPriorData.select(task2p1ViewEstimateSelector).text shouldBe task2ViewEstimateLinkText
        }
        s"has a href of $task2ViewEstimateLink" in {
          individualWithPriorData.select(task2p1ViewEstimateSelector).attr("href") shouldBe task2ViewEstimateLink
        }
      }

      s"has a task 3 heading of $task3Heading" in {
        individualWithPriorData.select(task3HeaderSelector).text shouldBe task3Heading
      }

      s"has a task 3 paragraph of $task3p1Individual" in {
        individualWithPriorData.select(task3p1Selector).text shouldBe task3p1Individual
      }
    }
  }
}

