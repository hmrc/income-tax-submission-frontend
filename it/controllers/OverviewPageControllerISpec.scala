/*
 * Copyright 2022 HM Revenue & Customs
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

import audit.AuditService
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import common.IncomeSources._
import common.SessionValues
import config.{AppConfig, ErrorHandler}
import controllers.predicates.AuthorisedAction
import itUtils.{IntegrationTest, TailoringDatabaseHelper, ViewHelpers}
import models.{IncomeSourcesModel, InterestModel, LiabilityCalculationIdModel}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.http.HeaderNames
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.Helpers.{OK, status, writeableOf_AnyContentAsEmpty}
import play.api.test.{FakeRequest, Helpers}
import services.{IncomeSourcesService, LiabilityCalculationService, TailoringSessionService}
import uk.gov.hmrc.http.SessionKeys
import views.html.OverviewPageView

import java.util.UUID
import scala.concurrent.Future

class OverviewPageControllerISpec extends IntegrationTest with ViewHelpers with TailoringDatabaseHelper {

  val tailoringComplete = Seq(INTEREST, DIVIDENDS, GIFT_AID, EMPLOYMENT, CIS)

  object Links {
    def startPageBreadcrumbUrl(taxYear: Int = taxYear): String = s"/update-and-submit-income-tax-return/$taxYear/start"

    def dividendsLink(taxYear: Int = taxYear): String =
      s"http://localhost:9308/update-and-submit-income-tax-return/personal-income/$taxYear/dividends/dividends-from-uk-companies"

    def dividendsLinkWithPriorData(taxYear: Int = taxYear): String =
      s"http://localhost:9308/update-and-submit-income-tax-return/personal-income/$taxYear/dividends/check-income-from-dividends"

    def interestsLink(taxYear: Int = taxYear): String =
      s"http://localhost:9308/update-and-submit-income-tax-return/personal-income/$taxYear/interest/untaxed-uk-interest"

    def interestsLinkWithPriorData(taxYear: Int = taxYear): String =
      s"http://localhost:9308/update-and-submit-income-tax-return/personal-income/$taxYear/interest/check-interest"

    def employmentLink(taxYear: Int = taxYear): String = s"http://localhost:9317/update-and-submit-income-tax-return/employment-income/$taxYear/employment-summary"

    def cisLink(taxYear: Int = taxYear): String = s"http://localhost:9338/update-and-submit-income-tax-return/construction-industry-scheme-deductions/$taxYear/summary"

    def viewEstimateLink(taxYear: Int = taxYear): String = s"/update-and-submit-income-tax-return/$taxYear/calculate"

    def viewAndChangeLinkInYear(isAgent: Boolean): String = if (isAgent) {
      "http://localhost:9081/report-quarterly/income-and-expenses/view/agents/tax-overview"
    }
    else {
      "http://localhost:9081/report-quarterly/income-and-expenses/view/tax-overview"
    }

    def viewAndChangeLink(isAgent: Boolean): String = if (isAgent) {
      "http://localhost:9081/report-quarterly/income-and-expenses/view/agents/income-tax-account"
    }
    else {
      "http://localhost:9081/report-quarterly/income-and-expenses/view"
    }

    val endOfYearContinueLink = s"/update-and-submit-income-tax-return/$taxYearEOY/final-calculation"

  }

  object ExpectedIndividualEN extends SpecificExpectedResults {
    val headingExpected = "Your Income Tax Return"
    val updateIncomeTaxReturnText = "Update your Income Tax Return"
    val submitReturnHeaderEOY = "Check and submit your Income Tax Return"
    val submitReturnText: String = "If you’ve finished updating your Income Tax Return, you can continue and see your final tax calculation. " +
      "You can check your calculation and then submit your Income Tax Return."
    val goToYourIncomeTax = "Go to your Income Tax Account to find out more about your current tax position."
    val addSections = "Add sections to your Income Tax Return"
    val notificationBanner = "We have added a section to your return, based on the information we already hold about you."
    val notificationBannerPlural = "We have added 5 sections to your return, based on the information we already hold about you."

  }

  object ExpectedAgentEN extends SpecificExpectedResults {
    val headingExpected = "Your client’s Income Tax Return"
    val updateIncomeTaxReturnText = "Update your client’s Income Tax Return"
    val submitReturnHeaderEOY = "Check and submit your client’s Income Tax Return"
    val submitReturnText: String = "If you’ve finished updating your client’s Income Tax Return, you can continue and see their final tax calculation." +
      " You can check their calculation and then submit their Income Tax Return."
    val goToYourIncomeTax = "Go to your client’s Income Tax Account to find out more about their current tax position."
    val addSections = "Add sections to your client’s Income Tax Return"
    val notificationBanner =  "We have added a section to your client’s return, based on the information we already hold about them."
    val notificationBannerPlural = "We have added 5 sections to your client’s return, based on the information we already hold about them."

  }

  object ExpectedIndividualCY extends SpecificExpectedResults {
    val headingExpected = "Eich Ffurflen Dreth Incwm"
    val updateIncomeTaxReturnText = "Diweddarwch eich Ffurflen Dreth Incwm"
    val submitReturnHeaderEOY = "Diweddaru a chyflwyno’ch Ffurflen Dreth Incwm"
    val submitReturnText: String = "Os ydych wedi gorffen diweddaru eich Ffurflen Dreth Incwm, gallwch barhau a gweld eich cyfrifiad treth terfynol. Gallwch wirio eich cyfrifiad ac yna fe allwch gyflwyno eich Ffurflen Dreth Incwm."
    val goToYourIncomeTax = "Ewch i’ch Cyfrif Treth Incwm i wybod mwy am eich sefyllfa dreth bresennol."
    val addSections = "Ychwanegu adrannau at eich Ffurflen Dreth Incwm"
    val notificationBanner = "Rydym wedi ychwanegu adran at eich Ffurflen Dreth, yn seiliedig ar yr wybodaeth sydd eisoes gennym amdanoch."
    val notificationBannerPlural = "Rydym wedi ychwanegu 5 adran at eich Ffurflen Dreth, yn seiliedig ar yr wybodaeth sydd eisoes gennym amdanoch."
  }

  object ExpectedAgentCY extends SpecificExpectedResults {
    val headingExpected = "Ffurflen Dreth Incwm eich cleient"
    val updateIncomeTaxReturnText = "Diweddarwch Ffurflen Dreth Incwm eich cleient"
    val submitReturnHeaderEOY = "Gwiriwch a chyflwynwch Ffurflen Dreth Incwm eich cleient"
    val submitReturnText: String = "Os ydych wedi gorffen diweddaru Ffurflen Dreth Incwm eich cleient, gallwch barhau a gweld eu cyfrifiad treth terfynol. Gwiriwch y cyfrifiad a chyflwyno’r Ffurflen Dreth Incwm."
    val goToYourIncomeTax = "Ewch i’r canlynol ar ran eich cleient Cyfrif Treth Incwm i wybod mwy am ei sefyllfa dreth bresennol."
    val addSections: String = "Ychwanegu adrannau at Ffurflen Dreth Incwm eich cleient"
    val notificationBanner = "Rydym wedi ychwanegu adran at Ffurflen Dreth eich cleient, yn seiliedig ar yr wybodaeth sydd eisoes gennym amdano."
    val notificationBannerPlural = "Rydym wedi ychwanegu 5 adran at Ffurflen Dreth eich cleient, yn seiliedig ar yr wybodaeth sydd eisoes gennym amdano."

  }

  trait SpecificExpectedResults {
    val headingExpected: String
    val updateIncomeTaxReturnText: String
    val submitReturnHeaderEOY: String
    val submitReturnText: String
    val goToYourIncomeTax: String
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
    val employmentLinkText: String
    val cisLinkText: String
    val giftAidLinkText: String
    val continue: String
    val incomeTaxAccountLink: String
    val updateTaxCalculation: String
    val checkSectionsText: String
  }

  object CommonExpectedEN extends CommonExpectedResults {
    val vcBreadcrumb = "Income Tax"
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
    val employmentLinkText = "PAYE employment (including student loans)"
    val cisLinkText = "Construction Industry Scheme deductions"
    val giftAidLinkText = "Donations to charity"
    val continue = "continue"
    val incomeTaxAccountLink = "Income Tax Account"
    val updateTaxCalculation = "Update tax calculation"
    val checkSectionsText = "Check every section, you may need to change the information that we have added for you."
  }

  object CommonExpectedCY extends CommonExpectedResults {
    val vcBreadcrumb = "Treth Incwm"
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
    val employmentLinkText = "Cyflogaeth TWE gan gynnwys Benthyciadau Myfyriwr"
    val cisLinkText = "Didyniadau Cynllun y Diwydiant Adeiladu"
    val giftAidLinkText = "Rhoddion i elusennau"
    val continue = "continue"
    val incomeTaxAccountLink = "Cyfrif Treth Incwm"
    val updateTaxCalculation = "Diweddaru cyfrifiad treth"
    val checkSectionsText = "Gwiriwch bob adran – mae’n bosibl y bydd rhaid i chi newid yr wybodaeth rydym wedi’i hychwanegu ar eich rhan."
  }

  object Selectors {
    private def sectionNameSelector(index: Int): String = s"#main-content > div > div > ol > li:nth-child($index) > span.app-task-list__task-name"
    def statusTagSelector(index: Int): String = s"#main-content > div > div > ol > li:nth-child($index) > span.hmrc-status-tag"

    val vcBreadcrumbSelector = "body > div > div.govuk-breadcrumbs > ol > li:nth-child(1) > a"
    val startPageBreadcrumbSelector = "body > div > div.govuk-breadcrumbs > ol > li:nth-child(2) > a"
    val overviewBreadcrumbSelector = "body > div > div.govuk-breadcrumbs > ol > li:nth-child(3)"
    val captionSelector = "#main-content > div > div > header > p"
    val headerSelector = "#main-content > div > div > header > h1"
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
    val cisSelector: String = sectionNameSelector(5)
    val cisLinkSelector = "#cis_link"
    val cisStatusSelector: String = statusTagSelector(5)
    val viewEstimateSelector = "#calculation_link"
    val submitReturnEOYSelector = "#heading-checkAndSubmit"
    val submitReturnTextEOYSelector = "#p-submitText"
    val checkSectionsSelector = "#p-checkSections"
    val addSectionsSelector = "#addSectionsLink"
    val goToYourIncomeTaxReturnSelector = "#p-gotoAccountDetails"
    val updateTaxCalculationSelector = "#updateTaxCalculation"
    val formSelector = "#main-content > div > div > form"
    val notificationBannerSelector = "#main-content > div > div > div.govuk-notification-banner > div.govuk-notification-banner__content > p"
  }

  val urlPathInYear = s"/update-and-submit-income-tax-return/$taxYear/view"
  val urlPathEndOfYear = s"/update-and-submit-income-tax-return/$taxYearEOY/income-tax-return-overview"

  lazy val frontendAppConfig: AppConfig = app.injector.instanceOf[AppConfig]

  val controller: OverviewPageController = new OverviewPageController(
    inYearAction,
    app.injector.instanceOf[IncomeSourcesService],
    app.injector.instanceOf[LiabilityCalculationService],
    app.injector.instanceOf[TailoringSessionService],
    app.injector.instanceOf[OverviewPageView],
    app.injector.instanceOf[AuthorisedAction],
    app.injector.instanceOf[ErrorHandler],
    app.injector.instanceOf[AuditService]
  )(frontendAppConfig, mcc, scala.concurrent.ExecutionContext.Implicits.global)

  def stubIncomeSources(incomeSources: IncomeSourcesModel): StubMapping = {
    stubGet(s"/income-tax-submission-service/income-tax/nino/AA123456A/sources\\?taxYear=$taxYear", OK, Json.toJson(incomeSources).toString())
  }

  def stubLiabilityCalculation(response: Option[LiabilityCalculationIdModel], returnStatus: Int = OK): StubMapping = {
    stubGet(s"/income-tax-calculation/income-tax/nino/AA123456A/taxYear/$taxYear/tax-calculation\\?crystallise=true", returnStatus, Json.toJson(response).toString())
  }

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
        val headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck")

        "render an overview page with all sections showing status tag 'under maintenance' when feature switch is false" when {
          val request = FakeRequest("GET", urlPathInYear).withHeaders(headers: _*)
          lazy val result: Future[Result] = {
            dropTailoringDB()
            insertData(tailoringComplete, taxYear)
            authoriseAgentOrIndividual(user.isAgent)
            route(appWithSourcesTurnedOff, request, user.isWelsh).get
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
          h1Check(specific.headingExpected, "xl")
          textOnPageCheck(caption(taxYearEOY, taxYear), captionSelector)
          textOnPageCheck(specific.updateIncomeTaxReturnText, updateYourIncomeTaxReturnSubheadingSelector)
          textOnPageCheck(checkSectionsText, checkSectionsSelector)

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
          "have a cis section that says under maintenance" which {
            textOnPageCheck(underMaintenance, cisStatusSelector)
          }
          "have a add sections link " which {
            linkCheck(specific.addSections, addSectionsSelector, "")
          }

          textOnPageCheck(specific.goToYourIncomeTax, goToYourIncomeTaxReturnSelector)
          linkCheck(incomeTaxAccountLink, viewEstimateSelector, Links.viewAndChangeLink(user.isAgent))
        }

        "render overview page with 'Not Started' status tags when there is no prior data, the employment section with" +
          "the status tag 'cannot update' user in the current taxYear and all feature switches are turned on" when {
          val request = FakeRequest("GET", urlPathInYear).withHeaders(headers: _*)

          lazy val result: Future[Result] = {
            dropTailoringDB()
            insertData(tailoringComplete, taxYear)
            authoriseAgentOrIndividual(user.isAgent)
            stubIncomeSources(incomeSourcesModel.copy(None, None, None, None, None))
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
          h1Check(specific.headingExpected, "xl")
          textOnPageCheck(caption(taxYearEOY, taxYear), captionSelector)
          textOnPageCheck(specific.updateIncomeTaxReturnText, updateYourIncomeTaxReturnSubheadingSelector)
          textOnPageCheck(checkSectionsText, checkSectionsSelector)

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

          "has a cis section " which {
            textOnPageCheck(cisLinkText, cisSelector)
            textOnPageCheck(cannotUpdateText, cisStatusSelector)
          }

          "has a donations to charity section" which {
            linkCheck(giftAidLinkText, giftAidLinkSelector, appConfig.personalIncomeTaxGiftAidUrl(taxYear))
            textOnPageCheck(notStartedText, giftAidStatusSelector)
          }

          "have a add sections link " which {
            linkCheck(specific.addSections, addSectionsSelector, "")
          }

          formPostLinkCheck(controllers.routes.OverviewPageController.inYearEstimate(taxYear).url, formSelector)
          buttonCheck(updateTaxCalculation, updateTaxCalculationSelector, None)

        }

        "render overview page with status tag 'Not Started' for interest when interest income source is None " when {
          val request = FakeRequest("GET", urlPathInYear).withHeaders(headers: _*)

          lazy val result: Future[Result] = {
            dropTailoringDB()
            insertData(tailoringComplete, taxYear)
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
          h1Check(specific.headingExpected, "xl")
          textOnPageCheck(caption(taxYearEOY, taxYear), captionSelector)
          textOnPageCheck(specific.updateIncomeTaxReturnText, updateYourIncomeTaxReturnSubheadingSelector)
          textOnPageCheck(checkSectionsText, checkSectionsSelector)

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

          "has a cis section" which {
            linkCheck(cisLinkText, cisLinkSelector, cisLink(taxYear))
            textOnPageCheck(updatedText, cisStatusSelector)
          }

          "have a add sections link " which {
            linkCheck(specific.addSections, addSectionsSelector, "")
          }

          formPostLinkCheck(controllers.routes.OverviewPageController.inYearEstimate(taxYear).url, formSelector)
          buttonCheck(updateTaxCalculation, updateTaxCalculationSelector, None)
        }

        "render overview page with correct status tags when there is prior data and user is in the current taxYear" should {
          "have the status as 'Not Started' for interest when interest income source is None" when {
            val incomeSources = incomeSourcesModel.copy(interest = None)

            val request = FakeRequest("GET", urlPathInYear).withHeaders(headers: _*)

            lazy val result: Future[Result] = {
              dropTailoringDB()
              insertData(tailoringComplete, taxYear)
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
            h1Check(specific.headingExpected, "xl")
            textOnPageCheck(caption(taxYearEOY, taxYear), captionSelector)
            textOnPageCheck(specific.updateIncomeTaxReturnText, updateYourIncomeTaxReturnSubheadingSelector)
            textOnPageCheck(checkSectionsText, checkSectionsSelector)

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

            "has a cis section" which {
              linkCheck(cisLinkText, cisLinkSelector, cisLink(taxYear))
              textOnPageCheck(updatedText, cisStatusSelector)
            }

            "have a add sections link " which {
              linkCheck(specific.addSections, addSectionsSelector, "")
            }

            formPostLinkCheck(controllers.routes.OverviewPageController.inYearEstimate(taxYear).url, formSelector)
            buttonCheck(updateTaxCalculation, updateTaxCalculationSelector, None)

          }
        }


        "interest income source is defined and the untaxed and taxed accounts do not have amounts" which {
          val incomeSources = incomeSourcesModel.copy(interest = Some(Seq(InterestModel("TestName", "TestSource", None, None))))

          val request = FakeRequest("GET", urlPathInYear).withHeaders(headers: _*)

          lazy val result: Future[Result] = {
            dropTailoringDB()
            insertData(tailoringComplete, taxYear)
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
          h1Check(specific.headingExpected, "xl")
          textOnPageCheck(caption(taxYearEOY, taxYear), captionSelector)
          textOnPageCheck(specific.updateIncomeTaxReturnText, updateYourIncomeTaxReturnSubheadingSelector)
          textOnPageCheck(checkSectionsText, checkSectionsSelector)

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

          "has a cis section" which {
            linkCheck(cisLinkText, cisLinkSelector, cisLink(taxYear))
            textOnPageCheck(updatedText, cisStatusSelector)
          }

          "have a add sections link " which {
            linkCheck(specific.addSections, addSectionsSelector, "")
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
              dropTailoringDB()
              insertData(tailoringComplete, taxYear)
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
            h1Check(specific.headingExpected, "xl")
            textOnPageCheck(caption(taxYearEOY, taxYear), captionSelector)
            textOnPageCheck(specific.updateIncomeTaxReturnText, updateYourIncomeTaxReturnSubheadingSelector)
            textOnPageCheck(checkSectionsText, checkSectionsSelector)

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

            "has a cis section" which {
              linkCheck(cisLinkText, cisLinkSelector, cisLink(taxYear))
              textOnPageCheck(updatedText, cisStatusSelector)
            }

            "have a add sections link " which {
              linkCheck(specific.addSections, addSectionsSelector, "")
            }

            formPostLinkCheck(controllers.routes.OverviewPageController.inYearEstimate(taxYear).url, formSelector)
            buttonCheck(updateTaxCalculation, updateTaxCalculationSelector, None)

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
        val headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYearEOY), "Csrf-Token" -> "nocheck")

        "render an overview page with all sections turned off" when {
          val request = FakeRequest("GET", urlPathEndOfYear).withHeaders(headers: _*)

          lazy val result: Future[Result] = {
            dropTailoringDB()
            insertData(tailoringComplete, taxYearEOY)
            authoriseAgentOrIndividual(user.isAgent)
            route(appWithSourcesTurnedOffEndOfYear, request, user.isWelsh).get
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
          h1Check(specific.headingExpected, "xl")
          textOnPageCheck(caption(taxYearEndOfYearMinusOne, taxYearEOY), captionSelector)
          textOnPageCheck(specific.updateIncomeTaxReturnText, updateYourIncomeTaxReturnSubheadingSelector)
          textOnPageCheck(checkSectionsText, checkSectionsSelector)

          "have a dividends section that says under maintenance" which {
            textOnPageCheck(underMaintenance, dividendsStatusSelector)
          }

          "have an interest section that says under maintenance" which {
            textOnPageCheck(underMaintenance, interestStatusSelector)
          }

          "have an employment section that says under maintenance" which {
            textOnPageCheck(underMaintenance, employmentStatusSelector)
          }

          "have a cis section that says under maintenance" which {
            textOnPageCheck(underMaintenance, cisStatusSelector)
          }

          "has a donations to charity section" which {
            textOnPageCheck(underMaintenance, giftAidStatusSelector)
          }

          "have a add sections link " which {
            linkCheck(specific.addSections, addSectionsSelector, "")
          }

          textOnPageCheck(specific.goToYourIncomeTax, goToYourIncomeTaxReturnSelector)
          linkCheck(incomeTaxAccountLink, viewEstimateSelector, Links.viewAndChangeLink(user.isAgent))

          textOnPageCheck(specific.submitReturnHeaderEOY, submitReturnEOYSelector)
          textOnPageCheck(specific.submitReturnText, submitReturnTextEOYSelector)
          formPostLinkCheck(endOfYearContinueLink, formSelector)
        }

        "render an overview page with prior data" when {
          val request = FakeRequest("GET", urlPathEndOfYear).withHeaders(headers: _*)

          lazy val result: Future[Result] = {
            dropTailoringDB()
            insertData(tailoringComplete, taxYearEOY)
            authoriseAgentOrIndividual(user.isAgent)
            stubIncomeSourcesEndOfYear
            route(appWithSourcesTurnedOnEndOfYear, request, user.isWelsh).get
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
          h1Check(specific.headingExpected, "xl")
          textOnPageCheck(caption(taxYearEndOfYearMinusOne, taxYearEOY), captionSelector)
          textOnPageCheck(specific.updateIncomeTaxReturnText, updateYourIncomeTaxReturnSubheadingSelector)
          textOnPageCheck(checkSectionsText, checkSectionsSelector)


          "has a dividends section" which {
            linkCheck(dividendsLinkText, dividendsLinkSelector, dividendsLinkWithPriorData(taxYearEOY))
            textOnPageCheck(updatedText, dividendsStatusSelector)
          }

          "has an interest section" which {
            linkCheck(interestsLinkText, interestLinkSelector, interestsLinkWithPriorData(taxYearEOY))
            textOnPageCheck(updatedText, interestStatusSelector)
          }

          "has an employment section" which {
            linkCheck(employmentLinkText, employmentLinkSelector, employmentLink(taxYearEOY))
            textOnPageCheck(updatedText, employmentStatusSelector)
          }

          "has a cis section" which {
            linkCheck(cisLinkText, cisLinkSelector, cisLink(taxYearEOY))
            textOnPageCheck(updatedText, cisStatusSelector)
          }

          "has a donations to charity section" which {
            linkCheck(giftAidLinkText, giftAidLinkSelector, appConfig.personalIncomeTaxGiftAidSubmissionCYAUrl(taxYearEOY))
            textOnPageCheck(updatedText, giftAidStatusSelector)
          }

          "have a add sections link " which {
            linkCheck(specific.addSections, addSectionsSelector, "")
          }

          textOnPageCheck(specific.submitReturnHeaderEOY, submitReturnEOYSelector)
          textOnPageCheck(specific.submitReturnText, submitReturnTextEOYSelector)
          formPostLinkCheck(endOfYearContinueLink, formSelector)
        }

        "render overview page with 'Started' status tags when there is prior data and the employment section is clickable with" +
          "the status tag 'To do' when user is in a previous year" when {
          val previousYearHeaders = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYearEOY), "Csrf-Token" -> "nocheck")
          val previousYearUrl = s"/update-and-submit-income-tax-return/$taxYearEOY/income-tax-return-overview"
          val taxYearMinusTwo = taxYearEOY - 1
          val request = FakeRequest("GET", previousYearUrl).withHeaders(previousYearHeaders: _*)

          lazy val result: Future[Result] = {
            dropTailoringDB()
            insertData(tailoringComplete, taxYearEOY)
            authoriseAgentOrIndividual(user.isAgent)
            stubIncomeSources(incomeSourcesModel)
            route(appWithTaxYearErrorOff, request, user.isWelsh).get
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
          h1Check(specific.headingExpected, "xl")
          textOnPageCheck(caption(taxYearMinusTwo, taxYearEOY), captionSelector)
          textOnPageCheck(specific.updateIncomeTaxReturnText, updateYourIncomeTaxReturnSubheadingSelector)
          textOnPageCheck(checkSectionsText, checkSectionsSelector)

          "has a dividends section" which {
            linkCheck(dividendsLinkText, dividendsLinkSelector, dividendsLink(taxYearEOY))
            textOnPageCheck(notStartedText, dividendsStatusSelector)
          }

          "has an interest section" which {
            linkCheck(interestsLinkText, interestLinkSelector, interestsLink(taxYearEOY))
            textOnPageCheck(notStartedText, interestStatusSelector)
          }

          "has an employment section " which {
            linkCheck(employmentLinkText, employmentLinkSelector, employmentLink(taxYearEOY))
            textOnPageCheck(todoText, employmentStatusSelector)
          }

          "has a cis section " which {
            linkCheck(cisLinkText, cisLinkSelector, cisLink(taxYearEOY))
            textOnPageCheck(notStartedText, cisStatusSelector)
          }

          "has a donations to charity section" which {
            linkCheck(giftAidLinkText, giftAidLinkSelector, appConfig.personalIncomeTaxGiftAidUrl(taxYearEOY))
            textOnPageCheck(notStartedText, giftAidStatusSelector)
          }

          "have a add sections link " which {
            linkCheck(specific.addSections, addSectionsSelector, "")
          }

          textOnPageCheck(specific.submitReturnHeaderEOY, submitReturnEOYSelector)
          textOnPageCheck(specific.submitReturnText, submitReturnTextEOYSelector)
          formPostLinkCheck(endOfYearContinueLink, formSelector)
        }

        "render the overview page EOY with 'Started' status tags for each section and 'Cannot Update' for the employment section" +
          "when employmentEOYEnabled feature switch is false" when {
          val previousYearHeaders = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYearEOY), "Csrf-Token" -> "nocheck")
          val previousYearUrl = s"/update-and-submit-income-tax-return/$taxYearEOY/income-tax-return-overview"
          val taxYearMinusTwo = taxYearEOY - 1
          val request = FakeRequest("GET", previousYearUrl).withHeaders(previousYearHeaders: _*)

          lazy val result: Future[Result] = {
            authoriseAgentOrIndividual(user.isAgent)
            stubIncomeSources(incomeSourcesModel)
            route(appWithEmploymentEOYEnabledOff, request, user.isWelsh).get
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
          h1Check(specific.headingExpected, "xl")
          textOnPageCheck(caption(taxYearMinusTwo, taxYearEOY), captionSelector)
          textOnPageCheck(specific.updateIncomeTaxReturnText, updateYourIncomeTaxReturnSubheadingSelector)

          "has a dividends section" which {
            linkCheck(dividendsLinkText, dividendsLinkSelector, dividendsLink(taxYearEOY))
            textOnPageCheck(notStartedText, dividendsStatusSelector)
          }

          "has an interest section" which {
            linkCheck(interestsLinkText, interestLinkSelector, interestsLink(taxYearEOY))
            textOnPageCheck(notStartedText, interestStatusSelector)
          }

          "has an employment section " which {
            textOnPageCheck(employmentLinkText, employmentSelector)
            textOnPageCheck(cannotUpdateText, employmentStatusSelector)
          }

          "has a cis section " which {
            linkCheck(cisLinkText, cisLinkSelector, cisLink(taxYearEOY))
            textOnPageCheck(notStartedText, cisStatusSelector)
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
          await(controller.show(taxYear)(fakeRequest.withSession(SessionValues.TAX_YEAR -> s"$taxYear")))
        }

        result.header.status shouldBe OK
      }

    }

    "return a SEE_OTHER (303) when end of year" when {

      "all auth requirements are met" in {

        val result = {
          authoriseIndividual()
          sourcesTurnedOffConfigEndOfYear
          await(controller.show(taxYearEOY)(fakeRequest.withSession(SessionValues.TAX_YEAR -> s"$taxYearEOY", SessionKeys.sessionId -> "sessionId-0101010101")))
        }

        result.header.status shouldBe SEE_OTHER
      }
    }

    s"return an OK (200) and no prior data and a session id" when {

      "all auth requirements are met" in {
        val result = {
          authoriseIndividual()
          await(controller.show(taxYear)(fakeRequest.withSession(SessionValues.TAX_YEAR -> s"$taxYear", SessionKeys.sessionId -> "sessionId-0101010101")))
        }

        result.header.status shouldBe OK
      }
    }

    s"return an OK (200) with prior data" when {

      "all auth requirements are met" in {
        val result = {
          stubIncomeSources
          authoriseIndividual()
          await(controller.show(taxYear)(fakeRequest.withSession(SessionValues.TAX_YEAR -> s"$taxYear")))
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

        "return a redirect with the calc id in session" which {
          lazy val calcId = UUID.randomUUID().toString
          lazy val request = FakeRequest(controllers.routes.OverviewPageController.finalCalculation(taxYear)).withSession(
            SessionValues.CLIENT_MTDITID -> "1234567890",
            SessionValues.CLIENT_NINO -> "AA123456A",
            SessionValues.TAX_YEAR -> s"$taxYear"
          ).withHeaders("X-Session-ID" -> sessionId)

          lazy val result: Future[Result] = {
            authUser()
            stubLiabilityCalculation(Some(LiabilityCalculationIdModel(calcId)))
            route(appWithSourcesTurnedOff, request).get
          }

          "has a status of SEE_OTHER" in {
            status(result) shouldBe SEE_OTHER
          }

          s"has a redirect to the view and change ${if (user.isAgent) "agent" else "individual"} page" in {
            val expectedUrl = if (user.isAgent) s"http://localhost:9081/report-quarterly/income-and-expenses/view/agents/$taxYear/final-tax-overview/calculate"
            else s"http://localhost:9081/report-quarterly/income-and-expenses/view/$taxYear/final-tax-overview/calculate"

            await(result).header.headers("Location") shouldBe expectedUrl
          }
        }
      }
    }

    "there is an error with the liability calculation" should {

      "redirect to an error page" which {

        lazy val request = FakeRequest(controllers.routes.OverviewPageController.finalCalculation(taxYear)).withSession(
          SessionValues.CLIENT_MTDITID -> "1234567890",
          SessionValues.CLIENT_NINO -> "AA123456A",
          SessionValues.TAX_YEAR -> s"$taxYear"
        ).withHeaders("X-Session-ID" -> sessionId)

        lazy val result: Future[Result] = {
          authoriseIndividual()
          stubLiabilityCalculation(None, SERVICE_UNAVAILABLE)
          route(appWithSourcesTurnedOff, request).get
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

        "return a redirect" which {
          lazy val request = FakeRequest(controllers.routes.OverviewPageController.inYearEstimate(taxYear)).withSession(
            SessionValues.CLIENT_MTDITID -> "1234567890",
            SessionValues.CLIENT_NINO -> "AA123456A",
            SessionValues.TAX_YEAR -> s"$taxYear"
          ).withHeaders("X-Session-ID" -> sessionId)

          lazy val result: Future[Result] = {
            authUser()
            route(appWithSourcesTurnedOff, request).get
          }

          "has a status of SEE_OTHER" in {
            status(result) shouldBe SEE_OTHER
          }

          s"has a redirect to the view and change ${if (user.isAgent) "agent" else "individual"} page" in {
            val expectedUrl = if (user.isAgent) {"http://localhost:9081/report-quarterly/income-and-expenses/view/agents/tax-overview"}
            else {"http://localhost:9081/report-quarterly/income-and-expenses/view/tax-overview"}

            await(result).header.headers("Location") shouldBe expectedUrl
          }
        }
      }
    }

  }

  def stubIncomeSources: StubMapping =
    stubGet(s"/income-tax-submission-service/income-tax/nino/AA123456A/sources\\?taxYear=$taxYear", OK, Json.toJson(incomeSourcesModel).toString)

  def stubIncomeSourcesEndOfYear: StubMapping =
    stubGet(s"/income-tax-submission-service/income-tax/nino/AA123456A/sources\\?taxYear=$taxYearEOY", OK, Json.toJson(incomeSourcesModel).toString)
}
