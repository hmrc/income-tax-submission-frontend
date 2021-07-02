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

package controllers

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import common.SessionValues
import config.{AppConfig, ErrorHandler}
import controllers.predicates.AuthorisedAction
import itUtils.{IntegrationTest, ViewHelpers}
import models.{IncomeSourcesModel, InterestModel}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.Helpers.{OK, SEE_OTHER, status, writeableOf_AnyContentAsEmpty}
import play.api.test.{FakeRequest, Helpers}
import play.twirl.api.Html
import services.{CalculationIdService, IncomeSourcesService}
import uk.gov.hmrc.http.SessionKeys
import views.html.OverviewPageView

import scala.concurrent.Future

class OverviewPageControllerTest extends IntegrationTest with ViewHelpers {

  object ExpectedResults {
    val taxYear = 2022
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
    val updateIncomeTaxReturnTextIndividual = "1. Update your Income Tax Return"
    val updateIncomeTaxReturnTextAgent = "1. Update your client’s Income Tax Return"
    val completeSectionsText = "Fill in the sections you need to update."
    val updatedText = "Updated"
    val notStartedText = "Not started"
    val underMaintenance = "Under maintenance"
    val cannotUpdateText = "Cannot update"
    val dividendsLinkText = "Dividends"
    val dividendsLink = s"http://localhost:9308/income-through-software/return/personal-income/$taxYear/dividends/dividends-from-uk-companies"
    val dividendsLinkWithPriorData = s"http://localhost:9308/income-through-software/return/personal-income/$taxYear/dividends/check-income-from-dividends"
    val interestsLinkText = "Interest"
    val interestsLink = s"http://localhost:9308/income-through-software/return/personal-income/$taxYear/interest/untaxed-uk-interest"
    val interestsLinkWithPriorData = s"http://localhost:9308/income-through-software/return/personal-income/$taxYear/interest/check-interest"
    val employmentLinkText = "Employment"
    val employmentLink = s"http://localhost:9317/income-through-software/return/employment-income/$taxYear/employment-summary"
    val giftAidLinkText = "Donations to charity"
    val viewTaxCalcText = "2. View Tax calculation to date"
    val provideUpdateIndividualText = "Update your Income Tax Return to view your tax estimate."
    val provideUpdateAgentText = "Update your client’s Income Tax Return to view their tax estimate."
    val viewEstimateLinkText = "View estimation"
    val viewEstimateLink = s"/income-through-software/return/$taxYear/calculate"
    val submitReturnText = "3. Submit return"
    val youWillBeAbleIndividualText = s"Update your Income Tax Return and submit it to us after 5 April $taxYearPlusOne."
    val youWillBeAbleAgentText = s"Update your client’s Income Tax Return and submit it to us after 5 April $taxYearPlusOne."
  }

  object Selectors {
    val vcBreadcrumbSelector = "body > div > div.govuk-breadcrumbs > ol > li:nth-child(1) > a"
    val startPageBreadcrumbSelector = "body > div > div.govuk-breadcrumbs > ol > li:nth-child(2) > a"
    val overviewBreadcrumbSelector = "body > div > div.govuk-breadcrumbs > ol > li:nth-child(3)"
    val captionSelector = "#main-content > div > div > header > p"
    val headerSelector = "#main-content > div > div > header > h1"
    val dividendsProvideUpdatesSelector = "#main-content > div > div > ol > li:nth-child(1) > h2"
    val completeSectionsSelector = "#main-content > div > div > ol > li:nth-child(1) > ol > li.govuk-body"
    val interestLinkSelector = "#interest_link"
    val interestStatusSelector = "#main-content > div > div > ol > li:nth-child(1) > ol > li:nth-child(3) > span.hmrc-status-tag"
    val dividendsLinkSelector = "#dividends_link"
    val dividendsStatusSelector = "#main-content > div > div > ol > li:nth-child(1) > ol > li:nth-child(4) > span.hmrc-status-tag"
    val employmentSelector = "#main-content > div > div > ol > li:nth-child(1) > ol > li:nth-child(6) > span.app-task-list__task-name"
    val giftAidLinkSelector = "#giftAid_link"
    val giftAidStatusSelector = "#main-content > div > div > ol > li:nth-child(1) > ol > li:nth-child(5) > span.hmrc-status-tag"
    val employmentLinkSelector = "#employment_link"
    val employmentStatusSelector = "#main-content > div > div > ol > li:nth-child(1) > ol > li:nth-child(6) > span.hmrc-status-tag"
    val viewTaxCalcSelector = "#main-content > div > div > ol > li:nth-child(2) > h2"
    val interestProvideUpdatesSelector = "#main-content > div > div > ol > li.app-task-list__items > p"
    val viewEstimateSelector = "#calculation_link"
    val submitReturnSelector = "#main-content > div > div > ol > li:nth-child(4) > h2"
    val youWillBeAbleSelector = "#main-content > div > div > ol > li:nth-child(4) > ul > li"
  }

  import ExpectedResults._
  import Selectors._

  private val urlPath = s"/income-through-software/return/$taxYear/view"

  lazy val frontendAppConfig: AppConfig = app.injector.instanceOf[AppConfig]

  val controller: OverviewPageController = new OverviewPageController(
    frontendAppConfig,
    mcc,
    scala.concurrent.ExecutionContext.Implicits.global,
    app.injector.instanceOf[IncomeSourcesService],
    app.injector.instanceOf[CalculationIdService],
    app.injector.instanceOf[OverviewPageView],
    app.injector.instanceOf[AuthorisedAction],
    app.injector.instanceOf[ErrorHandler]
  )

  val incomeSourcesModel: IncomeSourcesModel = IncomeSourcesModel(
    dividends = dividendsModel,
    interest = interestsModel,
    giftAid = Some(giftAidModel),
    employment = Some(employmentsModel)
  )

  def stubIncomeSources(incomeSources: IncomeSourcesModel): StubMapping = {
    stubGet("/income-tax-submission-service/income-tax/nino/AA123456A/sources\\?taxYear=2022", OK, Json.toJson(incomeSources).toString())
  }

  def stubIncomeSources: StubMapping = stubGet("/income-tax-submission-service/income-tax/nino/AA123456A/sources\\?taxYear=2022", OK,
    """{
      |	"dividends": {
      |		"ukDividends": 69.99,
      |		"otherUkDividends": 63.99
      |	},
      |	"interest": [{
      |		"accountName": "BANK",
      |		"incomeSourceId": "12345678908765432",
      |		"taxedUkInterest": 44.66,
      |		"untaxedUkInterest": 66.44
      |	}],
      | "giftAid" : {
      |    "gifts":{
      |       "landAndBuildings": 100
      |    }
      | },
      | "employment": {
      |   "hmrcEmploymentData": [
      |     {
      |       "employmentId": "1",
      |       "employerName": "name"
      |     }
      |   ],
      |   "customerEmploymentData": [
      |     {
      |       "employmentId": "2",
      |       "employerName": "name"
      |     }
      |   ]
      | }
      |}""".stripMargin)

  "The Overview page should render correctly in English" when {

    val headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck")

    "request is sent by an INDIVIDUAL" should {
      "render correctly when sources are off" which {
        val request = FakeRequest("GET", urlPath).withHeaders(headers: _*)

        lazy val result: Future[Result] = {
          authoriseIndividual()
          route(appWithSourcesTurnedOff, request).get
        }

        implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

        "returns status of OK(200)" in {
          status(result) shouldBe OK
        }

        welshToggleCheck("English")
        linkCheck(vcBreadcrumb, vcBreadcrumbSelector, vcBreadcrumbUrl)
        linkCheck(startPageBreadcrumb, startPageBreadcrumbSelector, startPageBreadcrumbUrl)
        textOnPageCheck(overviewBreadcrumb, overviewBreadcrumbSelector)

        titleCheck(individualHeading)
        h1Check(individualHeading, "xl")
        textOnPageCheck(caption, captionSelector)
        textOnPageCheck(updateIncomeTaxReturnTextIndividual, dividendsProvideUpdatesSelector)
        textOnPageCheck(completeSectionsText, completeSectionsSelector)

        "have a dividends section that says under maintenance" which {
          textOnPageCheck(underMaintenance, dividendsStatusSelector)
        }

        "have an interest section that says under maintenance" which {
          textOnPageCheck(underMaintenance, interestStatusSelector)
        }

        "have an employment section that says under maintenance" which {
          textOnPageCheck(underMaintenance, employmentStatusSelector)
        }

        "has a donations to charity section" which {
          textOnPageCheck(underMaintenance, giftAidStatusSelector)
        }

        textOnPageCheck(viewTaxCalcText, viewTaxCalcSelector)
        textOnPageCheck(provideUpdateIndividualText, interestProvideUpdatesSelector)
        textOnPageCheck(submitReturnText, submitReturnSelector)
        textOnPageCheck(youWillBeAbleIndividualText, youWillBeAbleSelector)
      }

      "render correctly with no prior data" should {
        val request = FakeRequest("GET", urlPath).withHeaders(headers: _*)

        lazy val result: Future[Result] = {
          authoriseIndividual()
          route(app, request).get
        }

        implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

        "returns status of OK(200)" in {
          status(result) shouldBe OK
        }

        welshToggleCheck("English")
        linkCheck(vcBreadcrumb, vcBreadcrumbSelector, vcBreadcrumbUrl)
        linkCheck(startPageBreadcrumb, startPageBreadcrumbSelector, startPageBreadcrumbUrl)
        textOnPageCheck(overviewBreadcrumb, overviewBreadcrumbSelector)

        titleCheck(individualHeading)
        h1Check(individualHeading, "xl")
        textOnPageCheck(caption, captionSelector)
        textOnPageCheck(updateIncomeTaxReturnTextIndividual, dividendsProvideUpdatesSelector)
        textOnPageCheck(completeSectionsText, completeSectionsSelector)

        "has a dividends section" which {
          linkCheck(dividendsLinkText, dividendsLinkSelector, dividendsLink)
          textOnPageCheck(notStartedText, dividendsStatusSelector)
        }

        "has an interest section" which {
          linkCheck(interestsLinkText, interestLinkSelector, interestsLink)
          textOnPageCheck(notStartedText, interestStatusSelector)
        }

        "has an employment section " which {
          textOnPageCheck(employmentLinkText, employmentSelector)
          textOnPageCheck(cannotUpdateText, employmentStatusSelector)
        }

        "has a donations to charity section" which {
          linkCheck(giftAidLinkText, giftAidLinkSelector, appConfig.personalIncomeTaxGiftAidUrl(taxYear))
          textOnPageCheck(notStartedText, giftAidStatusSelector)
        }

        textOnPageCheck(viewTaxCalcText, viewTaxCalcSelector)
        textOnPageCheck(provideUpdateIndividualText, interestProvideUpdatesSelector)
        textOnPageCheck(submitReturnText, submitReturnSelector)
        textOnPageCheck(youWillBeAbleIndividualText, youWillBeAbleSelector)
      }

      "render correctly with prior data" when {

        "have the status as 'Not Started' for interest" when {

          "interest income source None" which {
            val incomeSources = incomeSourcesModel.copy(interest = None)

            val request = FakeRequest("GET", urlPath).withHeaders(headers: _*)

            lazy val result: Future[Result] = {
              authoriseIndividual()
              stubIncomeSources(incomeSources)
              route(app, request).get
            }

            implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

            "returns status of OK(200)" in {
              status(result) shouldBe OK
            }

            welshToggleCheck("English")
            linkCheck(vcBreadcrumb, vcBreadcrumbSelector, vcBreadcrumbUrl)
            linkCheck(startPageBreadcrumb, startPageBreadcrumbSelector, startPageBreadcrumbUrl)
            textOnPageCheck(overviewBreadcrumb, overviewBreadcrumbSelector)

            titleCheck(individualHeading)
            h1Check(individualHeading, "xl")
            textOnPageCheck(caption, captionSelector)
            textOnPageCheck(updateIncomeTaxReturnTextIndividual, dividendsProvideUpdatesSelector)
            textOnPageCheck(completeSectionsText, completeSectionsSelector)

            "has a dividends section" which {
              linkCheck(dividendsLinkText, dividendsLinkSelector, dividendsLinkWithPriorData)
              textOnPageCheck(updatedText, dividendsStatusSelector)
            }

            "has an interest section with status of 'Not Started'" which {
              linkCheck(interestsLinkText, interestLinkSelector, interestsLink)
              textOnPageCheck(notStartedText, interestStatusSelector)
            }

            "has a donations to charity section" which {
              linkCheck(giftAidLinkText, giftAidLinkSelector, appConfig.personalIncomeTaxGiftAidSubmissionCYAUrl(taxYear))
              textOnPageCheck(updatedText, giftAidStatusSelector)
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

          "interest income source is defined and the untaxed and taxed accounts do not have amounts" which {
            val incomeSources = incomeSourcesModel.copy(interest = Some(Seq(InterestModel("TestName", "TestSource", None, None))))

            val request = FakeRequest("GET", urlPath).withHeaders(headers: _*)

            lazy val result: Future[Result] = {
              authoriseIndividual()
              stubIncomeSources(incomeSources)
              route(app, request).get
            }

            implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

            "returns status of OK(200)" in {
              status(result) shouldBe OK
            }

            welshToggleCheck("English")
            linkCheck(vcBreadcrumb, vcBreadcrumbSelector, vcBreadcrumbUrl)
            linkCheck(startPageBreadcrumb, startPageBreadcrumbSelector, startPageBreadcrumbUrl)
            textOnPageCheck(overviewBreadcrumb, overviewBreadcrumbSelector)

            titleCheck(individualHeading)
            h1Check(individualHeading, "xl")
            textOnPageCheck(caption, captionSelector)
            textOnPageCheck(updateIncomeTaxReturnTextIndividual, dividendsProvideUpdatesSelector)
            textOnPageCheck(completeSectionsText, completeSectionsSelector)

            "has a dividends section" which {
              linkCheck(dividendsLinkText, dividendsLinkSelector, dividendsLinkWithPriorData)
              textOnPageCheck(updatedText, dividendsStatusSelector)
            }

            "has an interest section with status of 'Not Started'" which {
              linkCheck(interestsLinkText, interestLinkSelector, interestsLink)
              textOnPageCheck(notStartedText, interestStatusSelector)
            }

            "has a donations to charity section" which {
              linkCheck(giftAidLinkText, giftAidLinkSelector, appConfig.personalIncomeTaxGiftAidSubmissionCYAUrl(taxYear))
              textOnPageCheck(updatedText, giftAidStatusSelector)
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

        "have the status as 'Updated' for interest" when {
          val interestsModelWithAmounts: Option[Seq[InterestModel]] = Some(Seq(InterestModel("TestName", "TestSource", Some(100.00), Some(100.00))))

          "interest income source is defined and the untaxed and taxed accounts have amounts" which {
            val incomeSources = incomeSourcesModel.copy(interest = interestsModelWithAmounts)

            val request = FakeRequest("GET", urlPath).withHeaders(headers: _*)

            lazy val result: Future[Result] = {
              authoriseIndividual()
              stubIncomeSources(incomeSources)
              route(app, request).get
            }

            implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

            "returns status of OK(200)" in {
              status(result) shouldBe OK
            }

            welshToggleCheck("English")
            linkCheck(vcBreadcrumb, vcBreadcrumbSelector, vcBreadcrumbUrl)
            linkCheck(startPageBreadcrumb, startPageBreadcrumbSelector, startPageBreadcrumbUrl)
            textOnPageCheck(overviewBreadcrumb, overviewBreadcrumbSelector)

            titleCheck(individualHeading)
            h1Check(individualHeading, "xl")
            textOnPageCheck(caption, captionSelector)
            textOnPageCheck(updateIncomeTaxReturnTextIndividual, dividendsProvideUpdatesSelector)
            textOnPageCheck(completeSectionsText, completeSectionsSelector)

            "has a dividends section" which {
              linkCheck(dividendsLinkText, dividendsLinkSelector, dividendsLinkWithPriorData)
              textOnPageCheck(updatedText, dividendsStatusSelector)
            }

            "has an interest section with status of 'Updated'" which {
              linkCheck(interestsLinkText, interestLinkSelector, interestsLinkWithPriorData)
              textOnPageCheck(updatedText, interestStatusSelector)
            }

            "has a donations to charity section" which {
              linkCheck(giftAidLinkText, giftAidLinkSelector, appConfig.personalIncomeTaxGiftAidSubmissionCYAUrl(taxYear))
              textOnPageCheck(updatedText, giftAidStatusSelector)
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

    "request is sent by an AGENT" should {

      "Have the correct content when sources are off" which {
        val request = FakeRequest("GET", urlPath).withHeaders(headers: _*)

        lazy val result: Future[Result] = {
          authoriseAgent()
          route(appWithSourcesTurnedOff, request).get
        }

        implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

        "returns status of OK(200)" in {
          status(result) shouldBe OK
        }

        welshToggleCheck("English")
        linkCheck(vcBreadcrumb, vcBreadcrumbSelector, vcAgentBreadcrumbUrl)
        linkCheck(startPageBreadcrumb, startPageBreadcrumbSelector, startPageBreadcrumbUrl)
        textOnPageCheck(overviewBreadcrumb, overviewBreadcrumbSelector)

        titleCheck(agentHeading)
        h1Check(agentHeading, "xl")
        textOnPageCheck(caption, captionSelector)
        textOnPageCheck(updateIncomeTaxReturnTextAgent, dividendsProvideUpdatesSelector)
        textOnPageCheck(completeSectionsText, completeSectionsSelector)

        "have a dividends section that says under maintenance" which {
          textOnPageCheck(underMaintenance, dividendsStatusSelector)
        }

        "have an interest section that says under maintenance" which {
          textOnPageCheck(underMaintenance, interestStatusSelector)
        }

        "have an employment section that says under maintenance" which {
          textOnPageCheck(underMaintenance, employmentStatusSelector)
        }

        "has a donations to charity section" which {
          textOnPageCheck(underMaintenance, giftAidStatusSelector)
        }

        textOnPageCheck(viewTaxCalcText, viewTaxCalcSelector)
        textOnPageCheck(provideUpdateAgentText, interestProvideUpdatesSelector)
        textOnPageCheck(submitReturnText, submitReturnSelector)
        textOnPageCheck(youWillBeAbleAgentText, youWillBeAbleSelector)
      }

      "render correctly with no prior data" should {
        val request = FakeRequest("GET", urlPath).withHeaders(headers: _*)

        lazy val result: Future[Result] = {
          authoriseAgent()
          route(app, request).get
        }

        implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

        "returns status of OK(200)" in {
          status(result) shouldBe OK
        }

        welshToggleCheck("English")
        linkCheck(vcBreadcrumb, vcBreadcrumbSelector, vcAgentBreadcrumbUrl)
        linkCheck(startPageBreadcrumb, startPageBreadcrumbSelector, startPageBreadcrumbUrl)
        textOnPageCheck(overviewBreadcrumb, overviewBreadcrumbSelector)

        titleCheck(agentHeading)
        h1Check(agentHeading, "xl")
        textOnPageCheck(caption, captionSelector)
        textOnPageCheck(updateIncomeTaxReturnTextAgent, dividendsProvideUpdatesSelector)
        textOnPageCheck(completeSectionsText, completeSectionsSelector)

        "has a dividends section" which {
          linkCheck(dividendsLinkText, dividendsLinkSelector, dividendsLink)
          textOnPageCheck(notStartedText, dividendsStatusSelector)
        }

        "has an interest section" which {
          linkCheck(interestsLinkText, interestLinkSelector, interestsLink)
          textOnPageCheck(notStartedText, interestStatusSelector)
        }

        "has an employment section" which {
          textOnPageCheck(employmentLinkText, employmentSelector)
          textOnPageCheck(cannotUpdateText, employmentStatusSelector)
        }

        "has a donations to charity section" which {
          linkCheck(giftAidLinkText, giftAidLinkSelector, appConfig.personalIncomeTaxGiftAidUrl(taxYear))
          textOnPageCheck(notStartedText, giftAidStatusSelector)
        }

        textOnPageCheck(viewTaxCalcText, viewTaxCalcSelector)
        textOnPageCheck(provideUpdateAgentText, interestProvideUpdatesSelector)
        textOnPageCheck(submitReturnText, submitReturnSelector)
        textOnPageCheck(youWillBeAbleAgentText, youWillBeAbleSelector)
      }

      "render correctly with prior data" should {
        val request = FakeRequest("GET", urlPath).withHeaders(headers: _*)

        lazy val result: Future[Result] = {
          authoriseAgent()
          stubIncomeSources
          route(app, request).get
        }

        implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

        "returns status of OK(200)" in {
          status(result) shouldBe OK
        }

        welshToggleCheck("English")
        linkCheck(vcBreadcrumb, vcBreadcrumbSelector, vcAgentBreadcrumbUrl)
        linkCheck(startPageBreadcrumb, startPageBreadcrumbSelector, startPageBreadcrumbUrl)
        textOnPageCheck(overviewBreadcrumb, overviewBreadcrumbSelector)

        titleCheck(agentHeading)
        h1Check(agentHeading, "xl")
        textOnPageCheck(caption, captionSelector)
        textOnPageCheck(updateIncomeTaxReturnTextAgent, dividendsProvideUpdatesSelector)
        textOnPageCheck(completeSectionsText, completeSectionsSelector)

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

        "has a donations to charity section" which {
          linkCheck(giftAidLinkText, giftAidLinkSelector, appConfig.personalIncomeTaxGiftAidSubmissionCYAUrl(taxYear))
          textOnPageCheck(updatedText, giftAidStatusSelector)
        }

        textOnPageCheck(viewTaxCalcText, viewTaxCalcSelector)
        linkCheck(viewEstimateLinkText, viewEstimateSelector, viewEstimateLink)
        textOnPageCheck(submitReturnText, submitReturnSelector)
        textOnPageCheck(youWillBeAbleAgentText, youWillBeAbleSelector)
      }
    }
  }

  "The Overview page should render correctly in Welsh" when {

    val headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck", HeaderNames.ACCEPT_LANGUAGE -> "cy")

    "request is sent by an INDIVIDUAL" should {
      "render correctly when sources are off" which {
        val request = FakeRequest("GET", urlPath).withHeaders(headers: _*)

        lazy val result: Future[Result] = {
          authoriseIndividual()
          route(appWithSourcesTurnedOff, request).get
        }

        implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

        "returns status of OK(200)" in {
          status(result) shouldBe OK
        }

        welshToggleCheck("Welsh")
        linkCheck(vcBreadcrumb, vcBreadcrumbSelector, vcBreadcrumbUrl)
        linkCheck(startPageBreadcrumb, startPageBreadcrumbSelector, startPageBreadcrumbUrl)
        textOnPageCheck(overviewBreadcrumb, overviewBreadcrumbSelector)

        titleCheck(individualHeading)
        h1Check(individualHeading, "xl")
        textOnPageCheck(caption, captionSelector)
        textOnPageCheck(updateIncomeTaxReturnTextIndividual, dividendsProvideUpdatesSelector)
        textOnPageCheck(completeSectionsText, completeSectionsSelector)

        "have a dividends section that says under maintenance" which {
          textOnPageCheck(underMaintenance, dividendsStatusSelector)
        }

        "have an interest section that says under maintenance" which {
          textOnPageCheck(underMaintenance, interestStatusSelector)
        }

        "have an employment section that says under maintenance" which {
          textOnPageCheck(underMaintenance, employmentStatusSelector)
        }

        "has a donations to charity section" which {
          textOnPageCheck(underMaintenance, giftAidStatusSelector)
        }

        textOnPageCheck(viewTaxCalcText, viewTaxCalcSelector)
        textOnPageCheck(provideUpdateIndividualText, interestProvideUpdatesSelector)
        textOnPageCheck(submitReturnText, submitReturnSelector)
        textOnPageCheck(youWillBeAbleIndividualText, youWillBeAbleSelector)
      }

      "render correctly with no prior data" should {
        val request = FakeRequest("GET", urlPath).withHeaders(headers: _*)

        lazy val result: Future[Result] = {
          authoriseIndividual()
          route(app, request).get
        }

        implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

        "returns status of OK(200)" in {
          status(result) shouldBe OK
        }

        welshToggleCheck("Welsh")
        linkCheck(vcBreadcrumb, vcBreadcrumbSelector, vcBreadcrumbUrl)
        linkCheck(startPageBreadcrumb, startPageBreadcrumbSelector, startPageBreadcrumbUrl)
        textOnPageCheck(overviewBreadcrumb, overviewBreadcrumbSelector)

        titleCheck(individualHeading)
        h1Check(individualHeading, "xl")
        textOnPageCheck(caption, captionSelector)
        textOnPageCheck(updateIncomeTaxReturnTextIndividual, dividendsProvideUpdatesSelector)
        textOnPageCheck(completeSectionsText, completeSectionsSelector)

        "has a dividends section" which {
          linkCheck(dividendsLinkText, dividendsLinkSelector, dividendsLink)
          textOnPageCheck(notStartedText, dividendsStatusSelector)
        }

        "has an interest section" which {
          linkCheck(interestsLinkText, interestLinkSelector, interestsLink)
          textOnPageCheck(notStartedText, interestStatusSelector)
        }

        "has an employment section " which {
          textOnPageCheck(employmentLinkText, employmentSelector)
          textOnPageCheck(cannotUpdateText, employmentStatusSelector)
        }

        "has a donations to charity section" which {
          linkCheck(giftAidLinkText, giftAidLinkSelector, appConfig.personalIncomeTaxGiftAidUrl(taxYear))
          textOnPageCheck(notStartedText, giftAidStatusSelector)
        }

        textOnPageCheck(viewTaxCalcText, viewTaxCalcSelector)
        textOnPageCheck(provideUpdateIndividualText, interestProvideUpdatesSelector)
        textOnPageCheck(submitReturnText, submitReturnSelector)
        textOnPageCheck(youWillBeAbleIndividualText, youWillBeAbleSelector)
      }

      "render correctly with prior data" should {
        val request = FakeRequest("GET", urlPath).withHeaders(headers: _*)

        lazy val result: Future[Result] = {
          authoriseIndividual()
          stubIncomeSources
          route(app, request).get
        }

        implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

        "returns status of OK(200)" in {
          status(result) shouldBe OK
        }

        welshToggleCheck("Welsh")
        linkCheck(vcBreadcrumb, vcBreadcrumbSelector, vcBreadcrumbUrl)
        linkCheck(startPageBreadcrumb, startPageBreadcrumbSelector, startPageBreadcrumbUrl)
        textOnPageCheck(overviewBreadcrumb, overviewBreadcrumbSelector)

        titleCheck(individualHeading)
        h1Check(individualHeading, "xl")
        textOnPageCheck(caption, captionSelector)
        textOnPageCheck(updateIncomeTaxReturnTextIndividual, dividendsProvideUpdatesSelector)
        textOnPageCheck(completeSectionsText, completeSectionsSelector)

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

        "has a donations to charity section" which {
          linkCheck(giftAidLinkText, giftAidLinkSelector, appConfig.personalIncomeTaxGiftAidSubmissionCYAUrl(taxYear))
          textOnPageCheck(updatedText, giftAidStatusSelector)
        }

        textOnPageCheck(viewTaxCalcText, viewTaxCalcSelector)
        linkCheck(viewEstimateLinkText, viewEstimateSelector, viewEstimateLink)
        textOnPageCheck(submitReturnText, submitReturnSelector)
        textOnPageCheck(youWillBeAbleIndividualText, youWillBeAbleSelector)
      }
    }

    "request is sent by an AGENT" should {

      "Have the correct content when sources are off" which {
        val request = FakeRequest("GET", urlPath).withHeaders(headers: _*)

        lazy val result: Future[Result] = {
          authoriseAgent()
          route(appWithSourcesTurnedOff, request).get
        }

        implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

        "returns status of OK(200)" in {
          status(result) shouldBe OK
        }

        welshToggleCheck("Welsh")
        linkCheck(vcBreadcrumb, vcBreadcrumbSelector, vcAgentBreadcrumbUrl)
        linkCheck(startPageBreadcrumb, startPageBreadcrumbSelector, startPageBreadcrumbUrl)
        textOnPageCheck(overviewBreadcrumb, overviewBreadcrumbSelector)

        titleCheck(agentHeading)
        h1Check(agentHeading, "xl")
        textOnPageCheck(caption, captionSelector)
        textOnPageCheck(updateIncomeTaxReturnTextAgent, dividendsProvideUpdatesSelector)
        textOnPageCheck(completeSectionsText, completeSectionsSelector)

        "have a dividends section that says under maintenance" which {
          textOnPageCheck(underMaintenance, dividendsStatusSelector)
        }

        "have an interest section that says under maintenance" which {
          textOnPageCheck(underMaintenance, interestStatusSelector)
        }

        "have an employment section that says under maintenance" which {
          textOnPageCheck(underMaintenance, employmentStatusSelector)
        }

        "has a donations to charity section" which {
          textOnPageCheck(underMaintenance, giftAidStatusSelector)
        }

        textOnPageCheck(viewTaxCalcText, viewTaxCalcSelector)
        textOnPageCheck(provideUpdateAgentText, interestProvideUpdatesSelector)
        textOnPageCheck(submitReturnText, submitReturnSelector)
        textOnPageCheck(youWillBeAbleAgentText, youWillBeAbleSelector)
      }

      "render correctly with no prior data" should {
        val request = FakeRequest("GET", urlPath).withHeaders(headers: _*)

        lazy val result: Future[Result] = {
          authoriseAgent()
          route(app, request).get
        }

        implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

        "returns status of OK(200)" in {
          status(result) shouldBe OK
        }

        welshToggleCheck("Welsh")
        linkCheck(vcBreadcrumb, vcBreadcrumbSelector, vcAgentBreadcrumbUrl)
        linkCheck(startPageBreadcrumb, startPageBreadcrumbSelector, startPageBreadcrumbUrl)
        textOnPageCheck(overviewBreadcrumb, overviewBreadcrumbSelector)

        titleCheck(agentHeading)
        h1Check(agentHeading, "xl")
        textOnPageCheck(caption, captionSelector)
        textOnPageCheck(updateIncomeTaxReturnTextAgent, dividendsProvideUpdatesSelector)
        textOnPageCheck(completeSectionsText, completeSectionsSelector)

        "has a dividends section" which {
          linkCheck(dividendsLinkText, dividendsLinkSelector, dividendsLink)
          textOnPageCheck(notStartedText, dividendsStatusSelector)
        }

        "has an interest section" which {
          linkCheck(interestsLinkText, interestLinkSelector, interestsLink)
          textOnPageCheck(notStartedText, interestStatusSelector)
        }

        "has an employment section" which {
          textOnPageCheck(employmentLinkText, employmentSelector)
          textOnPageCheck(cannotUpdateText, employmentStatusSelector)
        }

        "has a donations to charity section" which {
          linkCheck(giftAidLinkText, giftAidLinkSelector, appConfig.personalIncomeTaxGiftAidUrl(taxYear))
          textOnPageCheck(notStartedText, giftAidStatusSelector)
        }

        textOnPageCheck(viewTaxCalcText, viewTaxCalcSelector)
        textOnPageCheck(provideUpdateAgentText, interestProvideUpdatesSelector)
        textOnPageCheck(submitReturnText, submitReturnSelector)
        textOnPageCheck(youWillBeAbleAgentText, youWillBeAbleSelector)
      }

      "render correctly with prior data" should {
        val request = FakeRequest("GET", urlPath).withHeaders(headers: _*)

        lazy val result: Future[Result] = {
          authoriseAgent()
          stubIncomeSources
          route(app, request).get
        }

        implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

        "returns status of OK(200)" in {
          status(result) shouldBe OK
        }

        welshToggleCheck("Welsh")
        linkCheck(vcBreadcrumb, vcBreadcrumbSelector, vcAgentBreadcrumbUrl)
        linkCheck(startPageBreadcrumb, startPageBreadcrumbSelector, startPageBreadcrumbUrl)
        textOnPageCheck(overviewBreadcrumb, overviewBreadcrumbSelector)

        titleCheck(agentHeading)
        h1Check(agentHeading, "xl")
        textOnPageCheck(caption, captionSelector)
        textOnPageCheck(updateIncomeTaxReturnTextAgent, dividendsProvideUpdatesSelector)
        textOnPageCheck(completeSectionsText, completeSectionsSelector)

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

        "has a donations to charity section" which {
          linkCheck(giftAidLinkText, giftAidLinkSelector, appConfig.personalIncomeTaxGiftAidSubmissionCYAUrl(taxYear))
          textOnPageCheck(updatedText, giftAidStatusSelector)
        }

        textOnPageCheck(viewTaxCalcText, viewTaxCalcSelector)
        linkCheck(viewEstimateLinkText, viewEstimateSelector, viewEstimateLink)
        textOnPageCheck(submitReturnText, submitReturnSelector)
        textOnPageCheck(youWillBeAbleAgentText, youWillBeAbleSelector)
      }
    }
  }

  "Hitting the show endpoint" should {

    s"return an OK (200) and no prior data" when {

      "all auth requirements are met" in {
        val result = {
          authoriseIndividual()
          await(controller.show(taxYear)(fakeRequest.withSession(SessionValues.TAX_YEAR -> "2022")))
        }

        result.header.status shouldBe OK
      }

    }
    s"return an OK (200) and no prior data and a session id" when {

      "all auth requirements are met" in {
        val result = {
          authoriseIndividual()
          await(controller.show(taxYear)(fakeRequest.withSession(SessionValues.TAX_YEAR -> "2022", SessionKeys.sessionId -> "sessionId-0101010101")))
        }

        result.header.status shouldBe OK
      }

    }

    s"return an OK (200) with prior data" when {

      "all auth requirements are met" in {
        val result = {
          stubIncomeSources
          authoriseIndividual()
          await(controller.show(taxYear)(fakeRequest.withSession(SessionValues.TAX_YEAR -> "2022")))
        }

        result.header.status shouldBe OK
      }

    }

    s"return an UNAUTHORISED (401)" when {

      "the confidence level is too low" in {
        val result = {
          stubIncomeSources
          unauthorisedIndividualInsufficientConfidenceLevel()
          await(controller.show(taxYear)(fakeRequest))
        }

        result.header.status shouldBe SEE_OTHER
        result.header.headers shouldBe Map("Location" -> "/income-through-software/return/iv-uplift")
      }

    }

    "redirect to the sign in page" when {

      "it contains the wrong credentials" which {
        lazy val result = {
          unauthorisedIndividualWrongCredentials()
          await(controller.show(taxYear)(fakeRequest))
        }

        "has a status of SEE_OTHER (303)" in {
          result.header.status shouldBe SEE_OTHER
        }

        "has the sign in page redirect link" in {
          result.header.headers("Location") shouldBe appConfig.signInUrl
        }
      }

    }

  }

}
