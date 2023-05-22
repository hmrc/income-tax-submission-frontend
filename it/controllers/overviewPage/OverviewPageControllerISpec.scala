/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers.overviewPage

import audit.AuditService
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import common.IncomeSources.INTEREST
import common.SessionValues
import config.{AppConfig, ErrorHandler}
import controllers.OverviewPageController
import controllers.predicates.AuthorisedAction
import itUtils.{IntegrationTest, OverviewPageHelpers, ViewHelpers}
import models._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.http.HeaderNames
import play.api.http.Status._
import play.api.mvc.Result
import play.api.test.Helpers.{OK, status, writeableOf_AnyContentAsEmpty}
import play.api.test.{FakeRequest, Helpers}
import repositories.TailoringUserDataRepository
import services.{ExcludedJourneysService, IncomeSourcesService, LiabilityCalculationService, ValidTaxYearListService}
import uk.gov.hmrc.http.SessionKeys
import views.html.OverviewPageView

import java.util.UUID
import scala.concurrent.Future

class OverviewPageControllerISpec extends IntegrationTest with ViewHelpers with OverviewPageHelpers {

  object ExpectedIndividualEN extends SpecificExpectedResults {
    val headingExpected = "Your Income Tax Return"
    val updateIncomeTaxReturnText = "Update your Income Tax Return"
    val submitReturnHeaderEOY = "Check and submit your Income Tax Return"
    val submitReturnText: String = "If you’ve finished updating your Income Tax Return, you can continue and see your final tax calculation. " +
      "You can check your calculation and then submit your Income Tax Return."
    val ifWeHaveInfo: String = "If we have information about your income and deductions, " +
      "we’ll enter it for you. We get this information from our records and your software package - if you have one."
    val goToYourIncomeTax = "Go to your Income Tax Account to find out more about your current tax position."

    def inYearInsertText(taxYear: Int): String = s"You cannot submit your Income Tax Return until 6 April $taxYear."

    val addSections = "Add sections to your Income Tax Return"
    val notificationBanner = "We have added a section to your return, based on the information we already hold about you."
    val notificationBannerPlural = "We have added 8 sections to your return, based on the information we already hold about you."
  }

  object ExpectedAgentEN extends SpecificExpectedResults {
    val headingExpected = "Your client’s Income Tax Return"
    val updateIncomeTaxReturnText = "Update your client’s Income Tax Return"
    val submitReturnHeaderEOY = "Check and submit your client’s Income Tax Return"
    val submitReturnText: String = "If you’ve finished updating your client’s Income Tax Return, you can continue and see their final tax calculation." +
      " You can check their calculation and then submit their Income Tax Return."
    val ifWeHaveInfo: String = "If we have information about your client’s income and deductions, " +
      "we’ll enter it for you. We get this information from our records and your software package - if you have one."
    val goToYourIncomeTax = "Go to your client’s Income Tax Account to find out more about their current tax position."

    def inYearInsertText(taxYear: Int): String = s"You cannot submit your client’s Income Tax Return until 6 April $taxYear."

    val addSections = "Add sections to your client’s Income Tax Return"
    val notificationBanner = "We have added a section to your client’s return, based on the information we already hold about them."
    val notificationBannerPlural = "We have added 8 sections to your client’s return, based on the information we already hold about them."
  }

  object ExpectedIndividualCY extends SpecificExpectedResults {
    val headingExpected = "Eich Ffurflen Dreth Incwm"
    val updateIncomeTaxReturnText = "Diweddarwch eich Ffurflen Dreth Incwm"
    val submitReturnHeaderEOY = "Diweddaru a chyflwyno’ch Ffurflen Dreth Incwm"
    val submitReturnText: String = "Os ydych wedi gorffen diweddaru eich Ffurflen Dreth Incwm, gallwch barhau a gweld eich cyfrifiad treth terfynol. Gallwch wirio eich cyfrifiad ac yna fe allwch gyflwyno eich Ffurflen Dreth Incwm."
    val ifWeHaveInfo = "Os oes gennym wybodaeth am eich incwm a didyniadau, byddwn yn ei chofnodi ar eich rhan. Rydym yn cael yr wybodaeth hon o’n cofnodion a’ch pecyn meddalwedd - os oes gennych un."
    val goToYourIncomeTax = "Ewch i’ch Cyfrif Treth Incwm i wybod mwy am eich sefyllfa dreth bresennol."

    def inYearInsertText(taxYear: Int): String = s"Ni allwch gyflwyno’ch Ffurflen Dreth Incwm tan 6 Ebrill $taxYear."

    val addSections = "Ychwanegu adrannau at eich Ffurflen Dreth Incwm"
    val notificationBanner = "Rydym wedi ychwanegu adran at eich Ffurflen Dreth, yn seiliedig ar yr wybodaeth sydd eisoes gennym amdanoch."
    val notificationBannerPlural = "Rydym wedi ychwanegu 8 adran at eich Ffurflen Dreth, yn seiliedig ar yr wybodaeth sydd eisoes gennym amdanoch."
  }

  object ExpectedAgentCY extends SpecificExpectedResults {
    val headingExpected = "Ffurflen Dreth Incwm eich cleient"
    val updateIncomeTaxReturnText = "Diweddarwch Ffurflen Dreth Incwm eich cleient"
    val submitReturnHeaderEOY = "Gwiriwch a chyflwynwch Ffurflen Dreth Incwm eich cleient"
    val submitReturnText: String = "Os ydych wedi gorffen diweddaru Ffurflen Dreth Incwm eich cleient, gallwch barhau a gweld eu cyfrifiad treth terfynol. " +
      "Gwiriwch y cyfrifiad a chyflwyno’r Ffurflen Dreth Incwm."
    val ifWeHaveInfo: String = "Os oes gennym wybodaeth am incwm a didyniadau eich cleient, byddwn yn ei chofnodi ar eich rhan. " +
      "Rydym yn cael yr wybodaeth hon o’n cofnodion a’ch pecyn meddalwedd - os oes gennych un."
    val goToYourIncomeTax = "Ewch i’r canlynol ar ran eich cleient Cyfrif Treth Incwm i wybod mwy am ei sefyllfa dreth bresennol."

    def inYearInsertText(taxYear: Int): String = s"Ni allwch gyflwyno’ch Ffurflen Dreth Incwm eich cleient tan 6 Ebrill $taxYear."

    val addSections: String = "Ychwanegu adrannau at Ffurflen Dreth Incwm eich cleient"
    val notificationBanner = "Rydym wedi ychwanegu adran at Ffurflen Dreth eich cleient, yn seiliedig ar yr wybodaeth sydd eisoes gennym amdano."
    val notificationBannerPlural = "Rydym wedi ychwanegu 8 adran at Ffurflen Dreth eich cleient, yn seiliedig ar yr wybodaeth sydd eisoes gennym amdano."
  }

  trait SpecificExpectedResults {
    val headingExpected: String
    val updateIncomeTaxReturnText: String
    val submitReturnHeaderEOY: String
    val submitReturnText: String
    val ifWeHaveInfo: String
    val goToYourIncomeTax: String

    def inYearInsertText(taxYear: Int): String

    val addSections: String
    val notificationBanner: String
    val notificationBannerPlural: String
  }

  trait CommonExpectedResults {
    val vcBreadcrumb: String
    val startPageBreadcrumb: String
    val overviewBreadcrumb: String

    def caption(taxYearMinusOne: Int, taxYear: Int): String

    val updatedText: String
    val notStartedText: String
    val todoText: String
    val underMaintenance: String
    val cannotUpdateText: String
    val dividendsLinkText: String
    val interestsLinkText: String
    val employmentSLLinkText: String
    val employmentLinkText: String
    val cisLinkText: String
    val giftAidLinkText: String
    val pensionsLinkText: String
    val stateBenefitsLinkText: String
    val gainsLinkText: String
    val continue: String
    val fillInTheSections: String
    val incomeTaxAccountLink: String
    val updateTaxCalculation: String
    val checkSectionsText: String
  }

  object CommonExpectedEN extends CommonExpectedResults {
    val vcBreadcrumb = "Income Tax Account"
    val startPageBreadcrumb = "Update and submit an Income Tax Return"
    val overviewBreadcrumb = "Your Income Tax Return"

    def caption(taxYearMinusOne: Int, taxYear: Int): String = s"6 April $taxYearMinusOne to 5 April $taxYear"

    val updatedText = "Updated"
    val todoText = "To do"
    val notStartedText = "Not started"
    val underMaintenance = "Under maintenance"
    val cannotUpdateText = "Cannot update"
    val dividendsLinkText = "Dividends"
    val interestsLinkText = "Interest"
    val employmentLinkText = "PAYE employment"
    val employmentSLLinkText = "PAYE employment (including student loans)"
    val cisLinkText = "Construction Industry Scheme deductions"
    val giftAidLinkText = "Donations to charity"
    val pensionsLinkText = "Pensions"
    val stateBenefitsLinkText = "State benefits"
    val gainsLinkText = "Gains from life insurance policies and contracts"
    val continue = "continue"
    val fillInTheSections = "Fill in the sections you need to update. Use your software package to update items that are not on this list."
    val incomeTaxAccountLink = "Income Tax Account"
    val updateTaxCalculation = "Update tax calculation"
    val checkSectionsText = "Check every section, you may need to change the information that we have added for you."
  }

  object CommonExpectedCY extends CommonExpectedResults {
    val vcBreadcrumb = "Cyfrif Treth Incwm"
    val startPageBreadcrumb = "Diweddaru a chyflwyno Ffurflen Dreth Incwm"
    val overviewBreadcrumb = "Eich Ffurflen Dreth Incwm"

    def caption(taxYearMinusOne: Int, taxYear: Int): String = s"6 Ebrill $taxYearMinusOne i 5 Ebrill $taxYear"

    val updatedText = "Wedi diweddaru"
    val todoText = "I’w gwneud"
    val notStartedText = "Heb ddechrau"
    val underMaintenance = "Wrthi’n cynnal a chadw’r safle"
    val cannotUpdateText = "Methu diweddaru"
    val dividendsLinkText = "Difidendau"
    val interestsLinkText = "Llog"
    val employmentLinkText = "Cyflogaeth TWE"
    val employmentSLLinkText = "Cyflogaeth TWE (gan gynnwys Benthyciadau Myfyrwyr)"
    val cisLinkText = "Didyniadau Cynllun y Diwydiant Adeiladu"
    val giftAidLinkText = "Rhoddion i elusennau"
    val pensionsLinkText = "Pensiynau"
    val stateBenefitsLinkText = "Budd-daliadau’r Wladwriaeth"
    val gainsLinkText = "Enillion o bolisïau yswiriant bywyd a chontractau"
    val continue = "continue"
    val fillInTheSections: String = "Llenwch yr adrannau mae angen i chi eu diweddaru. " +
      "Defnyddiwch eich pecyn meddalwedd i ddiweddaru eitemau sydd ddim ar y rhestr hon."
    val incomeTaxAccountLink = "Cyfrif Treth Incwm"
    val updateTaxCalculation = "Diweddaru cyfrifiad treth"
    val checkSectionsText = "Gwiriwch bob adran – mae’n bosibl y bydd rhaid i chi newid yr wybodaeth rydym wedi’i hychwanegu ar eich rhan."
  }

  object Selectors {
    def sectionNameSelector(index: Int): String = s"#main-content > div > div > ol > li:nth-child($index) > span.app-task-list__task-name"

    private def statusTagSelector(index: Int): String = s"#main-content > div > div > ol > li:nth-child($index) > span.hmrc-status-tag"

    val vcBreadcrumbSelector = "body > div > div.govuk-breadcrumbs > ol > li:nth-child(1) > a"
    val startPageBreadcrumbSelector = "body > div > div.govuk-breadcrumbs > ol > li:nth-child(2) > a"
    val overviewBreadcrumbSelector = "body > div > div.govuk-breadcrumbs > ol > li:nth-child(3)"
    val updateYourIncomeTaxReturnSubheadingSelector = "#heading-tasklist"
    val inYearInsetTextSelector = "#main-content > div > div > div.govuk-inset-text"
    val interestLinkSelector = "#interest_link"
    val interestStatusSelector: String = statusTagSelector(1)
    val dividendsLinkSelector = "#dividends_link"
    val dividendsStatusSelector: String = statusTagSelector(2)
    val giftAidLinkSelector = "#giftAid_link"
    val giftAidStatusSelector: String = statusTagSelector(3)
    val employmentSelector: String = sectionNameSelector(4)
    val employmentLinkSelector = "#employment_link"
    val employmentStatusSelector: String = statusTagSelector(4)
    val gainsLinkSelector = "#gains_link"
    val gainsSelector: String = sectionNameSelector(5)
    val gainsStatusSelector: String = statusTagSelector(5)
    val cisSelector: String = sectionNameSelector(6)
    val cisLinkSelector = "#cis_link"
    val cisStatusSelector: String = statusTagSelector(6)
    val pensionsSelector: String = sectionNameSelector(7)
    val pensionsLinkSelector = "#pensions_link"
    val pensionsStatusSelector: String = statusTagSelector(7)
    val stateBenefitsSelector: String = sectionNameSelector(8)
    val stateBenefitsLinkSelector = "#stateBenefits_link"
    val stateBenefitsStatusSelector: String = statusTagSelector(8)

    val viewEstimateSelector = "#calculation_link"
    val submitReturnEOYSelector = "#heading-checkAndSubmit"
    val submitReturnTextEOYSelector = "#p-submitText"
    val youWillBeAbleSelector = "#main-content > div > div > ol > li:nth-child(4) > ul > li.govuk-body"
    val ifWeHaveInformationSelector = "#main-content > div > div > p:nth-child(1)"
    val fillInTheSectionsSelector = "#main-content > div > div > p:nth-child(2)"
    val goToYourIncomeTaxReturnSelector = "#p-gotoAccountDetails"
    val updateTaxCalculationSelector = "#updateTaxCalculation"
    val formSelector = "#main-content > div > div > form"
    val checkSectionsSelector = "#p-checkSections"
    val addSectionsSelector = "#addSectionsLink"
    val notificationBannerSelector = "#main-content > div > div > div.govuk-notification-banner > div.govuk-notification-banner__content > p"
  }

  private val urlPathInYear = s"/update-and-submit-income-tax-return/$taxYear/view"
  private val urlPathEndOfYear = s"/update-and-submit-income-tax-return/$taxYearEOY/income-tax-return-overview"

  lazy val frontendAppConfig: AppConfig = app.injector.instanceOf[AppConfig]

  val controller: OverviewPageController = new OverviewPageController(
    inYearAction,
    app.injector.instanceOf[IncomeSourcesService],
    app.injector.instanceOf[LiabilityCalculationService],
    app.injector.instanceOf[TailoringUserDataRepository],
    app.injector.instanceOf[OverviewPageView],
    app.injector.instanceOf[AuthorisedAction],
    app.injector.instanceOf[ValidTaxYearListService],
    app.injector.instanceOf[ErrorHandler],
    app.injector.instanceOf[AuditService],
    app.injector.instanceOf[ExcludedJourneysService]
  )(frontendAppConfig, mcc, scala.concurrent.ExecutionContext.Implicits.global)


  case class JourneyData(tailoringKey: String, expectedText: String, incomeSourcesModel: IncomeSourcesModel)

  def journeys(user: UserScenario[CommonExpectedResults, SpecificExpectedResults]) = Seq(
    JourneyData("interest", user.commonExpectedResults.interestsLinkText, IncomeSourcesModel(interest = incomeSourcesModel.interest)),
    JourneyData("dividends", user.commonExpectedResults.dividendsLinkText, IncomeSourcesModel(dividends = incomeSourcesModel.dividends)),
    JourneyData("gift-aid", user.commonExpectedResults.giftAidLinkText, IncomeSourcesModel(giftAid = incomeSourcesModel.giftAid)),
    JourneyData("gains", user.commonExpectedResults.gainsLinkText, IncomeSourcesModel(gains = incomeSourcesModel.gains)),
    JourneyData("cis", user.commonExpectedResults.cisLinkText, IncomeSourcesModel(cis = incomeSourcesModel.cis)),
    JourneyData("employment", user.commonExpectedResults.employmentSLLinkText, IncomeSourcesModel(employment = incomeSourcesModel.employment))
  )

  val userScenarios: Seq[UserScenario[CommonExpectedResults, SpecificExpectedResults]] = Seq(
    UserScenario(isWelsh = false, isAgent = false, CommonExpectedEN, Some(ExpectedIndividualEN)),
    UserScenario(isWelsh = false, isAgent = true, CommonExpectedEN, Some(ExpectedAgentEN)),
    UserScenario(isWelsh = true, isAgent = false, CommonExpectedCY, Some(ExpectedIndividualCY)),
    UserScenario(isWelsh = true, isAgent = true, CommonExpectedCY, Some(ExpectedAgentCY))
  )

  ".show for in year" when {
    import Links._
    import Selectors._

    userScenarios.foreach { user =>
      import user.commonExpectedResults._

      val specific = user.specificExpectedResults.get

      s"language is ${welshTest(user.isWelsh)} and request is from an ${agentTest(user.isAgent)}" should {
        val headers = Seq(
          HeaderNames.COOKIE -> playSessionCookies(taxYear, validTaxYearList),
          "Csrf-Token" -> "nocheck"
        )

        "render an overview page with all the sections showing with the HREFs redirecting to the Tailoring questions" when {
          "there is no prior data, and the tailoring feature switch is on" which {
            val request = FakeRequest("GET", urlPathInYear).withHeaders(headers: _*)
            lazy val result: Future[Result] = {
              cleanDatabase(taxYear)
              insertAllJourneys()
              authoriseAgentOrIndividual(user.isAgent)
              stubGetExcludedCall(taxYear, nino, Seq(
                ExcludeJourneyModel(INTEREST, Some("hash"))
              ))
              route(customApp(tailoringEnabled = true), request, user.isWelsh).get
            }

            implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

            "has a status of OK(200)" in {
              status(result) shouldBe OK
            }

            linkCheck(interestsLinkText, Selectors.interestLinkSelector, interestsTailoringGatewayLink(taxYear))
            linkCheck(dividendsLinkText, Selectors.dividendsLinkSelector, dividendsTailoringGatewayLink(taxYear))
            linkCheck(giftAidLinkText, Selectors.giftAidLinkSelector, giftAidTailoringGatewayLink(taxYear))

            //TODO Add other journeys here
          }
        }

        "render an overview page with all sections showing status tag 'under maintenance' when feature switch is false" when {
          val request = FakeRequest("GET", urlPathInYear).withHeaders(headers: _*)
          lazy val result: Future[Result] = {
            cleanDatabase(taxYear)
            insertAllJourneys()
            stubGetExcludedCall(taxYear, nino)
            authoriseAgentOrIndividual(user.isAgent)
            route(customApp(
              dividendsEnabled = false,
              interestEnabled = false,
              giftAidEnabled = false,
              employmentEnabled = false,
              stockDividendsEnabled = false,
              studentLoansEnabled = false,
              gainsEnabled = false,
              employmentEOYEnabled = false,
              cisEnabled = false,
              pensionsEnabled = false,
              stateBenefitsEnabled = false,
              crystallisationEnabled = false
            ), request, user.isWelsh).get
          }

          implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

          "returns status of OK(200)" in {
            status(result) shouldBe OK
          }

          welshToggleCheck(welshTest(user.isWelsh))
          linkCheck(vcBreadcrumb, vcBreadcrumbSelector, Links.viewAndChangeLink(user.isAgent))
          linkCheck(startPageBreadcrumb, startPageBreadcrumbSelector, startPageBreadcrumbUrl(taxYear))
          textOnPageCheck(overviewBreadcrumb, overviewBreadcrumbSelector)

          titleCheck(specific.headingExpected, user.isWelsh)
          h1Check(specific.headingExpected + " " + caption(taxYearEOY, taxYear))
          captionCheck(caption(taxYearEOY, taxYear))
          textOnPageCheck(specific.updateIncomeTaxReturnText, updateYourIncomeTaxReturnSubheadingSelector)
          textOnPageCheck(specific.ifWeHaveInfo, ifWeHaveInformationSelector)
          textOnPageCheck(fillInTheSections, fillInTheSectionsSelector)
          textOnPageCheck(specific.inYearInsertText(taxYear), inYearInsetTextSelector)

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
          "has a gains section that says under maintenance" which {
            textOnPageCheck(underMaintenance, gainsStatusSelector)
          }
          "have a cis section that says under maintenance" which {
            textOnPageCheck(underMaintenance, cisStatusSelector)
          }
          "have a pensions section that says under maintenance" which {
            textOnPageCheck(underMaintenance, pensionsStatusSelector)
          }
          "have a state benefits section that says under maintenance" which {
            textOnPageCheck(underMaintenance, stateBenefitsStatusSelector)
          }
          "have a estimate link" which {
            linkCheck(incomeTaxAccountLink, viewEstimateSelector, Links.viewAndChangeLink(user.isAgent))
          }

          textOnPageCheck(specific.goToYourIncomeTax, goToYourIncomeTaxReturnSelector)
        }

        "render an overview page with all sections showing employment text when student loans feature switch is false" when {
          val request = FakeRequest("GET", urlPathInYear).withHeaders(headers: _*)
          lazy val result: Future[Result] = {
            authoriseAgentOrIndividual(user.isAgent)
            stubGetExcludedCall(taxYear, nino)
            route(customApp(
              studentLoansEnabled = false
            ), request, user.isWelsh).get
          }

          implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

          "returns status of OK(200)" in {
            status(result) shouldBe OK
          }

          textOnPageCheck(employmentLinkText, employmentSelector)
          welshToggleCheck(welshTest(user.isWelsh))
        }

        "render overview page with 'Not Started' status tags when there is no prior data, the employment section with" +
          "the status tag 'cannot update' user in the current taxYear and all feature switches are turned on" when {
          val request = FakeRequest("GET", urlPathInYear).withHeaders(headers: _*)
          lazy val result: Future[Result] = {
            cleanDatabase(taxYear)
            insertAllJourneys()
            stubGetExcludedCall(taxYear, nino)
            authoriseAgentOrIndividual(user.isAgent)
            stubIncomeSources(incomeSourcesModel.copy(None, None, None, None, None, None, None, None, None))
            route(app, request, user.isWelsh).get
          }

          implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

          "returns status of OK(200)" in {
            status(result) shouldBe OK
          }

          welshToggleCheck(welshTest(user.isWelsh))
          linkCheck(vcBreadcrumb, vcBreadcrumbSelector, Links.viewAndChangeLink(user.isAgent))
          linkCheck(startPageBreadcrumb, startPageBreadcrumbSelector, startPageBreadcrumbUrl(taxYear))
          textOnPageCheck(overviewBreadcrumb, overviewBreadcrumbSelector)

          titleCheck(specific.headingExpected, user.isWelsh)
          h1Check(specific.headingExpected + " " + caption(taxYearEOY, taxYear))
          captionCheck(caption(taxYearEOY, taxYear))
          textOnPageCheck(specific.updateIncomeTaxReturnText, updateYourIncomeTaxReturnSubheadingSelector)
          textOnPageCheck(specific.ifWeHaveInfo, ifWeHaveInformationSelector)
          textOnPageCheck(fillInTheSections, fillInTheSectionsSelector)
          textOnPageCheck(specific.inYearInsertText(taxYear), inYearInsetTextSelector)

          "has a dividends section" which {
            linkCheck(dividendsLinkText, dividendsLinkSelector, dividendsLink(taxYear))
            textOnPageCheck(notStartedText, dividendsStatusSelector)
          }

          "has an interest section" which {
            linkCheck(interestsLinkText, interestLinkSelector, interestsLink(taxYear))
            textOnPageCheck(notStartedText, interestStatusSelector)
          }

          "has an employment section " which {
            textOnPageCheck(employmentSLLinkText, employmentSelector)
            textOnPageCheck(cannotUpdateText, employmentStatusSelector)
          }

          "has an gains section " which {
            textOnPageCheck(gainsLinkText, gainsSelector)
            textOnPageCheck(notStartedText, gainsStatusSelector)
          }

          "has a cis section " which {
            textOnPageCheck(cisLinkText, cisSelector)
            textOnPageCheck(cannotUpdateText, cisStatusSelector)
            linkCheck(cisLinkText, cisLinkSelector, cisLink(taxYear))
          }

          "has a pensions section " which {
            textOnPageCheck(pensionsLinkText, pensionsSelector)
            textOnPageCheck(notStartedText, pensionsStatusSelector)
          }

          "has a state benefits section " which {
            textOnPageCheck(stateBenefitsLinkText, stateBenefitsSelector)
            textOnPageCheck(notStartedText, stateBenefitsStatusSelector)
          }

          "has a donations to charity section" which {
            linkCheck(giftAidLinkText, giftAidLinkSelector, appConfig.personalIncomeTaxGiftAidUrl(taxYear))
            textOnPageCheck(notStartedText, giftAidStatusSelector)
          }

          formPostLinkCheck(controllers.routes.OverviewPageController.inYearEstimate(taxYear).url, formSelector)
          buttonCheck(updateTaxCalculation, updateTaxCalculationSelector, None)
        }

        "render overview page with status tag 'Not Started' for interest when interest income source is None " when {
          val request = FakeRequest("GET", urlPathInYear).withHeaders(headers: _*)
          lazy val result: Future[Result] = {
            cleanDatabase(taxYear)
            insertAllJourneys()
            stubGetExcludedCall(taxYear, nino)
            authoriseAgentOrIndividual(user.isAgent)
            stubIncomeSources(incomeSourcesModel.copy(interest = None))
            route(app, request, user.isWelsh).get
          }

          implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

          "returns status of OK(200)" in {
            status(result) shouldBe OK
          }

          welshToggleCheck(welshTest(user.isWelsh))
          linkCheck(vcBreadcrumb, vcBreadcrumbSelector, Links.viewAndChangeLink(user.isAgent))
          linkCheck(startPageBreadcrumb, startPageBreadcrumbSelector, startPageBreadcrumbUrl(taxYear))
          textOnPageCheck(overviewBreadcrumb, overviewBreadcrumbSelector)

          titleCheck(specific.headingExpected, user.isWelsh)
          h1Check(specific.headingExpected + " " + caption(taxYearEOY, taxYear))
          captionCheck(caption(taxYearEOY, taxYear))
          textOnPageCheck(specific.updateIncomeTaxReturnText, updateYourIncomeTaxReturnSubheadingSelector)
          textOnPageCheck(specific.ifWeHaveInfo, ifWeHaveInformationSelector)
          textOnPageCheck(fillInTheSections, fillInTheSectionsSelector)
          textOnPageCheck(specific.inYearInsertText(taxYear), inYearInsetTextSelector)

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
            linkCheck(employmentSLLinkText, employmentLinkSelector, employmentLink(taxYear))
            textOnPageCheck(updatedText, employmentStatusSelector)
          }

          "has a gains section" which {
            linkCheck(gainsLinkText, gainsLinkSelector, gainsLink(taxYear))
            textOnPageCheck(updatedText, gainsStatusSelector)
          }

          "has a cis section" which {
            linkCheck(cisLinkText, cisLinkSelector, cisLink(taxYear))
            textOnPageCheck(updatedText, cisStatusSelector)
          }

          "has a pensions section" which {
            linkCheck(pensionsLinkText, pensionsLinkSelector, pensionsLink(taxYear))
            textOnPageCheck(updatedText, pensionsStatusSelector)
          }

          "has a state benefits section" which {
            linkCheck(stateBenefitsLinkText, stateBenefitsLinkSelector, stateBenefitsLink(taxYear))
            textOnPageCheck(updatedText, stateBenefitsStatusSelector)
          }

          formPostLinkCheck(controllers.routes.OverviewPageController.inYearEstimate(taxYear).url, formSelector)
          buttonCheck(updateTaxCalculation, updateTaxCalculationSelector, None)
        }

        "render overview page with correct status tags when there is prior data and user is in the current taxYear" should {
          "have the status as 'Not Started' for interest when interest income source is None" when {
            val incomeSources = incomeSourcesModel.copy(interest = None)
            val request = FakeRequest("GET", urlPathInYear).withHeaders(headers: _*)
            lazy val result: Future[Result] = {
              cleanDatabase(taxYear)
              insertAllJourneys()
              stubGetExcludedCall(taxYear, nino)
              authoriseAgentOrIndividual(user.isAgent)
              stubIncomeSources(incomeSources)
              route(app, request, user.isWelsh).get
            }

            implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

            "returns status of OK(200)" in {
              status(result) shouldBe OK
            }

            welshToggleCheck(welshTest(user.isWelsh))
            linkCheck(vcBreadcrumb, vcBreadcrumbSelector, Links.viewAndChangeLink(user.isAgent))
            linkCheck(startPageBreadcrumb, startPageBreadcrumbSelector, startPageBreadcrumbUrl(taxYear))
            textOnPageCheck(overviewBreadcrumb, overviewBreadcrumbSelector)

            titleCheck(specific.headingExpected, user.isWelsh)
            h1Check(specific.headingExpected + " " + caption(taxYearEOY, taxYear))
            captionCheck(caption(taxYearEOY, taxYear))
            textOnPageCheck(specific.updateIncomeTaxReturnText, updateYourIncomeTaxReturnSubheadingSelector)
            textOnPageCheck(specific.ifWeHaveInfo, ifWeHaveInformationSelector)
            textOnPageCheck(fillInTheSections, fillInTheSectionsSelector)
            textOnPageCheck(specific.inYearInsertText(taxYear), inYearInsetTextSelector)

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
              linkCheck(employmentSLLinkText, employmentLinkSelector, employmentLink(taxYear))
              textOnPageCheck(updatedText, employmentStatusSelector)
            }

            "has a cis section" which {
              linkCheck(cisLinkText, cisLinkSelector, cisLink(taxYear))
              textOnPageCheck(updatedText, cisStatusSelector)
            }

            "has a pensions section" which {
              linkCheck(pensionsLinkText, pensionsLinkSelector, pensionsLink(taxYear))
              textOnPageCheck(updatedText, pensionsStatusSelector)
            }

            "has a state benefits section" which {
              linkCheck(stateBenefitsLinkText, stateBenefitsLinkSelector, stateBenefitsLink(taxYear))
              textOnPageCheck(updatedText, stateBenefitsStatusSelector)
            }

            formPostLinkCheck(controllers.routes.OverviewPageController.inYearEstimate(taxYear).url, formSelector)
            buttonCheck(updateTaxCalculation, updateTaxCalculationSelector, None)
          }
        }

        "render overview page with correct status tags when a service returns UNAVAILABLE and user is in the current taxYear" should {
          "have the status as 'Under Maintenance' for interest when interest income source is unavailable" when {
            val incomeSources = incomeSourcesModel.copy(
              errors = Some(List(("interest", APIErrorBodyModel("UNAVAILABLE", "Something went wrong")))),
              interest = None)
            val request = FakeRequest("GET", urlPathInYear).withHeaders(headers: _*)
            lazy val result: Future[Result] = {
              cleanDatabase(taxYear)
              insertAllJourneys()
              stubGetExcludedCall(taxYear, nino)
              authoriseAgentOrIndividual(user.isAgent)
              stubIncomeSources(incomeSources)
              route(app, request, user.isWelsh).get
            }

            implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

            "returns status of OK(200)" in {
              status(result) shouldBe OK
            }

            welshToggleCheck(welshTest(user.isWelsh))
            linkCheck(vcBreadcrumb, vcBreadcrumbSelector, Links.viewAndChangeLink(user.isAgent))
            linkCheck(startPageBreadcrumb, startPageBreadcrumbSelector, startPageBreadcrumbUrl(taxYear))
            textOnPageCheck(overviewBreadcrumb, overviewBreadcrumbSelector)

            titleCheck(specific.headingExpected, user.isWelsh)
            h1Check(specific.headingExpected + " " + caption(taxYearEOY, taxYear))
            captionCheck(caption(taxYearEOY, taxYear))
            textOnPageCheck(specific.updateIncomeTaxReturnText, updateYourIncomeTaxReturnSubheadingSelector)
            textOnPageCheck(specific.ifWeHaveInfo, ifWeHaveInformationSelector)
            textOnPageCheck(fillInTheSections, fillInTheSectionsSelector)
            textOnPageCheck(specific.inYearInsertText(taxYear), inYearInsetTextSelector)

            "has a dividends section" which {
              linkCheck(dividendsLinkText, dividendsLinkSelector, dividendsLinkWithPriorData(taxYear))
              textOnPageCheck(updatedText, dividendsStatusSelector)
            }

            "has an interest section with status of 'Under Maintenance'" which {
              textOnPageCheck(underMaintenance, interestStatusSelector)
            }

            "has a donations to charity section" which {
              linkCheck(giftAidLinkText, giftAidLinkSelector, appConfig.personalIncomeTaxGiftAidSubmissionCYAUrl(taxYear))
              textOnPageCheck(updatedText, giftAidStatusSelector)
            }

            "has an employment section" which {
              linkCheck(employmentSLLinkText, employmentLinkSelector, employmentLink(taxYear))
              textOnPageCheck(updatedText, employmentStatusSelector)
            }

            "has an gains section" which {
              linkCheck(gainsLinkText, gainsLinkSelector, gainsLink(taxYear))
              textOnPageCheck(updatedText, gainsStatusSelector)
            }

            "has a cis section" which {
              linkCheck(cisLinkText, cisLinkSelector, cisLink(taxYear))
              textOnPageCheck(updatedText, cisStatusSelector)
            }

            "has a pensions section" which {
              linkCheck(pensionsLinkText, pensionsLinkSelector, pensionsLink(taxYear))
              textOnPageCheck(updatedText, pensionsStatusSelector)
            }

            "has a state benefits section" which {
              linkCheck(stateBenefitsLinkText, stateBenefitsLinkSelector, stateBenefitsLink(taxYear))
              textOnPageCheck(updatedText, stateBenefitsStatusSelector)
            }

            formPostLinkCheck(controllers.routes.OverviewPageController.inYearEstimate(taxYear).url, formSelector)
            buttonCheck(updateTaxCalculation, updateTaxCalculationSelector, None)
          }
        }

        "interest income source is defined and the untaxed and taxed accounts do not have amounts" which {
          val incomeSources = incomeSourcesModel.copy(interest = Some(Seq(InterestModel("TestName", "TestSource", None, None))))
          val request = FakeRequest("GET", urlPathInYear).withHeaders(headers: _*)
          lazy val result: Future[Result] = {
            cleanDatabase(taxYear)
            insertAllJourneys()
            stubGetExcludedCall(taxYear, nino)
            authoriseAgentOrIndividual(user.isAgent)
            stubIncomeSources(incomeSources)
            route(app, request, user.isWelsh).get
          }

          implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

          "returns status of OK(200)" in {
            status(result) shouldBe OK
          }

          welshToggleCheck(welshTest(user.isWelsh))
          linkCheck(vcBreadcrumb, vcBreadcrumbSelector, Links.viewAndChangeLink(user.isAgent))
          linkCheck(startPageBreadcrumb, startPageBreadcrumbSelector, startPageBreadcrumbUrl(taxYear))
          textOnPageCheck(overviewBreadcrumb, overviewBreadcrumbSelector)

          titleCheck(specific.headingExpected, user.isWelsh)
          h1Check(specific.headingExpected + " " + caption(taxYearEOY, taxYear))
          captionCheck(caption(taxYearEOY, taxYear))
          textOnPageCheck(specific.updateIncomeTaxReturnText, updateYourIncomeTaxReturnSubheadingSelector)
          textOnPageCheck(specific.ifWeHaveInfo, ifWeHaveInformationSelector)
          textOnPageCheck(fillInTheSections, fillInTheSectionsSelector)
          textOnPageCheck(specific.inYearInsertText(taxYear), inYearInsetTextSelector)

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
            linkCheck(employmentSLLinkText, employmentLinkSelector, employmentLink(taxYear))
            textOnPageCheck(updatedText, employmentStatusSelector)
          }

          "has an gains section" which {
            linkCheck(gainsLinkText, gainsLinkSelector, gainsLink(taxYear))
            textOnPageCheck(updatedText, gainsStatusSelector)
          }

          "has a cis section" which {
            linkCheck(cisLinkText, cisLinkSelector, cisLink(taxYear))
            textOnPageCheck(updatedText, cisStatusSelector)
          }

          "has a pensions section" which {
            linkCheck(pensionsLinkText, pensionsLinkSelector, pensionsLink(taxYear))
            textOnPageCheck(updatedText, pensionsStatusSelector)
          }

          "has a state benefits section" which {
            linkCheck(stateBenefitsLinkText, stateBenefitsLinkSelector, stateBenefitsLink(taxYear))
            textOnPageCheck(updatedText, stateBenefitsStatusSelector)
          }

          formPostLinkCheck(controllers.routes.OverviewPageController.inYearEstimate(taxYear).url, formSelector)
          buttonCheck(updateTaxCalculation, updateTaxCalculationSelector, None)
        }

        "have the status as 'Updated' for interest" when {
          val interestsModelWithAmounts: Option[Seq[InterestModel]] = Some(Seq(InterestModel("TestName", "TestSource", Some(100.00), Some(100.00))))

          "interest income source is defined and the untaxed and taxed accounts have amounts" which {
            val incomeSources = incomeSourcesModel.copy(interest = interestsModelWithAmounts)
            val request = FakeRequest("GET", urlPathInYear).withHeaders(headers: _*)
            lazy val result: Future[Result] = {
              cleanDatabase(taxYear)
              insertAllJourneys()
              stubGetExcludedCall(taxYear, nino)
              authoriseAgentOrIndividual(user.isAgent)
              stubIncomeSources(incomeSources)
              route(app, request, user.isWelsh).get
            }

            implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

            "returns status of OK(200)" in {
              status(result) shouldBe OK
            }

            welshToggleCheck(welshTest(user.isWelsh))
            linkCheck(vcBreadcrumb, vcBreadcrumbSelector, Links.viewAndChangeLink(user.isAgent))
            linkCheck(startPageBreadcrumb, startPageBreadcrumbSelector, startPageBreadcrumbUrl(taxYear))
            textOnPageCheck(overviewBreadcrumb, overviewBreadcrumbSelector)

            titleCheck(specific.headingExpected, user.isWelsh)
            h1Check(specific.headingExpected + " " + caption(taxYearEOY, taxYear))
            captionCheck(caption(taxYearEOY, taxYear))
            textOnPageCheck(specific.updateIncomeTaxReturnText, updateYourIncomeTaxReturnSubheadingSelector)
            textOnPageCheck(specific.ifWeHaveInfo, ifWeHaveInformationSelector)
            textOnPageCheck(fillInTheSections, fillInTheSectionsSelector)
            textOnPageCheck(specific.inYearInsertText(taxYear), inYearInsetTextSelector)

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
              linkCheck(employmentSLLinkText, employmentLinkSelector, employmentLink(taxYear))
              textOnPageCheck(updatedText, employmentStatusSelector)
            }

            "has a gains section" which {
              linkCheck(gainsLinkText, gainsLinkSelector, gainsLink(taxYear))
              textOnPageCheck(updatedText, gainsStatusSelector)
            }

            "has a cis section" which {
              linkCheck(cisLinkText, cisLinkSelector, cisLink(taxYear))
              textOnPageCheck(updatedText, cisStatusSelector)
            }

            "has a pensions section" which {
              linkCheck(pensionsLinkText, pensionsLinkSelector, pensionsLink(taxYear))
              textOnPageCheck(updatedText, pensionsStatusSelector)
            }

            "has a state benefits section" which {
              linkCheck(stateBenefitsLinkText, stateBenefitsLinkSelector, stateBenefitsLink(taxYear))
              textOnPageCheck(updatedText, stateBenefitsStatusSelector)
            }

            formPostLinkCheck(controllers.routes.OverviewPageController.inYearEstimate(taxYear).url, formSelector)
            buttonCheck(updateTaxCalculation, updateTaxCalculationSelector, None)
          }
        }

        "have all journeys with a plural notification banner" when {
          val request = FakeRequest("GET", urlPathInYear).withHeaders(headers: _*)
          lazy val result: Future[Result] = {
            cleanDatabase(taxYear)
            authoriseAgentOrIndividual(user.isAgent)
            stubGetExcludedCall(taxYear, nino)
            // TODO rm copy when stockDividends fully implemented
            stubIncomeSources(incomeSourcesModel.copy(stockDividends = None))
            route(customApp(tailoringEnabled = true), request, user.isWelsh).get
          }

          implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

          "returns status of OK(200)" in {
            status(result) shouldBe OK
          }

          welshToggleCheck(welshTest(user.isWelsh))
          linkCheck(vcBreadcrumb, vcBreadcrumbSelector, Links.viewAndChangeLink(user.isAgent))
          linkCheck(startPageBreadcrumb, startPageBreadcrumbSelector, startPageBreadcrumbUrl(taxYear))
          textOnPageCheck(overviewBreadcrumb, overviewBreadcrumbSelector)

          titleCheck(specific.headingExpected, user.isWelsh)
          h1Check(specific.headingExpected + " " + caption(taxYearEOY, taxYear))
          captionCheck(caption(taxYearEOY, taxYear))
          textOnPageCheck(specific.updateIncomeTaxReturnText, updateYourIncomeTaxReturnSubheadingSelector)
          textOnPageCheck(checkSectionsText, checkSectionsSelector)
          textOnPageCheck(specific.notificationBannerPlural, notificationBannerSelector)

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
            linkCheck(employmentSLLinkText, employmentLinkSelector, employmentLink(taxYear))
            textOnPageCheck(updatedText, employmentStatusSelector)
          }

          "has an gains section" which {
            linkCheck(gainsLinkText, gainsLinkSelector, gainsLink(taxYear))
            textOnPageCheck(updatedText, gainsStatusSelector)
          }

          "has a cis section" which {
            linkCheck(cisLinkText, cisLinkSelector, cisLink(taxYear))
            textOnPageCheck(updatedText, cisStatusSelector)
          }

          "has a pensions section" which {
            linkCheck(pensionsLinkText, pensionsLinkSelector, pensionsLink(taxYear))
            textOnPageCheck(updatedText, pensionsStatusSelector)
          }

          "has a state benefits section" which {
            linkCheck(stateBenefitsLinkText, stateBenefitsLinkSelector, stateBenefitsLink(taxYear))
            textOnPageCheck(updatedText, stateBenefitsStatusSelector)
          }

          "have a add sections link " which {
            linkCheck(specific.addSections, addSectionsSelector, Links.addSectionsLink(taxYear))
          }

          formPostLinkCheck(controllers.routes.OverviewPageController.inYearEstimate(taxYear).url, formSelector)
          buttonCheck(updateTaxCalculation, updateTaxCalculationSelector, None)
        }

        "display all rows" when {
          "there are no activated journeys in the db and the tailoring feature switch is off" which {
            val request = FakeRequest("GET", urlPathInYear).withHeaders(headers: _*)
            lazy val result = {
              cleanDatabase(taxYear)
              stubGetExcludedCall(taxYear, nino)
              authoriseAgentOrIndividual(user.isAgent)
              route(app, request, user.isWelsh).get
            }

            implicit val document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

            "has a status of OK(200)" in {
              status(result) shouldBe OK
            }

            textOnPageCheck(user.commonExpectedResults.interestsLinkText, interestLinkSelector)
            textOnPageCheck(user.commonExpectedResults.dividendsLinkText, dividendsLinkSelector)
            textOnPageCheck(user.commonExpectedResults.giftAidLinkText, giftAidLinkSelector)
            textOnPageCheck(user.commonExpectedResults.cisLinkText, cisSelector)
            textOnPageCheck(user.commonExpectedResults.gainsLinkText, gainsSelector)
            textOnPageCheck(user.commonExpectedResults.pensionsLinkText, pensionsSelector)
            textOnPageCheck(user.commonExpectedResults.employmentSLLinkText, employmentSelector)
          }
        }
      }
    }

    "an error is returned obtaining income sources" should {
      val headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear, validTaxYearList), "Csrf-Token" -> "nocheck")
      val request = FakeRequest("GET", urlPathInYear).withHeaders(headers: _*)

      lazy val result: Future[Result] = {
        cleanDatabase(taxYear)
        insertAllJourneys()
        stubGetExcludedCall(taxYear, nino)
        authoriseAgentOrIndividual(user.isAgent)
        stubIncomeSources(incomeSourcesModel.copy(None, None, None, None, None, None), INTERNAL_SERVER_ERROR)
        route(app, request).get
      }

      "return a status of INTERNAL_SERVER_ERROR(500)" in {
        status(result) shouldBe INTERNAL_SERVER_ERROR
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
        val headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYearEOY, validTaxYearList), "Csrf-Token" -> "nocheck")

        "render an overview page with all sections turned off" when {
          val request = FakeRequest("GET", urlPathEndOfYear).withHeaders(headers: _*)

          lazy val result: Future[Result] = {
            cleanDatabase(taxYearEOY)
            insertAllJourneys(endOfYear = true)
            stubGetExcludedCall(taxYearEOY, nino)
            authoriseAgentOrIndividual(user.isAgent)
            route(customApp(
              dividendsEnabled = false,
              interestEnabled = false,
              giftAidEnabled = false,
              employmentEnabled = false,
              stockDividendsEnabled = false,
              studentLoansEnabled = false,
              gainsEnabled = false,
              employmentEOYEnabled = false,
              cisEnabled = false,
              pensionsEnabled = false,
              stateBenefitsEnabled = false,
              crystallisationEnabled = false
            ), request, user.isWelsh).get
          }

          implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

          "returns status of OK(200)" in {
            status(result) shouldBe OK
          }

          welshToggleCheck(welshTest(user.isWelsh))
          linkCheck(vcBreadcrumb, vcBreadcrumbSelector, Links.viewAndChangeLink(user.isAgent))
          linkCheck(startPageBreadcrumb, startPageBreadcrumbSelector, Links.startPageBreadcrumbUrl(taxYearEOY))
          textOnPageCheck(overviewBreadcrumb, overviewBreadcrumbSelector)

          titleCheck(specific.headingExpected, user.isWelsh)
          h1Check(specific.headingExpected + " " + caption(taxYearEndOfYearMinusOne, taxYearEOY))
          captionCheck(caption(taxYearEndOfYearMinusOne, taxYearEOY))
          textOnPageCheck(specific.ifWeHaveInfo, ifWeHaveInformationSelector)
          textOnPageCheck(fillInTheSections, fillInTheSectionsSelector)
          textOnPageCheck(specific.updateIncomeTaxReturnText, updateYourIncomeTaxReturnSubheadingSelector)

          "have a dividends section that says under maintenance" which {
            textOnPageCheck(underMaintenance, dividendsStatusSelector)
          }

          "have an interest section that says under maintenance" which {
            textOnPageCheck(underMaintenance, interestStatusSelector)
          }

          "have an employment section that says under maintenance" which {
            textOnPageCheck(underMaintenance, employmentStatusSelector)
          }

          "have an gains section that says under maintenance" which {
            textOnPageCheck(underMaintenance, gainsStatusSelector)
          }

          "have a cis section that says under maintenance" which {
            textOnPageCheck(underMaintenance, cisStatusSelector)
          }

          "have a pensions section that says under maintenance" which {
            textOnPageCheck(underMaintenance, pensionsStatusSelector)
          }

          "have a state benefits section that says under maintenance" which {
            textOnPageCheck(underMaintenance, stateBenefitsStatusSelector)
          }

          "has a donations to charity section" which {
            textOnPageCheck(underMaintenance, giftAidStatusSelector)
          }

          textOnPageCheck(specific.goToYourIncomeTax, goToYourIncomeTaxReturnSelector)

          "have a estimate link" which {
            linkCheck(incomeTaxAccountLink, viewEstimateSelector, Links.viewAndChangeLink(user.isAgent))
          }

          textOnPageCheck(specific.submitReturnHeaderEOY, submitReturnEOYSelector)
          textOnPageCheck(specific.submitReturnText, submitReturnTextEOYSelector)
          formPostLinkCheck(endOfYearContinueLink, formSelector)
        }

        "render an overview page with prior data" when {
          val request = FakeRequest("GET", urlPathEndOfYear).withHeaders(headers: _*)
          lazy val result: Future[Result] = {
            cleanDatabase(taxYearEOY)
            insertAllJourneys(endOfYear = true)
            stubGetExcludedCall(taxYearEOY, nino)
            authoriseAgentOrIndividual(user.isAgent)
            stubIncomeSourcesEndOfYear
            route(customApp(), request, user.isWelsh).get
          }

          implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

          "returns status of OK(200)" in {
            status(result) shouldBe OK
          }

          welshToggleCheck(welshTest(user.isWelsh))
          linkCheck(vcBreadcrumb, vcBreadcrumbSelector, Links.viewAndChangeLink(user.isAgent))
          linkCheck(startPageBreadcrumb, startPageBreadcrumbSelector, Links.startPageBreadcrumbUrl(taxYearEOY))
          textOnPageCheck(overviewBreadcrumb, overviewBreadcrumbSelector)

          titleCheck(specific.headingExpected, user.isWelsh)
          h1Check(specific.headingExpected + " " + caption(taxYearEndOfYearMinusOne, taxYearEOY))
          captionCheck(caption(taxYearEndOfYearMinusOne, taxYearEOY))
          textOnPageCheck(specific.ifWeHaveInfo, ifWeHaveInformationSelector)
          textOnPageCheck(fillInTheSections, fillInTheSectionsSelector)
          textOnPageCheck(specific.updateIncomeTaxReturnText, updateYourIncomeTaxReturnSubheadingSelector)

          "has a dividends section" which {
            linkCheck(dividendsLinkText, dividendsLinkSelector, dividendsLinkWithPriorData(taxYearEOY))
            textOnPageCheck(updatedText, dividendsStatusSelector)
          }

          "has an interest section" which {
            linkCheck(interestsLinkText, interestLinkSelector, interestsLinkWithPriorData(taxYearEOY))
            textOnPageCheck(updatedText, interestStatusSelector)
          }

          "has an employment section" which {
            linkCheck(employmentSLLinkText, employmentLinkSelector, employmentLink(taxYearEOY))
            textOnPageCheck(updatedText, employmentStatusSelector)
          }

          "has a gains section" which {
            linkCheck(gainsLinkText, gainsLinkSelector, gainsLink(taxYearEOY))
            textOnPageCheck(updatedText, gainsStatusSelector)
          }

          "has a cis section" which {
            linkCheck(cisLinkText, cisLinkSelector, cisLink(taxYearEOY))
            textOnPageCheck(updatedText, cisStatusSelector)
          }

          "has a pensions section" which {
            linkCheck(pensionsLinkText, pensionsLinkSelector, pensionsLink(taxYearEOY))
            textOnPageCheck(updatedText, pensionsStatusSelector)
          }

          "has a state benefits section" which {
            linkCheck(stateBenefitsLinkText, stateBenefitsLinkSelector, stateBenefitsLink(taxYearEOY))
            textOnPageCheck(updatedText, stateBenefitsStatusSelector)
          }

          "has a donations to charity section" which {
            linkCheck(giftAidLinkText, giftAidLinkSelector, appConfig.personalIncomeTaxGiftAidSubmissionCYAUrl(taxYearEOY))
            textOnPageCheck(updatedText, giftAidStatusSelector)
          }

          textOnPageCheck(specific.submitReturnHeaderEOY, submitReturnEOYSelector)
          textOnPageCheck(specific.submitReturnText, submitReturnTextEOYSelector)
          formPostLinkCheck(endOfYearContinueLink, formSelector)
        }

        "render overview page with 'Started' status tags when there is prior data and the employment section is clickable with" +
          "the status tag 'To do' when user is in a previous year" when {
          val previousYearHeaders = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYearEOY, validTaxYearList), "Csrf-Token" -> "nocheck")
          val previousYearUrl = s"/update-and-submit-income-tax-return/$taxYearEOY/income-tax-return-overview"
          val taxYearMinusTwo = taxYearEOY - 1
          val request = FakeRequest("GET", previousYearUrl).withHeaders(previousYearHeaders: _*)

          lazy val result: Future[Result] = {
            cleanDatabase(taxYearEOY)
            insertAllJourneys(endOfYear = true)
            stubGetExcludedCall(taxYearEOY, nino)
            authoriseAgentOrIndividual(user.isAgent)
            stubIncomeSources(incomeSourcesModel)
            route(customApp(), request, user.isWelsh).get
          }

          implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

          "returns status of OK(200)" in {
            status(result) shouldBe OK
          }

          welshToggleCheck(welshTest(user.isWelsh))
          linkCheck(vcBreadcrumb, vcBreadcrumbSelector, Links.viewAndChangeLink(user.isAgent))
          linkCheck(startPageBreadcrumb, startPageBreadcrumbSelector, startPageBreadcrumbUrl(taxYearEOY))
          textOnPageCheck(overviewBreadcrumb, overviewBreadcrumbSelector)

          titleCheck(specific.headingExpected, user.isWelsh)
          h1Check(specific.headingExpected + " " + caption(taxYearMinusTwo, taxYearEOY))
          captionCheck(caption(taxYearMinusTwo, taxYearEOY))
          textOnPageCheck(specific.updateIncomeTaxReturnText, updateYourIncomeTaxReturnSubheadingSelector)
          textOnPageCheck(specific.ifWeHaveInfo, ifWeHaveInformationSelector)
          textOnPageCheck(fillInTheSections, fillInTheSectionsSelector)

          "has a dividends section" which {
            linkCheck(dividendsLinkText, dividendsLinkSelector, dividendsLink(taxYearEOY))
            textOnPageCheck(notStartedText, dividendsStatusSelector)
          }

          "has an interest section" which {
            linkCheck(interestsLinkText, interestLinkSelector, interestsLink(taxYearEOY))
            textOnPageCheck(notStartedText, interestStatusSelector)
          }

          "has an employment section " which {
            linkCheck(employmentSLLinkText, employmentLinkSelector, employmentLink(taxYearEOY))
            textOnPageCheck(notStartedText, employmentStatusSelector)
          }

          "has a cis section " which {
            linkCheck(cisLinkText, cisLinkSelector, cisLink(taxYearEOY))
            textOnPageCheck(notStartedText, cisStatusSelector)
          }

          "has a gains section " which {
            linkCheck(gainsLinkText, gainsLinkSelector, gainsLink(taxYearEOY))
            textOnPageCheck(notStartedText, gainsStatusSelector)
          }

          "has a pensions section " which {
            linkCheck(pensionsLinkText, pensionsLinkSelector, pensionsLink(taxYearEOY))
            textOnPageCheck(notStartedText, pensionsStatusSelector)
          }

          "has a state benefits section " which {
            linkCheck(stateBenefitsLinkText, stateBenefitsLinkSelector, stateBenefitsLink(taxYearEOY))
            textOnPageCheck(notStartedText, stateBenefitsStatusSelector)
          }

          "has a donations to charity section" which {
            linkCheck(giftAidLinkText, giftAidLinkSelector, appConfig.personalIncomeTaxGiftAidUrl(taxYearEOY))
            textOnPageCheck(notStartedText, giftAidStatusSelector)
          }

          textOnPageCheck(specific.submitReturnHeaderEOY, submitReturnEOYSelector)
          textOnPageCheck(specific.submitReturnText, submitReturnTextEOYSelector)
          formPostLinkCheck(endOfYearContinueLink, formSelector)
        }

        "render the overview page EOY with 'Started' status tags for each section and 'Cannot Update' for the employment section" +
          "when employmentEOYEnabled feature switch is false" when {
          val previousYearHeaders = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYearEOY, validTaxYearList), "Csrf-Token" -> "nocheck")
          val previousYearUrl = s"/update-and-submit-income-tax-return/$taxYearEOY/income-tax-return-overview"
          val taxYearMinusTwo = taxYearEOY - 1
          val request = FakeRequest("GET", previousYearUrl).withHeaders(previousYearHeaders: _*)

          lazy val result: Future[Result] = {
            cleanDatabase(taxYearEOY)
            insertAllJourneys(endOfYear = true)
            stubGetExcludedCall(taxYearEOY, nino)
            authoriseAgentOrIndividual(user.isAgent)
            stubIncomeSources(incomeSourcesModel)
            route(customApp(employmentEOYEnabled = false), request, user.isWelsh).get
          }

          implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

          "returns status of OK(200)" in {
            status(result) shouldBe OK
          }

          welshToggleCheck(welshTest(user.isWelsh))
          linkCheck(vcBreadcrumb, vcBreadcrumbSelector, Links.viewAndChangeLink(user.isAgent))
          linkCheck(startPageBreadcrumb, startPageBreadcrumbSelector, startPageBreadcrumbUrl(taxYearEOY))
          textOnPageCheck(overviewBreadcrumb, overviewBreadcrumbSelector)

          titleCheck(specific.headingExpected, user.isWelsh)
          h1Check(specific.headingExpected + " " + caption(taxYearMinusTwo, taxYearEOY))
          captionCheck(caption(taxYearMinusTwo, taxYearEOY))
          textOnPageCheck(specific.updateIncomeTaxReturnText, updateYourIncomeTaxReturnSubheadingSelector)
          textOnPageCheck(specific.ifWeHaveInfo, ifWeHaveInformationSelector)
          textOnPageCheck(fillInTheSections, fillInTheSectionsSelector)

          "has a dividends section" which {
            linkCheck(dividendsLinkText, dividendsLinkSelector, dividendsLink(taxYearEOY))
            textOnPageCheck(notStartedText, dividendsStatusSelector)
          }

          "has an interest section" which {
            linkCheck(interestsLinkText, interestLinkSelector, interestsLink(taxYearEOY))
            textOnPageCheck(notStartedText, interestStatusSelector)
          }

          "has an employment section " which {
            textOnPageCheck(employmentSLLinkText, employmentSelector)
            textOnPageCheck(cannotUpdateText, employmentStatusSelector)
          }

          "has a cis section " which {
            linkCheck(cisLinkText, cisLinkSelector, cisLink(taxYearEOY))
            textOnPageCheck(notStartedText, cisStatusSelector)
          }

          "has a gains section " which {
            linkCheck(gainsLinkText, gainsLinkSelector, gainsLink(taxYearEOY))
            textOnPageCheck(notStartedText, gainsStatusSelector)
          }

          "has a pensions section " which {
            linkCheck(pensionsLinkText, pensionsLinkSelector, pensionsLink(taxYearEOY))
            textOnPageCheck(notStartedText, pensionsStatusSelector)
          }

          "has a state benefits section " which {
            linkCheck(stateBenefitsLinkText, stateBenefitsLinkSelector, stateBenefitsLink(taxYearEOY))
            textOnPageCheck(notStartedText, stateBenefitsStatusSelector)
          }

          "has a donations to charity section" which {
            linkCheck(giftAidLinkText, giftAidLinkSelector, appConfig.personalIncomeTaxGiftAidUrl(taxYearEOY))
            textOnPageCheck(notStartedText, giftAidStatusSelector)
          }

          textOnPageCheck(specific.submitReturnHeaderEOY, submitReturnEOYSelector)
          textOnPageCheck(specific.submitReturnText, submitReturnTextEOYSelector)
          formPostLinkCheck(endOfYearContinueLink, formSelector)
        }
      }
    }
  }

  "Hitting the show endpoint" should {
    s"return an OK (200) and no prior data" when {
      "all auth requirements are met" in {
        val result = {
          authoriseIndividual()
          stubGetExcludedCall(taxYear, nino)
          await(controller.show(taxYear)(fakeRequest.withSession(
            SessionKeys.authToken -> "mock-bearer-token",
            SessionValues.TAX_YEAR -> s"$taxYear",
            SessionValues.VALID_TAX_YEARS -> validTaxYearList.mkString(","))
          ))
        }

        result.header.status shouldBe OK
      }
    }

    "return a SEE_OTHER (303) when end of year" when {
      "all auth requirements are met" in {
        val result = {
          authoriseIndividual()
          stubGetExcludedCall(taxYear, nino)
          await(controller.show(taxYearEOY)(fakeRequest.withSession(
            SessionKeys.authToken -> "mock-bearer-token",
            SessionValues.TAX_YEAR -> s"$taxYearEOY",
            SessionValues.VALID_TAX_YEARS -> validTaxYearList.mkString(","),
            SessionKeys.sessionId -> "sessionId-0101010101")
          ))
        }

        result.header.status shouldBe SEE_OTHER
      }
    }

    s"return an OK (200) and no prior data and a session id" when {
      "all auth requirements are met" in {
        val result = {
          authoriseIndividual()
          stubGetExcludedCall(taxYear, nino)
          await(controller.show(taxYear)(fakeRequest.withSession(
            SessionKeys.authToken -> "mock-bearer-token",
            SessionValues.TAX_YEAR -> s"$taxYear",
            SessionValues.VALID_TAX_YEARS -> validTaxYearList.mkString(","),
            SessionKeys.sessionId -> "sessionId-0101010101")
          ))
        }

        result.header.status shouldBe OK
      }
    }

    s"return an OK (200) with prior data" when {
      "all auth requirements are met" in {
        val result = {
          stubIncomeSources
          authoriseIndividual()
          stubGetExcludedCall(taxYear, nino)
          await(controller.show(taxYear)(fakeRequest.withSession(
            SessionKeys.authToken -> "mock-bearer-token",
            SessionValues.TAX_YEAR -> s"$taxYear",
            SessionValues.VALID_TAX_YEARS -> validTaxYearList.mkString(",")
          )))
        }

        result.header.status shouldBe OK
      }
    }

    s"return an UNAUTHORISED (401)" when {
      "the confidence level is too low" in {
        val result = {
          stubIncomeSources
          stubGetExcludedCall(taxYear, nino)
          unauthorisedIndividualInsufficientConfidenceLevel()
          await(controller.show(taxYear)(fakeRequest.withSession(SessionKeys.authToken -> "mock-bearer-token")))
        }

        result.header.status shouldBe SEE_OTHER
        result.header.headers shouldBe Map("Location" -> "/update-and-submit-income-tax-return/iv-uplift")
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

  "Hitting the final calculation endpoint" when {

    userScenarios.filterNot(_.isWelsh).foreach { user =>

      def authUser(): StubMapping = if (!user.isAgent) authoriseIndividual() else authoriseAgent()

      s"as an ${if (user.isAgent) "agent" else "individual"}" should {
        "return a redirect with the calc id in session when the at the end of year" which {
          lazy val calcId = UUID.randomUUID().toString
          lazy val request = FakeRequest(controllers.routes.OverviewPageController.finalCalculation(taxYearEOY)).withSession(
            SessionKeys.authToken -> "mock-bearer-token",
            SessionValues.CLIENT_MTDITID -> "1234567890",
            SessionValues.CLIENT_NINO -> "AA123456A",
            SessionValues.TAX_YEAR -> s"$taxYearEOY",
            SessionValues.VALID_TAX_YEARS -> validTaxYearList.mkString(",")
          ).withHeaders("X-Session-ID" -> sessionId)

          lazy val result: Future[Result] = {
            authUser()
            stubLiabilityCalculationEOY(Some(LiabilityCalculationIdModel(calcId)))
            route(customApp(), request).get
          }

          "has a status of SEE_OTHER" in {
            status(result) shouldBe SEE_OTHER
          }

          s"has a redirect to the view and change ${if (user.isAgent) "agent" else "individual"} page" in {
            val expectedUrl = if (user.isAgent) {
              s"http://localhost:9081/report-quarterly/income-and-expenses/view/agents/$taxYearEOY/final-tax-overview/calculate$vcPtaNavBarOrigin"
            } else {
              s"http://localhost:9081/report-quarterly/income-and-expenses/view/$taxYearEOY/final-tax-overview/calculate$vcPtaNavBarOrigin"
            }

            await(result).header.headers("Location") shouldBe expectedUrl
          }
        }

        "redirect back to the overview page when trying access in year" which {
          lazy val calcId = UUID.randomUUID().toString
          lazy val request = FakeRequest(controllers.routes.OverviewPageController.finalCalculation(taxYear)).withSession(
            SessionKeys.authToken -> "mock-bearer-token",
            SessionValues.CLIENT_MTDITID -> "1234567890",
            SessionValues.CLIENT_NINO -> "AA123456A",
            SessionValues.TAX_YEAR -> s"$taxYear",
            SessionValues.VALID_TAX_YEARS -> validTaxYearList.mkString(",")
          ).withHeaders("X-Session-ID" -> sessionId)

          lazy val result: Future[Result] = {
            authUser()
            stubLiabilityCalculation(Some(LiabilityCalculationIdModel(calcId)))
            route(customApp(), request).get
          }

          "has a status of SEE_OTHER" in {
            status(result) shouldBe SEE_OTHER
          }

          s"has a redirect location back to the overview page" in {
            val expectedUrl = s"http://localhost:9302/update-and-submit-income-tax-return/$taxYear/view"

            await(result).header.headers("Location") shouldBe expectedUrl
          }
        }
      }
    }

    "there is an error with the liability calculation" should {
      "redirect to an error page" which {
        lazy val request = FakeRequest(controllers.routes.OverviewPageController.finalCalculation(taxYearEOY)).withSession(
          SessionKeys.authToken -> "mock-bearer-token",
          SessionValues.CLIENT_MTDITID -> "1234567890",
          SessionValues.CLIENT_NINO -> "AA123456A",
          SessionValues.TAX_YEAR -> s"$taxYearEOY",
          SessionValues.VALID_TAX_YEARS -> validTaxYearList.mkString(",")
        ).withHeaders("X-Session-ID" -> sessionId)

        lazy val result: Future[Result] = {
          authoriseIndividual()
          stubLiabilityCalculationEOY(None, SERVICE_UNAVAILABLE)
          route(customApp(
            dividendsEnabled = false,
            interestEnabled = false,
            giftAidEnabled = false,
            gainsEnabled = false,
            employmentEnabled = false,
            stockDividendsEnabled = false,
            studentLoansEnabled = false,
            employmentEOYEnabled = false,
            cisEnabled = false,
            pensionsEnabled = false,
            stateBenefitsEnabled = false,
            crystallisationEnabled = false
          ), request).get
        }

        "has the status SERVICE_AVAILABLE (503)" in {
          status(result) shouldBe SERVICE_UNAVAILABLE
        }

        "is a webpage" in {
          await(result).body.contentType shouldBe Some("text/html; charset=utf-8")
        }
      }
    }
  }

  "Hitting the inYear estimate endpoint" when {
    userScenarios.filterNot(_.isWelsh).foreach { user =>

      def authUser(): StubMapping = if (!user.isAgent) authoriseIndividual() else authoriseAgent()

      s"as an ${if (user.isAgent) "agent" else "individual"}" should {
        "return a redirect when in year" which {
          lazy val request = FakeRequest(controllers.routes.OverviewPageController.inYearEstimate(taxYear)).withSession(
            SessionKeys.authToken -> "mock-bearer-token",
            SessionValues.CLIENT_MTDITID -> "1234567890",
            SessionValues.CLIENT_NINO -> "AA123456A",
            SessionValues.TAX_YEAR -> s"$taxYear",
            SessionValues.VALID_TAX_YEARS -> validTaxYearList.mkString(",")
          ).withHeaders("X-Session-ID" -> sessionId)

          lazy val result: Future[Result] = {
            authUser()
            route(customApp(
              dividendsEnabled = false,
              interestEnabled = false,
              giftAidEnabled = false,
              employmentEnabled = false,
              gainsEnabled = false,
              stockDividendsEnabled = false,
              studentLoansEnabled = false,
              employmentEOYEnabled = false,
              cisEnabled = false,
              pensionsEnabled = false,
              stateBenefitsEnabled = false,
              crystallisationEnabled = false
            ), request).get
          }

          "has a status of SEE_OTHER" in {
            status(result) shouldBe SEE_OTHER
          }

          s"has a redirect to the view and change ${if (user.isAgent) "agent" else "individual"} page" in {
            val expectedUrl = if (user.isAgent) {
              "http://localhost:9081/report-quarterly/income-and-expenses/view/agents/tax-overview"
            }
            else {
              s"http://localhost:9081/report-quarterly/income-and-expenses/view/tax-overview$vcPtaNavBarOrigin"
            }

            await(result).header.headers("Location") shouldBe expectedUrl
          }
        }

        "redirect back to the in year overview page when trying to access it at the end of year" which {
          lazy val request = FakeRequest(controllers.routes.OverviewPageController.inYearEstimate(taxYearEOY)).withSession(
            SessionKeys.authToken -> "mock-bearer-token",
            SessionValues.CLIENT_MTDITID -> "1234567890",
            SessionValues.CLIENT_NINO -> "AA123456A",
            SessionValues.TAX_YEAR -> s"$taxYearEOY",
            SessionValues.VALID_TAX_YEARS -> validTaxYearList.mkString(",")
          ).withHeaders("X-Session-ID" -> sessionId)

          lazy val result: Future[Result] = {
            authUser()
            route(customApp(
              dividendsEnabled = false,
              interestEnabled = false,
              giftAidEnabled = false,
              gainsEnabled = false,
              employmentEnabled = false,
              stockDividendsEnabled = false,
              studentLoansEnabled = false,
              employmentEOYEnabled = false,
              cisEnabled = false,
              pensionsEnabled = false,
              stateBenefitsEnabled = false,
              crystallisationEnabled = false
            ), request).get
          }

          "has a status of SEE_OTHER" in {
            status(result) shouldBe SEE_OTHER
          }

          s"has a redirect to the end of year overview page" in {
            val expectedUrl = s"/update-and-submit-income-tax-return/$taxYearEOY/income-tax-return-overview"

            await(result).header.headers("Location") shouldBe expectedUrl
          }
        }
      }
    }
  }
}
