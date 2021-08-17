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
import play.api.http.Status.SEE_OTHER
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.Helpers.{OK, status, writeableOf_AnyContentAsEmpty}
import play.api.test.{FakeRequest, Helpers}
import services.{CalculationIdService, IncomeSourcesService}
import uk.gov.hmrc.http.SessionKeys
import views.html.OverviewPageView

import scala.concurrent.Future

class OverviewPageControllerISpec extends IntegrationTest with ViewHelpers {

  val taxYear = 2022
  val taxYearEndOfYear: Int = taxYear - 1
  val taxYearMinusOne: Int = taxYear - 1
  val taxYearPlusOne: Int = taxYear + 1
  val taxYearEndOfYearMinusOne: Int = taxYearEndOfYear - 1

  object Links {
    def startPageBreadcrumbUrl(taxYear: Int = taxYear): String = s"/income-through-software/return/$taxYear/start"

    def dividendsLink(taxYear: Int = taxYear): String =
      s"http://localhost:9308/income-through-software/return/personal-income/$taxYear/dividends/dividends-from-uk-companies"

    def dividendsLinkWithPriorData(taxYear: Int = taxYear): String =
      s"http://localhost:9308/income-through-software/return/personal-income/$taxYear/dividends/check-income-from-dividends"

    def interestsLink(taxYear: Int = taxYear): String =
      s"http://localhost:9308/income-through-software/return/personal-income/$taxYear/interest/untaxed-uk-interest"

    def interestsLinkWithPriorData(taxYear: Int = taxYear): String =
      s"http://localhost:9308/income-through-software/return/personal-income/$taxYear/interest/check-interest"

    def employmentLink(taxYear: Int = taxYear): String = s"http://localhost:9317/income-through-software/return/employment-income/$taxYear/employment-summary"

    def newEmploymentLink(taxYear: Int = taxYear): String = s"http://localhost:9317/income-through-software/return/employment-income/$taxYear/add-employment"

    def viewEstimateLink(taxYear: Int = taxYear): String = s"/income-through-software/return/$taxYear/calculate"

    def vcBreadCrumbUrl(isAgent:Boolean): String = if (isAgent) {"http://localhost:9081/report-quarterly/income-and-expenses/view/agents/income-tax-account"}
      else {"http://localhost:9081/report-quarterly/income-and-expenses/view"}

    val endOfYearContinueLink = s"/income-through-software/return/$taxYearEndOfYear/final-calculation"

  }

  object ExpectedIndividualEN extends SpecificExpectedResults {
    val headingExpected = "Your Income Tax Return"
    val updateIncomeTaxReturnText = "1. Update your Income Tax Return"
    def youWillBeAble(taxYear: Int = taxYearPlusOne): String = s"Update your Income Tax Return and submit it to us after 5 April $taxYear."
    val submitReturnHeaderEOY = "3. Submit your Income Tax Return"
    val submitReturnText: String = "If you’ve finished updating your Income Tax Return, you can continue and see your final tax calculation. " +
      "You can check your calculation and then submit your Income Tax Return."
    val provideUpdate: String = "Provide at least one update before you can view your tax calculation to date."
    val ifWeHaveInfo = "If we have information about your income and deductions, we’ll enter it for you. We get this information from:"
    val warning = "Warning Use your software package to update parts of your Income Tax Return that are not on this list."
    val incomeCalcToDate = "This is your Income Tax calculation to date. You will get a final tax calculation when you submit your Income Tax Return."
  }

  object ExpectedAgentEN extends SpecificExpectedResults {
    val headingExpected = "Your client’s Income Tax Return"
    val updateIncomeTaxReturnText = "1. Update your client’s Income Tax Return"
    def youWillBeAble(taxYear: Int = taxYearPlusOne): String = s"Update your client’s Income Tax Return and submit it to us after 5 April $taxYear."
    val submitReturnHeaderEOY = "3. Submit your client’s Income Tax Return"
    val submitReturnText: String = "If you’ve finished updating your client’s Income Tax Return, you can continue and see their final tax calculation. " +
      "Check the calculation and submit the Income Tax Return."
    val provideUpdate: String = "Provide at least one update before you can view your client’s tax calculation to date."
    val ifWeHaveInfo = "If we have information about your client’s income and deductions, we’ll enter it for you. We get this information from:"
    val warning = "Warning Use your software package to update parts of your client’s Income Tax Return that are not on this list."
    val incomeCalcToDate: String = "This is your client’s Income Tax calculation to date. You will get a final tax calculation when " +
      "you submit your client’s Income Tax Return."
  }

  object ExpectedIndividualCY extends SpecificExpectedResults {
    val headingExpected = "Your Income Tax Return"
    val updateIncomeTaxReturnText = "1. Update your Income Tax Return"
    def youWillBeAble(taxYear: Int = taxYearPlusOne): String = s"Update your Income Tax Return and submit it to us after 5 April $taxYear."
    val submitReturnHeaderEOY = "3. Submit your Income Tax Return"
    val submitReturnText: String = "If you’ve finished updating your Income Tax Return, you can continue and see your final tax calculation. " +
      "You can check your calculation and then submit your Income Tax Return."
    val provideUpdate: String = "Provide at least one update before you can view your tax calculation to date."
    val ifWeHaveInfo = "If we have information about your income and deductions, we’ll enter it for you. We get this information from:"
    val warning = "Warning Use your software package to update parts of your Income Tax Return that are not on this list."
    val incomeCalcToDate = "This is your Income Tax calculation to date. You will get a final tax calculation when you submit your Income Tax Return."
  }

  object ExpectedAgentCY extends SpecificExpectedResults {
    val headingExpected = "Your client’s Income Tax Return"
    val updateIncomeTaxReturnText = "1. Update your client’s Income Tax Return"
    def youWillBeAble(taxYear: Int = taxYearPlusOne): String = s"Update your client’s Income Tax Return and submit it to us after 5 April $taxYear."
    val submitReturnHeaderEOY = "3. Submit your client’s Income Tax Return"
    val submitReturnText: String = "If you’ve finished updating your client’s Income Tax Return, you can continue and see their final tax calculation. " +
      "Check the calculation and submit the Income Tax Return."
    val provideUpdate: String = "Provide at least one update before you can view your client’s tax calculation to date."
    val ifWeHaveInfo = "If we have information about your client’s income and deductions, we’ll enter it for you. We get this information from:"
    val warning = "Warning Use your software package to update parts of your client’s Income Tax Return that are not on this list."
    val incomeCalcToDate: String = "This is your client’s Income Tax calculation to date. You will get a final tax calculation when " +
      "you submit your client’s Income Tax Return."
  }

  trait SpecificExpectedResults {
    val headingExpected: String
    val updateIncomeTaxReturnText: String
    val provideUpdate: String
    def youWillBeAble(taxYear: Int): String
    val submitReturnHeaderEOY: String
    val submitReturnText: String
    val ifWeHaveInfo: String
    val warning: String
    val incomeCalcToDate: String
  }

  trait CommonExpectedResults {
    val vcBreadcrumb: String
    val startPageBreadcrumb: String
    val overviewBreadcrumb: String
    def caption(taxYearMinusOne: Int, taxYear: Int): String
    val completeSectionsText: String
    val updatedText: String
    val notStartedText: String
    val underMaintenance: String
    val cannotUpdateText: String
    val dividendsLinkText: String
    val interestsLinkText: String
    val employmentLinkText: String
    val giftAidLinkText: String
    val viewTaxCalcText: String
    val viewTaxCalcLink: String
    val submitReturnText: String
    val ourRecords: String
    val softwarePackage: String
    val continue: String
  }

  object CommonExpectedEN extends CommonExpectedResults {
    val vcBreadcrumb = "Income Tax"
    val startPageBreadcrumb = "Update and submit an Income Tax Return"
    val overviewBreadcrumb = "Your Income Tax Return"
    def caption(taxYearMinusOne: Int, taxYear: Int): String = s"6 April $taxYearMinusOne to 5 April $taxYear"
    val completeSectionsText = "Fill in the sections you need to update."
    val updatedText = "Updated"
    val notStartedText = "Not started"
    val underMaintenance = "Under maintenance"
    val cannotUpdateText = "Cannot update"
    val dividendsLinkText = "Dividends"
    val interestsLinkText = "Interest"
    val employmentLinkText = "PAYE Employment"
    val giftAidLinkText = "Donations to charity"
    val viewTaxCalcText = "2. View tax calculation to date"
    val viewTaxCalcLink = "View tax calculation to date"
    val submitReturnText = "3. Submit return"
    val ourRecords = "our records"
    val softwarePackage = "your software package"
    val continue = "continue"
  }

  object CommonExpectedCY extends CommonExpectedResults {
    val vcBreadcrumb = "Income Tax"
    val startPageBreadcrumb = "Update and submit an Income Tax Return"
    val overviewBreadcrumb = "Your Income Tax Return"
    def caption(taxYearMinusOne: Int, taxYear: Int): String = s"6 April $taxYearMinusOne to 5 April $taxYear"
    val completeSectionsText = "Fill in the sections you need to update."
    val updatedText = "Updated"
    val notStartedText = "Not started"
    val underMaintenance = "Under maintenance"
    val cannotUpdateText = "Cannot update"
    val dividendsLinkText = "Dividends"
    val interestsLinkText = "Interest"
    val employmentLinkText = "PAYE Employment"
    val giftAidLinkText = "Donations to charity"
    val viewTaxCalcText = "2. View tax calculation to date"
    val viewTaxCalcLink = "View tax calculation to date"
    val submitReturnText = "3. Submit return"
    val ourRecords = "our records"
    val softwarePackage = "your software package"
    val continue = "continue"
  }

  object Selectors {
    val vcBreadcrumbSelector = "body > div > div.govuk-breadcrumbs > ol > li:nth-child(1) > a"
    val startPageBreadcrumbSelector = "body > div > div.govuk-breadcrumbs > ol > li:nth-child(2) > a"
    val overviewBreadcrumbSelector = "body > div > div.govuk-breadcrumbs > ol > li:nth-child(3)"
    val captionSelector = "#main-content > div > div > header > p"
    val headerSelector = "#main-content > div > div > header > h1"
    val updateYourIncomeTaxReturnSubheadingSelector = "#main-content > div > div > ol > li:nth-child(1) > h2"
    val completeSectionsSelector = "#main-content > div > div > ol > li:nth-child(1) > ol > li.govuk-body"
    val interestLinkSelector = "#interest_link"
    val interestStatusSelector = "#main-content > div > div > ol > li:nth-child(1) > ol > li:nth-child(4) > span.hmrc-status-tag"
    val dividendsLinkSelector = "#dividends_link"
    val dividendsStatusSelector = "#main-content > div > div > ol > li:nth-child(1) > ol > li:nth-child(5) > span.hmrc-status-tag"
    val employmentSelector = "#main-content > div > div > ol > li:nth-child(1) > ol > li:nth-child(7) > span.app-task-list__task-name"
    val giftAidLinkSelector = "#giftAid_link"
    val giftAidStatusSelector = "#main-content > div > div > ol > li:nth-child(1) > ol > li:nth-child(6) > span.hmrc-status-tag"
    val employmentLinkSelector = "#employment_link"
    val employmentStatusSelector = "#main-content > div > div > ol > li:nth-child(1) > ol > li:nth-child(7) > span.hmrc-status-tag"
    val viewTaxCalcSelector = "#main-content > div > div > ol > li:nth-child(2) > h2"
    val provideUpdatesSelector = "#main-content > div > div > ol > li.app-task-list__items > p"
    val viewEstimateSelector = "#calculation_link"
    val submitReturnSelector = "#main-content > div > div > ol > li:nth-child(4) > h2"
    val youWillBeAbleSelector = "#main-content > div > div > ol > li:nth-child(4) > ul > li"
    val endOfYearParagraph1Selector = "#main-content > div > div > p"
    val endOfYearBulletPoint1Selector = "#main-content > div > div > ul > li:nth-child(1)"
    val endOfYearBulletPoint2Selector = "#main-content > div > div > ul > li:nth-child(2)"
    val endOfYearWarningTextSelector = "#main-content > div > div > ol > li:nth-child(1) > ol > div > strong"
    val interestStatusSelectorEndOfYear = "#main-content > div > div > ol > li:nth-child(1) > ol > li:nth-child(4) > span.hmrc-status-tag"
    val dividendsStatusSelectorEndOfYear = "#main-content > div > div > ol > li:nth-child(1) > ol > li:nth-child(5) > span.hmrc-status-tag"
    val giftAidStatusSelectorEndOfYear = "#main-content > div > div > ol > li:nth-child(1) > ol > li:nth-child(6) > span.hmrc-status-tag"
    val employmentStatusSelectorEndOfYear = "#main-content > div > div > ol > li:nth-child(1) > ol > li:nth-child(7) > span.hmrc-status-tag"
    val endOfYearContinueButtonSelector = "#main-content > div > div > ol > li:nth-child(4) > ul > form"
  }

  private val urlPathInYear = s"/income-through-software/return/$taxYear/view"
  private val urlPathEndOfYear = s"/income-through-software/return/$taxYearEndOfYear/income-tax-return-overview"

  lazy val frontendAppConfig: AppConfig = app.injector.instanceOf[AppConfig]

  val controller: OverviewPageController = new OverviewPageController(
    frontendAppConfig,
    mcc,
    scala.concurrent.ExecutionContext.Implicits.global,
    inYearAction,
    app.injector.instanceOf[IncomeSourcesService],
    app.injector.instanceOf[CalculationIdService],
    app.injector.instanceOf[OverviewPageView],
    app.injector.instanceOf[AuthorisedAction],
    app.injector.instanceOf[ErrorHandler]
  )


  def stubIncomeSources(incomeSources: IncomeSourcesModel): StubMapping = {
    stubGet("/income-tax-submission-service/income-tax/nino/AA123456A/sources\\?taxYear=2022", OK, Json.toJson(incomeSources).toString())
  }

  val userScenarios: Seq[UserScenario[CommonExpectedResults, SpecificExpectedResults]] = {
    Seq(
      UserScenario(isWelsh = false, isAgent = false, CommonExpectedEN, Some(ExpectedIndividualEN)),
      UserScenario(isWelsh = false, isAgent = true, CommonExpectedEN, Some(ExpectedAgentEN)),
      UserScenario(isWelsh = true, isAgent = false, CommonExpectedCY, Some(ExpectedIndividualCY)),
      UserScenario(isWelsh = true, isAgent = true, CommonExpectedCY, Some(ExpectedAgentCY))
    )
  }

  ".show for in year" when {
    import Links._
    import Selectors._

    userScenarios.foreach { user =>
      import user.commonExpectedResults._

      val specific = user.specificExpectedResults.get

      s"language is ${welshTest(user.isWelsh)} and request is from an ${agentTest(user.isAgent)}" should {
        val headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck")

        "render an overview page with all sections showing status tag 'under maintenance' when feature switch is false" when {
          val request = FakeRequest("GET", urlPathInYear).withHeaders(headers: _*)

          lazy val result: Future[Result] = {
            authoriseAgentOrIndividual(user.isAgent)
            route(appWithSourcesTurnedOff, request, user.isWelsh).get
          }


          implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

          "returns status of OK(200)" in {
            status(result) shouldBe OK
          }

          welshToggleCheck(welshTest(user.isWelsh))
          linkCheck(vcBreadcrumb, vcBreadcrumbSelector, Links.vcBreadCrumbUrl(user.isAgent))
          linkCheck(startPageBreadcrumb, startPageBreadcrumbSelector, startPageBreadcrumbUrl(taxYear))
          textOnPageCheck(overviewBreadcrumb, overviewBreadcrumbSelector)

          titleCheck(specific.headingExpected)
          h1Check(specific.headingExpected, "xl")
          textOnPageCheck(caption(taxYearMinusOne, taxYear), captionSelector)
          textOnPageCheck(specific.ifWeHaveInfo, endOfYearParagraph1Selector)
          textOnPageCheck(ourRecords, endOfYearBulletPoint1Selector)
          textOnPageCheck(softwarePackage, endOfYearBulletPoint2Selector)
          textOnPageCheck(specific.warning, endOfYearWarningTextSelector)
          textOnPageCheck(specific.updateIncomeTaxReturnText, updateYourIncomeTaxReturnSubheadingSelector)
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
          textOnPageCheck(specific.provideUpdate, provideUpdatesSelector)
          textOnPageCheck(submitReturnText, submitReturnSelector)
          textOnPageCheck(specific.youWillBeAble(taxYearPlusOne), youWillBeAbleSelector)
        }

        "render overview page with 'Not Started' status tags when there is no prior data and the employment section with" +
          "the status tag 'cannot update' user in the current taxYear" when {

          val request = FakeRequest("GET", urlPathInYear).withHeaders(headers: _*)

          lazy val result: Future[Result] = {
            authoriseAgentOrIndividual(user.isAgent)
            stubIncomeSources(incomeSourcesModel.copy(None, None, None, None))
            route(app, request, user.isWelsh).get
          }

          implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

          "returns status of OK(200)" in {
            status(result) shouldBe OK
          }

          welshToggleCheck(welshTest(user.isWelsh))
          linkCheck(vcBreadcrumb, vcBreadcrumbSelector, Links.vcBreadCrumbUrl(user.isAgent))
          linkCheck(startPageBreadcrumb, startPageBreadcrumbSelector, startPageBreadcrumbUrl(taxYear))
          textOnPageCheck(overviewBreadcrumb, overviewBreadcrumbSelector)

          titleCheck(specific.headingExpected)
          h1Check(specific.headingExpected, "xl")
          textOnPageCheck(caption(taxYearMinusOne, taxYear), captionSelector)
          textOnPageCheck(specific.ifWeHaveInfo, endOfYearParagraph1Selector)
          textOnPageCheck(ourRecords, endOfYearBulletPoint1Selector)
          textOnPageCheck(softwarePackage, endOfYearBulletPoint2Selector)
          textOnPageCheck(specific.warning, endOfYearWarningTextSelector)
          textOnPageCheck(specific.updateIncomeTaxReturnText, updateYourIncomeTaxReturnSubheadingSelector)
          textOnPageCheck(completeSectionsText, completeSectionsSelector)

          "has a dividends section" which {
            linkCheck(dividendsLinkText, dividendsLinkSelector, dividendsLink(taxYear))
            textOnPageCheck(notStartedText, dividendsStatusSelector)
          }

          "has an interest section" which {
            linkCheck(interestsLinkText, interestLinkSelector, interestsLink(taxYear))
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
          textOnPageCheck(specific.provideUpdate, provideUpdatesSelector)
          textOnPageCheck(submitReturnText, submitReturnSelector)
          textOnPageCheck(specific.youWillBeAble(taxYearPlusOne), youWillBeAbleSelector)
        }

        "render overview page with 'Started' status tags when there is prior data and the employment section is clickable with" +
          "the status tag 'Not Started' when user is in a previous year" when {
          val previousYearHeaders = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYearMinusOne), "Csrf-Token" -> "nocheck")
          val previousYearUrl = s"/income-through-software/return/$taxYearMinusOne/income-tax-return-overview"
          val taxYearMinusTwo = taxYearMinusOne - 1
          val request = FakeRequest("GET", previousYearUrl).withHeaders(previousYearHeaders: _*)

          lazy val result: Future[Result] = {
            authoriseAgentOrIndividual(user.isAgent)
            stubIncomeSources(incomeSourcesModel)
            route(appWithTaxYearErrorOff, request, user.isWelsh).get
          }

          implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

          "returns status of OK(200)" in {
            status(result) shouldBe OK
          }

          welshToggleCheck(welshTest(user.isWelsh))
          linkCheck(vcBreadcrumb, vcBreadcrumbSelector, Links.vcBreadCrumbUrl(user.isAgent))
          linkCheck(startPageBreadcrumb, startPageBreadcrumbSelector, startPageBreadcrumbUrl(taxYearMinusOne))
          textOnPageCheck(overviewBreadcrumb, overviewBreadcrumbSelector)

          titleCheck(specific.headingExpected)
          h1Check(specific.headingExpected, "xl")
          textOnPageCheck(caption(taxYearMinusTwo, taxYearMinusOne), captionSelector)
          textOnPageCheck(specific.ifWeHaveInfo, endOfYearParagraph1Selector)
          textOnPageCheck(ourRecords, endOfYearBulletPoint1Selector)
          textOnPageCheck(softwarePackage, endOfYearBulletPoint2Selector)
          textOnPageCheck(specific.warning, endOfYearWarningTextSelector)
          textOnPageCheck(specific.updateIncomeTaxReturnText, updateYourIncomeTaxReturnSubheadingSelector)
          textOnPageCheck(completeSectionsText, completeSectionsSelector)

          "has a dividends section" which {
            linkCheck(dividendsLinkText, dividendsLinkSelector, dividendsLink(taxYearMinusOne))
            textOnPageCheck(notStartedText, dividendsStatusSelector)
          }

          "has an interest section" which {
            linkCheck(interestsLinkText, interestLinkSelector, interestsLink(taxYearMinusOne))
            textOnPageCheck(notStartedText, interestStatusSelectorEndOfYear)
          }

          "has an employment section " which {
            linkCheck(employmentLinkText, employmentLinkSelector, newEmploymentLink(taxYearMinusOne))
            textOnPageCheck(notStartedText, employmentStatusSelector)
          }

          "has a donations to charity section" which {
            linkCheck(giftAidLinkText, giftAidLinkSelector, appConfig.personalIncomeTaxGiftAidUrl(taxYearMinusOne))
            textOnPageCheck(notStartedText, giftAidStatusSelector)
          }

          textOnPageCheck(viewTaxCalcText, viewTaxCalcSelector)
          textOnPageCheck(specific.provideUpdate, provideUpdatesSelector)
        }

        "render overview page with status tag 'Not Started' for interest when interest income source is None " when {

          val request = FakeRequest("GET", urlPathInYear).withHeaders(headers: _*)

          lazy val result: Future[Result] = {
            authoriseAgentOrIndividual(user.isAgent)
            stubIncomeSources(incomeSourcesModel.copy(interest = None))
            route(app, request, user.isWelsh).get
          }

          implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

          "returns status of OK(200)" in {
            status(result) shouldBe OK
          }

          welshToggleCheck(welshTest(user.isWelsh))
          linkCheck(vcBreadcrumb, vcBreadcrumbSelector, Links.vcBreadCrumbUrl(user.isAgent))
          linkCheck(startPageBreadcrumb, startPageBreadcrumbSelector, startPageBreadcrumbUrl(taxYear))
          textOnPageCheck(overviewBreadcrumb, overviewBreadcrumbSelector)

          titleCheck(specific.headingExpected)
          h1Check(specific.headingExpected, "xl")
          textOnPageCheck(caption(taxYearMinusOne, taxYear), captionSelector)
          textOnPageCheck(specific.ifWeHaveInfo, endOfYearParagraph1Selector)
          textOnPageCheck(ourRecords, endOfYearBulletPoint1Selector)
          textOnPageCheck(softwarePackage, endOfYearBulletPoint2Selector)
          textOnPageCheck(specific.warning, endOfYearWarningTextSelector)
          textOnPageCheck(specific.updateIncomeTaxReturnText, updateYourIncomeTaxReturnSubheadingSelector)
          textOnPageCheck(completeSectionsText, completeSectionsSelector)

          "has a dividends section" which {
            linkCheck(dividendsLinkText, dividendsLinkSelector, dividendsLinkWithPriorData(taxYear))
            textOnPageCheck(updatedText, dividendsStatusSelector)
          }

          "has an interest section with status of 'Not Started'" which {
            linkCheck(interestsLinkText, interestLinkSelector, interestsLink(taxYear))
            textOnPageCheck(notStartedText, interestStatusSelector)
          }

          "has a donations to charity section" which {
            linkCheck(giftAidLinkText, giftAidLinkSelector, appConfig.personalIncomeTaxGiftAidSubmissionCYAUrl(taxYear))
            textOnPageCheck(updatedText, giftAidStatusSelector)
          }

          "has an employment section" which {
            linkCheck(employmentLinkText, employmentLinkSelector, employmentLink(taxYear))
            textOnPageCheck(updatedText, employmentStatusSelector)
          }

          textOnPageCheck(viewTaxCalcText, viewTaxCalcSelector)
          textOnPageCheck(specific.incomeCalcToDate, provideUpdatesSelector)
          linkCheck(viewTaxCalcLink, viewEstimateSelector, viewEstimateLink(taxYear))
          textOnPageCheck(submitReturnText, submitReturnSelector)
          textOnPageCheck(specific.youWillBeAble(taxYearPlusOne), youWillBeAbleSelector)
        }

        "render overview page with correct status tags when there is prior data and user is in the current taxYear" should {
          "have the status as 'Not Started' for interest when interest income source is None" when {

            val incomeSources = incomeSourcesModel.copy(interest = None)

            val request = FakeRequest("GET", urlPathInYear).withHeaders(headers: _*)

            lazy val result: Future[Result] = {
              authoriseAgentOrIndividual(user.isAgent)
              stubIncomeSources(incomeSources)
              route(app, request, user.isWelsh).get
            }

            implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

            "returns status of OK(200)" in {
              status(result) shouldBe OK
            }

            welshToggleCheck(welshTest(user.isWelsh))
            linkCheck(vcBreadcrumb, vcBreadcrumbSelector, Links.vcBreadCrumbUrl(user.isAgent))
            linkCheck(startPageBreadcrumb, startPageBreadcrumbSelector, startPageBreadcrumbUrl(taxYear))
            textOnPageCheck(overviewBreadcrumb, overviewBreadcrumbSelector)

            titleCheck(specific.headingExpected)
            h1Check(specific.headingExpected, "xl")
            textOnPageCheck(caption(taxYearMinusOne, taxYear), captionSelector)
            textOnPageCheck(specific.ifWeHaveInfo, endOfYearParagraph1Selector)
            textOnPageCheck(ourRecords, endOfYearBulletPoint1Selector)
            textOnPageCheck(softwarePackage, endOfYearBulletPoint2Selector)
            textOnPageCheck(specific.warning, endOfYearWarningTextSelector)
            textOnPageCheck(specific.updateIncomeTaxReturnText, updateYourIncomeTaxReturnSubheadingSelector)
            textOnPageCheck(completeSectionsText, completeSectionsSelector)

            "has a dividends section" which {
              linkCheck(dividendsLinkText, dividendsLinkSelector, dividendsLinkWithPriorData(taxYear))
              textOnPageCheck(updatedText, dividendsStatusSelector)
            }

            "has an interest section with status of 'Not Started'" which {
              linkCheck(interestsLinkText, interestLinkSelector, interestsLink(taxYear))
              textOnPageCheck(notStartedText, interestStatusSelector)
            }

            "has a donations to charity section" which {
              linkCheck(giftAidLinkText, giftAidLinkSelector, appConfig.personalIncomeTaxGiftAidSubmissionCYAUrl(taxYear))
              textOnPageCheck(updatedText, giftAidStatusSelector)
            }

            "has an employment section" which {
              linkCheck(employmentLinkText, employmentLinkSelector, employmentLink(taxYear))
              textOnPageCheck(updatedText, employmentStatusSelector)
            }

            textOnPageCheck(viewTaxCalcText, viewTaxCalcSelector)
            textOnPageCheck(specific.incomeCalcToDate, provideUpdatesSelector)
            linkCheck(viewTaxCalcLink, viewEstimateSelector, viewEstimateLink(taxYear))
            textOnPageCheck(submitReturnText, submitReturnSelector)
            textOnPageCheck(specific.youWillBeAble(taxYearPlusOne), youWillBeAbleSelector)
          }
        }

        "interest income source is defined and the untaxed and taxed accounts do not have amounts" which {
          val incomeSources = incomeSourcesModel.copy(interest = Some(Seq(InterestModel("TestName", "TestSource", None, None))))

          val request = FakeRequest("GET", urlPathInYear).withHeaders(headers: _*)

          lazy val result: Future[Result] = {
            authoriseAgentOrIndividual(user.isAgent)
            stubIncomeSources(incomeSources)
            route(app, request, user.isWelsh).get
          }

          implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

          "returns status of OK(200)" in {
            status(result) shouldBe OK
          }

          welshToggleCheck(welshTest(user.isWelsh))
          linkCheck(vcBreadcrumb, vcBreadcrumbSelector, Links.vcBreadCrumbUrl(user.isAgent))
          linkCheck(startPageBreadcrumb, startPageBreadcrumbSelector, startPageBreadcrumbUrl(taxYear))
          textOnPageCheck(overviewBreadcrumb, overviewBreadcrumbSelector)

          titleCheck(specific.headingExpected)
          h1Check(specific.headingExpected, "xl")
          textOnPageCheck(caption(taxYearMinusOne, taxYear), captionSelector)
          textOnPageCheck(specific.ifWeHaveInfo, endOfYearParagraph1Selector)
          textOnPageCheck(ourRecords, endOfYearBulletPoint1Selector)
          textOnPageCheck(softwarePackage, endOfYearBulletPoint2Selector)
          textOnPageCheck(specific.warning, endOfYearWarningTextSelector)
          textOnPageCheck(specific.updateIncomeTaxReturnText, updateYourIncomeTaxReturnSubheadingSelector)
          textOnPageCheck(completeSectionsText, completeSectionsSelector)

          "has a dividends section" which {
            linkCheck(dividendsLinkText, dividendsLinkSelector, dividendsLinkWithPriorData(taxYear))
            textOnPageCheck(updatedText, dividendsStatusSelector)
          }

          "has an interest section with status of 'Not Started'" which {
            linkCheck(interestsLinkText, interestLinkSelector, interestsLink(taxYear))
            textOnPageCheck(notStartedText, interestStatusSelector)
          }

          "has a donations to charity section" which {
            linkCheck(giftAidLinkText, giftAidLinkSelector, appConfig.personalIncomeTaxGiftAidSubmissionCYAUrl(taxYear))
            textOnPageCheck(updatedText, giftAidStatusSelector)
          }

          "has an employment section" which {
            linkCheck(employmentLinkText, employmentLinkSelector, employmentLink(taxYear))
            textOnPageCheck(updatedText, employmentStatusSelector)
          }

          textOnPageCheck(viewTaxCalcText, viewTaxCalcSelector)
          textOnPageCheck(specific.incomeCalcToDate, provideUpdatesSelector)
          linkCheck(viewTaxCalcLink, viewEstimateSelector, viewEstimateLink(taxYear))
          textOnPageCheck(submitReturnText, submitReturnSelector)
          textOnPageCheck(specific.youWillBeAble(taxYearPlusOne), youWillBeAbleSelector)
        }

        "have the status as 'Updated' for interest" when {
          val interestsModelWithAmounts: Option[Seq[InterestModel]] = Some(Seq(InterestModel("TestName", "TestSource", Some(100.00), Some(100.00))))

          "interest income source is defined and the untaxed and taxed accounts have amounts" which {
            val incomeSources = incomeSourcesModel.copy(interest = interestsModelWithAmounts)

            val request = FakeRequest("GET", urlPathInYear).withHeaders(headers: _*)

            lazy val result: Future[Result] = {
              authoriseAgentOrIndividual(user.isAgent)
              stubIncomeSources(incomeSources)
              route(app, request, user.isWelsh).get
            }

            implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

            "returns status of OK(200)" in {
              status(result) shouldBe OK
            }

            welshToggleCheck(welshTest(user.isWelsh))
            linkCheck(vcBreadcrumb, vcBreadcrumbSelector, Links.vcBreadCrumbUrl(user.isAgent))
            linkCheck(startPageBreadcrumb, startPageBreadcrumbSelector, startPageBreadcrumbUrl(taxYear))
            textOnPageCheck(overviewBreadcrumb, overviewBreadcrumbSelector)

            titleCheck(specific.headingExpected)
            h1Check(specific.headingExpected, "xl")
            textOnPageCheck(caption(taxYearMinusOne, taxYear), captionSelector)
            textOnPageCheck(specific.ifWeHaveInfo, endOfYearParagraph1Selector)
            textOnPageCheck(ourRecords, endOfYearBulletPoint1Selector)
            textOnPageCheck(softwarePackage, endOfYearBulletPoint2Selector)
            textOnPageCheck(specific.warning, endOfYearWarningTextSelector)
            textOnPageCheck(specific.updateIncomeTaxReturnText, updateYourIncomeTaxReturnSubheadingSelector)
            textOnPageCheck(completeSectionsText, completeSectionsSelector)

            "has a dividends section" which {
              linkCheck(dividendsLinkText, dividendsLinkSelector, dividendsLinkWithPriorData(taxYear))
              textOnPageCheck(updatedText, dividendsStatusSelector)
            }

            "has an interest section with status of 'Updated'" which {
              linkCheck(interestsLinkText, interestLinkSelector, interestsLinkWithPriorData(taxYear))
              textOnPageCheck(updatedText, interestStatusSelector)
            }

            "has a donations to charity section" which {
              linkCheck(giftAidLinkText, giftAidLinkSelector, appConfig.personalIncomeTaxGiftAidSubmissionCYAUrl(taxYear))
              textOnPageCheck(updatedText, giftAidStatusSelector)
            }

            "has an employment section" which {
              linkCheck(employmentLinkText, employmentLinkSelector, employmentLink(taxYear))
              textOnPageCheck(updatedText, employmentStatusSelector)
            }

            textOnPageCheck(viewTaxCalcText, viewTaxCalcSelector)
            textOnPageCheck(specific.incomeCalcToDate, provideUpdatesSelector)
            linkCheck(viewTaxCalcLink, viewEstimateSelector, viewEstimateLink(taxYear))
            textOnPageCheck(submitReturnText, submitReturnSelector)
            textOnPageCheck(specific.youWillBeAble(taxYearPlusOne), youWillBeAbleSelector)
          }
        }
      }
    }
  }

  ".show for end of year" when {
    import Links._
    import Selectors._

    userScenarios.foreach { user =>
      import user.commonExpectedResults._

      val specific = user.specificExpectedResults.get

      s"language is ${welshTest(user.isWelsh)} and request is from an ${agentTest(user.isAgent)}" should {
        val headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYearEndOfYear), "Csrf-Token" -> "nocheck")

        "render an overview page with all sections turned off" when {
          val request = FakeRequest("GET", urlPathEndOfYear).withHeaders(headers: _*)

          lazy val result: Future[Result] = {
            authoriseAgentOrIndividual(user.isAgent)
            route(appWithSourcesTurnedOffEndOfYear, request, user.isWelsh).get
          }

          implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

          "returns status of OK(200)" in {
            status(result) shouldBe OK
          }

          welshToggleCheck(welshTest(user.isWelsh))
          linkCheck(vcBreadcrumb, vcBreadcrumbSelector, Links.vcBreadCrumbUrl(user.isAgent))
          linkCheck(startPageBreadcrumb, startPageBreadcrumbSelector, Links.startPageBreadcrumbUrl(taxYearEndOfYear))
          textOnPageCheck(overviewBreadcrumb, overviewBreadcrumbSelector)

          titleCheck(specific.headingExpected)
          h1Check(specific.headingExpected, "xl")
          textOnPageCheck(caption(taxYearEndOfYearMinusOne, taxYearEndOfYear), captionSelector)
          textOnPageCheck(specific.ifWeHaveInfo, endOfYearParagraph1Selector)
          textOnPageCheck(ourRecords, endOfYearBulletPoint1Selector)
          textOnPageCheck(softwarePackage, endOfYearBulletPoint2Selector)
          textOnPageCheck(specific.warning, endOfYearWarningTextSelector)
          textOnPageCheck(specific.updateIncomeTaxReturnText, updateYourIncomeTaxReturnSubheadingSelector)
          textOnPageCheck(completeSectionsText, completeSectionsSelector)

          "have a dividends section that says under maintenance" which {
            textOnPageCheck(underMaintenance, dividendsStatusSelectorEndOfYear)
          }

          "have an interest section that says under maintenance" which {
            textOnPageCheck(underMaintenance, interestStatusSelectorEndOfYear)
          }

          "have an employment section that says under maintenance" which {
            textOnPageCheck(underMaintenance, employmentStatusSelectorEndOfYear)
          }

          "has a donations to charity section" which {
            textOnPageCheck(underMaintenance, giftAidStatusSelectorEndOfYear)
          }

          textOnPageCheck(viewTaxCalcText, viewTaxCalcSelector)
          textOnPageCheck(specific.provideUpdate, provideUpdatesSelector)
          textOnPageCheck(specific.submitReturnHeaderEOY, submitReturnSelector)
          textOnPageCheck(specific.submitReturnText, youWillBeAbleSelector)
          formPostLinkCheck(endOfYearContinueLink, endOfYearContinueButtonSelector)
        }

        "render an overview page with prior data" when {
          val request = FakeRequest("GET", urlPathEndOfYear).withHeaders(headers: _*)

          lazy val result: Future[Result] = {
            authoriseAgentOrIndividual(user.isAgent)
            stubIncomeSourcesEndOfYear
            route(appWithSourcesTurnedOnEndOfYear, request, user.isWelsh).get
          }

          implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

          "returns status of OK(200)" in {
            status(result) shouldBe OK
          }

          welshToggleCheck(welshTest(user.isWelsh))
          linkCheck(vcBreadcrumb, vcBreadcrumbSelector, Links.vcBreadCrumbUrl(user.isAgent))
          linkCheck(startPageBreadcrumb, startPageBreadcrumbSelector, Links.startPageBreadcrumbUrl(taxYearEndOfYear))
          textOnPageCheck(overviewBreadcrumb, overviewBreadcrumbSelector)

          titleCheck(specific.headingExpected)
          h1Check(specific.headingExpected, "xl")
          textOnPageCheck(caption(taxYearEndOfYearMinusOne, taxYearEndOfYear), captionSelector)
          textOnPageCheck(specific.ifWeHaveInfo, endOfYearParagraph1Selector)
          textOnPageCheck(ourRecords, endOfYearBulletPoint1Selector)
          textOnPageCheck(softwarePackage, endOfYearBulletPoint2Selector)
          textOnPageCheck(specific.warning, endOfYearWarningTextSelector)
          textOnPageCheck(specific.updateIncomeTaxReturnText, updateYourIncomeTaxReturnSubheadingSelector)
          textOnPageCheck(completeSectionsText, completeSectionsSelector)

          "has a dividends section" which {
            linkCheck(dividendsLinkText, dividendsLinkSelector, dividendsLinkWithPriorData(taxYearEndOfYear))
            textOnPageCheck(updatedText, dividendsStatusSelectorEndOfYear)
          }

          "has an interest section" which {
            linkCheck(interestsLinkText, interestLinkSelector, interestsLinkWithPriorData(taxYearEndOfYear))
            textOnPageCheck(updatedText, interestStatusSelectorEndOfYear)
          }

          "has an employment section" which {
            linkCheck(employmentLinkText, employmentLinkSelector, employmentLink(taxYearEndOfYear))
            textOnPageCheck(updatedText, employmentStatusSelectorEndOfYear)
          }

          "has a donations to charity section" which {
            linkCheck(giftAidLinkText, giftAidLinkSelector, appConfig.personalIncomeTaxGiftAidSubmissionCYAUrl(taxYearEndOfYear))
            textOnPageCheck(updatedText, giftAidStatusSelectorEndOfYear)
          }

          textOnPageCheck(viewTaxCalcText, viewTaxCalcSelector)
          textOnPageCheck(specific.incomeCalcToDate, provideUpdatesSelector)
          linkCheck(viewTaxCalcLink, viewEstimateSelector, viewEstimateLink(taxYearEndOfYear))
          textOnPageCheck(specific.submitReturnHeaderEOY, submitReturnSelector)
          textOnPageCheck(specific.submitReturnText, youWillBeAbleSelector)
          formPostLinkCheck(endOfYearContinueLink, endOfYearContinueButtonSelector)
        }
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

    "return a SEE_OTHER (303) when end of year" when {

      "all auth requirements are met" in {

        val result = {
          authoriseIndividual()
          sourcesTurnedOffConfigEndOfYear
          await(controller.show(taxYearEndOfYear)(fakeRequest.withSession(SessionValues.TAX_YEAR -> "2021", SessionKeys.sessionId -> "sessionId-0101010101")))
        }

        result.header.status shouldBe SEE_OTHER



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

  def stubIncomeSourcesEndOfYear: StubMapping = stubGet("/income-tax-submission-service/income-tax/nino/AA123456A/sources\\?taxYear=2021", OK,
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

}