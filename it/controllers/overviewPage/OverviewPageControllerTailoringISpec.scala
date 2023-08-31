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

import itUtils.{IntegrationTest, OverviewPageHelpers, ViewHelpers}
import models.IncomeSourcesModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.http.HeaderNames
import play.api.http.Status.OK
import play.api.mvc.Result
import play.api.test.Helpers.{status, writeableOf_AnyContentAsEmpty}
import play.api.test.{FakeRequest, Helpers}

import scala.concurrent.Future

class OverviewPageControllerTailoringISpec extends IntegrationTest with ViewHelpers with OverviewPageHelpers {

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
    val notificationBannerPlural = "We have added 5 sections to your return, based on the information we already hold about you."
    val EmptyTailoringLiText: String ="No sections have been added to your Income Tax Return"
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
    val notificationBannerPlural = "We have added 5 sections to your client’s return, based on the information we already hold about them."
    val EmptyTailoringLiText: String ="No sections have been added to your client’s Income Tax Return"
  }

  object ExpectedIndividualCY extends SpecificExpectedResults {
    val headingExpected = "Eich Ffurflen Dreth Incwm"
    val updateIncomeTaxReturnText = "Diweddarwch eich Ffurflen Dreth Incwm"
    val submitReturnHeaderEOY = "Diweddaru a chyflwyno’ch Ffurflen Dreth Incwm"
    val submitReturnText: String = "Os ydych wedi gorffen diweddaru eich Ffurflen Dreth Incwm, gallwch barhau a gweld eich cyfrifiad treth terfynol. " +
      "Gallwch wirio eich cyfrifiad ac yna fe allwch gyflwyno eich Ffurflen Dreth Incwm."
    val ifWeHaveInfo: String = "Os oes gennym wybodaeth am eich incwm a didyniadau, byddwn yn ei chofnodi ar eich rhan. " +
      "Rydym yn cael yr wybodaeth hon o’n cofnodion a’ch pecyn meddalwedd - os oes gennych un."
    val goToYourIncomeTax = "Ewch i’ch Cyfrif Treth Incwm i wybod mwy am eich sefyllfa dreth bresennol."

    def inYearInsertText(taxYear: Int): String = s"Ni allwch gyflwyno’ch Ffurflen Dreth Incwm tan 6 Ebrill $taxYear."

    val addSections = "Ychwanegu adrannau at eich Ffurflen Dreth Incwm"
    val notificationBanner = "Rydym wedi ychwanegu adran at eich Ffurflen Dreth, yn seiliedig ar yr wybodaeth sydd eisoes gennym amdanoch."
    val notificationBannerPlural = "Rydym wedi ychwanegu 5 adran at eich Ffurflen Dreth, yn seiliedig ar yr wybodaeth sydd eisoes gennym amdanoch."
    val EmptyTailoringLiText: String ="Nid oes unrhyw adrannau wedi’u hychwanegu at eich Ffurflen Dreth Incwm"
  }

  object ExpectedAgentCY extends SpecificExpectedResults {
    val headingExpected = "Ffurflen Dreth Incwm eich cleient"
    val updateIncomeTaxReturnText = "Diweddarwch Ffurflen Dreth Incwm eich cleient"
    val submitReturnHeaderEOY = "Gwiriwch a chyflwynwch Ffurflen Dreth Incwm eich cleient"
    val submitReturnText: String = "Os ydych wedi gorffen diweddaru Ffurflen Dreth Incwm eich cleient, " +
      "gallwch barhau a gweld eu cyfrifiad treth terfynol. Gwiriwch y cyfrifiad a chyflwyno’r Ffurflen Dreth Incwm."
    val ifWeHaveInfo: String = "Os oes gennym wybodaeth am incwm a didyniadau eich cleient, byddwn yn ei chofnodi ar eich rhan. " +
      "Rydym yn cael yr wybodaeth hon o’n cofnodion a’ch pecyn meddalwedd - os oes gennych un."
    val goToYourIncomeTax = "Ewch i’r canlynol ar ran eich cleient Cyfrif Treth Incwm i wybod mwy am ei sefyllfa dreth bresennol."

    def inYearInsertText(taxYear: Int): String = s"Ni allwch gyflwyno’ch Ffurflen Dreth Incwm eich cleient tan 6 Ebrill $taxYear."

    val addSections: String = "Ychwanegu adrannau at Ffurflen Dreth Incwm eich cleient"
    val notificationBanner = "Rydym wedi ychwanegu adran at Ffurflen Dreth eich cleient, yn seiliedig ar yr wybodaeth sydd eisoes gennym amdano."
    val notificationBannerPlural = "Rydym wedi ychwanegu 5 adran at Ffurflen Dreth eich cleient, yn seiliedig ar yr wybodaeth sydd eisoes gennym amdano."
    val EmptyTailoringLiText: String ="Nid oes unrhyw adrannau wedi’u hychwanegu at Ffurflen Dreth Incwm eich cleient"
  }

  trait SpecificExpectedResults {
    val headingExpected: String
    val updateIncomeTaxReturnText: String
    val EmptyTailoringLiText: String
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
    val selfEmploymentLinkText: String
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
    val selfEmploymentLinkText = "Self employment"
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
    val selfEmploymentLinkText = "Self employment"
    val continue = "continue"
    val fillInTheSections: String = "Llenwch yr adrannau mae angen i chi eu diweddaru. " +
      "Defnyddiwch eich pecyn meddalwedd i ddiweddaru eitemau sydd ddim ar y rhestr hon."
    val incomeTaxAccountLink = "Cyfrif Treth Incwm"
    val updateTaxCalculation = "Diweddaru cyfrifiad treth"
    val checkSectionsText = "Gwiriwch bob adran – mae’n bosibl y bydd rhaid i chi newid yr wybodaeth rydym wedi’i hychwanegu ar eich rhan."
  }

  object Selectors {
    def sectionNameSelector(index: Int): String = s"#main-content > div > div > ol > li:nth-child($index) > span.app-task-list__task-name"
    val EmptyTailoringLiSelector: String = s"#main-content > div > div > ol > li.govuk-inset-text"

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
    val cisSelector: String = sectionNameSelector(5)
    val cisLinkSelector = "#cis_link"
    val cisStatusSelector: String = statusTagSelector(5)
    val pensionsLinkSelector = "#pensions_link"
    val pensionsStatusSelector: String = statusTagSelector(6)
    val selfEmploymentSelector: String = sectionNameSelector(10)
    val selfEmploymentLinkSelector = "#selfEmployment_link"
    val selfEmploymentStatusSelector: String = statusTagSelector(10)
    val viewEstimateSelector = "#calculation_link"
    val submitReturnEOYSelector = "#heading-checkAndSubmit"
    val submitReturnTextEOYSelector = "#p-submitText"
    val youWillBeAbleSelector = "#main-content > div > div > ol > li:nth-child(4) > ul > li.govuk-body"
    val ifWeHaveInformationSelector = "#main-content > div > div > p:nth-child(3)"
    val fillInTheSectionsSelector = "#main-content > div > div > p:nth-child(4)"
    val goToYourIncomeTaxReturnSelector = "#p-gotoAccountDetails"
    val updateTaxCalculationSelector = "#updateTaxCalculation"
    val formSelector = "#main-content > div > div > form"
    val checkSectionsSelector = "#p-checkSections"
    val addSectionsSelector = "#addSectionsLink"
    val notificationBannerSelector = "#main-content > div > div > div.govuk-notification-banner > div.govuk-notification-banner__content > p"
  }

  case class JourneyData(tailoringKey: String, expectedText: String, incomeSourcesModel: IncomeSourcesModel)

  def journeys(user: UserScenario[CommonExpectedResults, SpecificExpectedResults]):Seq[JourneyData] = Seq(
    JourneyData("interest", user.commonExpectedResults.interestsLinkText, IncomeSourcesModel(interest = incomeSourcesModel.interest)),
    JourneyData("dividends", user.commonExpectedResults.dividendsLinkText, IncomeSourcesModel(dividends = incomeSourcesModel.dividends)),
    JourneyData("gift-aid", user.commonExpectedResults.giftAidLinkText, IncomeSourcesModel(giftAid = incomeSourcesModel.giftAid)),
    JourneyData("cis", user.commonExpectedResults.cisLinkText, IncomeSourcesModel(cis = incomeSourcesModel.cis)),
    JourneyData("employment", user.commonExpectedResults.employmentSLLinkText, IncomeSourcesModel(employment = incomeSourcesModel.employment))
  )

  val userScenarios: Seq[UserScenario[CommonExpectedResults, SpecificExpectedResults]] = Seq(
    UserScenario(isWelsh = false, isAgent = false, CommonExpectedEN, Some(ExpectedIndividualEN)),
    UserScenario(isWelsh = false, isAgent = true, CommonExpectedEN, Some(ExpectedAgentEN)),
    UserScenario(isWelsh = true, isAgent = false, CommonExpectedCY, Some(ExpectedIndividualCY)),
    UserScenario(isWelsh = true, isAgent = true, CommonExpectedCY, Some(ExpectedAgentCY))
  )

  private val urlPathInYear = s"/update-and-submit-income-tax-return/$taxYear/view"

  userScenarios.foreach { user =>
    import Selectors._
    import user.commonExpectedResults._

    val specific = user.specificExpectedResults.get

    s"language is ${welshTest(user.isWelsh)} and request is from an ${agentTest(user.isAgent)}" should {
      val headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear, validTaxYearList), "Csrf-Token" -> "nocheck")

      "only display the relevant row for tailoring" when {
        journeys(user).foreach { journey =>
          s"the journey is for '${journey.tailoringKey}'" which {
            val request = FakeRequest("GET", urlPathInYear).withHeaders(headers: _*)

            lazy val result: Future[Result] = {
              cleanDatabase(taxYear)
              insertJourneys(false, journey.tailoringKey)
              stubGetExcludedCall(taxYear, nino)
              authoriseAgentOrIndividual(user.isAgent)
              route(customApp(tailoringEnabled = true), request, user.isWelsh).get
            }

            implicit val document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

            "has a status of OK(200)" in {
              status(result) shouldBe OK
            }

            textOnPageCheck(checkSectionsText, checkSectionsSelector)

            textOnPageCheck(journey.expectedText, sectionNameSelector(1))
            "have a add sections link " which {
              linkCheck(specific.addSections, addSectionsSelector, Links.addSectionsLink(taxYear))
            }
          }
        }
      }
      "only display the " when {
            val request = FakeRequest("GET", urlPathInYear).withHeaders(headers: _*)

            lazy val result: Future[Result] = {
              cleanDatabase(taxYear)
              stubGetExcludedCall(taxYear, nino)
              authoriseAgentOrIndividual(user.isAgent)
              route(customApp(tailoringEnabled = true), request, user.isWelsh).get
            }

            implicit val document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

            "has a status of OK(200)" in {
              status(result) shouldBe OK
            }

            textOnPageCheck(checkSectionsText, checkSectionsSelector)

            textOnPageCheck(specific.EmptyTailoringLiText, EmptyTailoringLiSelector)
            "have a add sections link " which {
              linkCheck(specific.addSections, addSectionsSelector, Links.addSectionsLink(taxYear))
            }
      }
      "only display the relevant row with priorData and display singular notificationBannner" when {
        journeys(user).foreach { journey =>
          s"the journey is for '${journey.tailoringKey}' priorData Only" which {
            val request = FakeRequest("GET", urlPathInYear).withHeaders(headers: _*)

            lazy val result: Future[Result] = {
              cleanDatabase(taxYear)
              stubIncomeSources(journey.incomeSourcesModel)
              stubGetExcludedCall(taxYear, nino)
              authoriseAgentOrIndividual(user.isAgent)
              route(customApp(tailoringEnabled = true), request, user.isWelsh).get
            }

            implicit val document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

            "has a status of OK(200)" in {
              status(result) shouldBe OK
            }

            textOnPageCheck(specific.notificationBanner, notificationBannerSelector)
            textOnPageCheck(journey.expectedText, sectionNameSelector(1))
            "have a add sections link " which {
              linkCheck(specific.addSections, addSectionsSelector, Links.addSectionsLink(taxYear))
            }
          }
        }
      }
    }
  }

}
