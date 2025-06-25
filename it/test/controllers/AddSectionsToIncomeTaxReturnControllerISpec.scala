/*
 * Copyright 2024 HM Revenue & Customs
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
import config.AppConfig
import forms.AddSectionsForm
import helpers.PlaySessionCookieBaker
import itUtils.{IntegrationTest, ViewHelpers}
import models.IncomeSourcesModel
import models.mongo.{DatabaseError, TailoringUserDataModel}
import org.apache.pekko.Done
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.http.HeaderNames
import play.api.http.Status.{INTERNAL_SERVER_ERROR, SEE_OTHER}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.test.Helpers.{OK, status, writeableOf_AnyContentAsEmpty}
import play.api.test.{FakeRequest, Helpers}
import play.api.{Application, Environment, Mode}
import repositories.TailoringUserDataRepository
import uk.gov.hmrc.http.SessionKeys

class AddSectionsToIncomeTaxReturnControllerISpec extends IntegrationTest with ViewHelpers {

  trait CommonExpectedResults {
    def caption(taxYearMinusOne: Int, taxYear: Int): String

    val selectAllTheSections: String
    val cis: String
    val dividends: String
    val dividendsHint: String
    val employment: String
    val employmentHint: String
    val giftAid: String
    val giftAidHint: String
    val interest: String
    val interestHint: String
    val gains: String
    val gainsHint: String
    val pensions: String
    val pensionsHint: String
    val stateBenefits: String
    val stateBenefitsHint: String
    val selfEmployment: String
    val saveAndContinue: String
  }

  trait SpecificExpectedResults {
    val heading: String
    val youCanAddSections: String
    val useYourSoftwarePackage: String
    val noMoreIncomeSourcesInset: String
  }

  object ExpectedIndividualEN extends SpecificExpectedResults {
    val heading: String = "Add sections to your Income Tax Return"
    val youCanAddSections: String = "You can add or remove these sections any time before you submit your Income Tax Return."
    val useYourSoftwarePackage: String = "Use your software package to update sections that are not on your return" +
      " or this list, for example income from property."
    val noMoreIncomeSourcesInset: String = "There are no more sections that can be added to your Income Tax Return."
  }

  object ExpectedAgentEN extends SpecificExpectedResults {
    val heading: String = "Add sections to your client’s Income Tax Return"
    val youCanAddSections: String = "You can add or remove these sections any time before you submit your client’s Income Tax Return."
    val useYourSoftwarePackage: String = "Use your software package to update sections that are not on your client’s" +
      " return or this list, for example income from property."
    val noMoreIncomeSourcesInset: String = "There are no more sections that can be added to your client’s Income Tax Return."
  }

  object ExpectedIndividualCY extends SpecificExpectedResults {
    val heading: String = "Ychwanegu adrannau at eich Ffurflen Dreth Incwm"
    val youCanAddSections: String = "Gallwch ychwanegu neu dynnu’r adrannau hyn unrhyw bryd cyn i chi gyflwyno’ch Ffurflen Dreth Incwm."
    val useYourSoftwarePackage: String = "Defnyddiwch eich pecyn meddalwedd i ddiweddaru adrannau nad ydynt ar eich Ffurflen Dreth nac" +
      " ar y rhestr hon, er enghraifft, incwm oeiddo."
    val noMoreIncomeSourcesInset: String = "Nid oes rhagor o adrannau y gellir eu hychwanegu at eich Ffurflen Treth Incwm."
  }

  object ExpectedAgentCY extends SpecificExpectedResults {
    val heading: String = "Ychwanegu adrannau at Ffurflen Dreth Incwm eich cleient"
    val youCanAddSections: String = "Gallwch ychwanegu neu dynnu’r adrannau hyn unrhyw bryd cyn i chi gyflwyno Ffurflen Dreth Incwm eich cleient."
    val useYourSoftwarePackage: String = "Defnyddiwch eich pecyn meddalwedd i ddiweddaru adrannau nad ydynt ar Ffurflen Dreth eich cleient" +
      " nac ar y rhestr hon, er enghraifft, incwm o eiddo."
    val noMoreIncomeSourcesInset: String = "Nid oes rhagor o adrannau y gellir eu hychwanegu at Ffurflen Dreth Incwm eich cleient."
  }

  object CommonExpectedEN extends CommonExpectedResults {
    def caption(taxYearMinusOne: Int, taxYear: Int): String = s"6 April $taxYearMinusOne to 5 April $taxYear"

    val selectAllTheSections: String = "Select all the sections you want to add."
    val cis: String = "Construction Industry Scheme deductions"
    val dividends: String = "Dividends"
    val dividendsHint: String = "from UK trusts, unit trusts and open-ended investment companies"
    val employment: String = "PAYE employment (including student loans)"
    val employmentHint: String = "including employment benefits, expenses and student loan repayments"
    val gains: String = "Gains from life insurance policies and contracts"
    val gainsHint: String = "including life insurance policies, life annuity contracts and capital redemption policies"
    val giftAid: String = "Donations to charity"
    val giftAidHint: String = "including donations to overseas charities and donations of land, property, shares and securities"
    val interest: String = "Interest"
    val interestHint: String = "including taxed and untaxed UK interest from banks, building societies, trust funds and life annuity payments"
    val pensions: String = "Pensions"
    val pensionsHint: String = "including income from pensions, payments into UK and overseas pensions and allowances"
    val stateBenefits: String = "State benefits"
    val stateBenefitsHint: String = "including Employment and Support Allowance and Jobseeker’s Allowance"
    val selfEmployment: String = "Self employment"
    val saveAndContinue: String = "Save and continue"
  }

  object CommonExpectedCY extends CommonExpectedResults {
    def caption(taxYearMinusOne: Int, taxYear: Int): String = s"6 Ebrill $taxYearMinusOne i 5 Ebrill $taxYear"

    val selectAllTheSections: String = "Dewiswch yr holl adrannau rydych am eu hychwanegu."
    val cis: String = "Didyniadau Cynllun y Diwydiant Adeiladu"
    val dividends: String = "Difidendau"
    val dividendsHint: String = "gan ymddiriedolaethau yn y DU, ymddiriedolaethau unedol a chwmnïau buddsoddi penagored"
    val employment: String = "Cyflogaeth TWE"
    val employmentHint: String = "gan gynnwys buddiannau cyflogaeth, treuliau ac ad-daliadau benthyciad myfyriwr"
    val gains: String = "Enillion o bolisïau yswiriant bywyd a chontractau"
    val gainsHint: String = "gan gynnwys polisïau yswiriant bywyd, contractau blwydd-daliadau bywyd a pholisïau adbrynu cyfalaf"
    val giftAid: String = "Rhoddion i elusennau"
    val giftAidHint: String = "gan gynnwys rhoddion i elusennau tramor a rhoddion o dir, eiddo, cyfranddaliadau a gwarannau"
    val interest: String = "Llog"
    val interestHint: String = "gan gynnwys llog y DU wedi’i drethu a heb ei drethu a geir gan faciau, cymdeithasau adeiladu a" +
      " thrwy gronfeydd ymddiriedolaeth a thaliadau blwydd-dal bywyd"
    val pensions: String = "Pensiynau"
    val pensionsHint: String = "gan gynnwys incwm o bensiynau, taliadau i bensiynau a lwfansau’r DU a thramor"
    val stateBenefits: String = "Budd-daliadau’r Wladwriaeth"
    val stateBenefitsHint: String = "gan gynnwys Lwfans Cyflogaeth a Chymorth, a Lwfans Ceisio Gwaith"
    val selfEmployment: String = "Self employment"
    val saveAndContinue: String = "Cadw ac yn eich blaen"
  }

  object Selectors {
    val youCanAddLegendSelector: String = "#main-content > div > div > form > fieldset > legend > p:nth-child(1)"
    val selectAllHintTextSelector: String = "#main-content > div > div > form > fieldset > div.govuk-hint"
    val cisSelector: String = "#cis"
    val dividendsSelector: String = "#dividends"
    val dividendsHintSelector: String = "#dividends-item-hint"
    val giftAidSelector: String = "#gift-aid"
    val giftAidHintSelector: String = "#gift-aid-item-hint"
    val gainsSelector: String = "#gains"
    val gainsHintSelector: String = "#gains-item-hint"
    val interestSelector: String = "#interest"
    val interestHintSelector: String = "#interest-item-hint"
    val employmentSelector: String = "#employment"
    val employmentHintSelector: String = "#employment-item-hint"
    val pensionsSelector: String = "#pensions"
    val pensionsHintSelector: String = "#pensions-item-hint"
    val stateBenefitsSelector: String = "#state-benefits"
    val stateBenefitsHintSelector: String = "#state-benefits-item-hint"
    val selfEmploymentSelector: String = "#self-employment"
    val useYourSoftwareParagraphSelector: String = "#main-content > div > div > form > fieldset > legend > p:nth-child(2)"
    val useYourSoftwareParagraphNoSourcesSelector: String = "#main-content > div > div > form > div.govuk-body > p"
    val noMoreIncomeSourcesInsetSelector: String = "#main-content > div > div > form > div.govuk-inset-text > p"
    val saveAndContinueSelector = "#continue"
  }

  val addSectionsInputFieldName: String = "addSections[]"

  private val urlPath = s"/update-and-submit-income-tax-return/$taxYear/add-sections"

  lazy val frontendAppConfig: AppConfig = app.injector.instanceOf[AppConfig]

  val invalidEncryptionApp: Application = customApp(invalidEncryptionKey = true)

  private lazy val tailoringRepository: TailoringUserDataRepository = app.injector.instanceOf[TailoringUserDataRepository]

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .in(Environment.simple(mode = Mode.Dev))
    .configure(config(tailoringEnabled = true))
    .build()


  private def cleanDatabase(taxYear: Int): Seq[String] = {
    await(tailoringRepository.clear(taxYear))
    await(tailoringRepository.ensureIndexes())
  }

  private def insertJourneys(endOfYear: Boolean, journeys: String*): Either[DatabaseError, Done] =
    await(tailoringRepository.create(TailoringUserDataModel(
      "AA123456A",
      if (endOfYear) taxYearEOY else taxYear,
      journeys
    )))

  private def insertAllJourneys(endOfYear: Boolean = false): Either[DatabaseError, Done] =
    insertJourneys(
      endOfYear,
      "dividends",
      "interest",
      "gift-aid",
      "employment",
      "gains",
      "cis",
      "pensions",
      "property",
      "state-benefits",
      "self-employment",
      "stock-dividends"
    )

  val userScenarios: Seq[UserScenario[CommonExpectedResults, SpecificExpectedResults]] = Seq(
    UserScenario(isWelsh = false, isAgent = false, CommonExpectedEN, Some(ExpectedIndividualEN)),
    UserScenario(isWelsh = false, isAgent = true, CommonExpectedEN, Some(ExpectedAgentEN)),
    UserScenario(isWelsh = true, isAgent = false, CommonExpectedCY, Some(ExpectedIndividualCY)),
    UserScenario(isWelsh = true, isAgent = true, CommonExpectedCY, Some(ExpectedAgentCY))
  )

  def stubIncomeSources(incomeSources: IncomeSourcesModel): StubMapping = {
    stubGet(s"/income-tax-submission-service/income-tax/nino/AA123456A/sources\\?taxYear=$taxYear", OK, Json.toJson(incomeSources).toString())
  }

  def stubIncomeInvalidSources: StubMapping = {
    stubGet(s"/income-tax-submission-service/income-tax/nino/AA123456A/sources\\?taxYear=$taxYear", OK, Json.toJson("incomeSources").toString())
  }

  ".show" should {
    userScenarios.foreach { scenarioData =>
      import scenarioData.commonExpectedResults._

      val specific = scenarioData.specificExpectedResults.get

      s"render the page for isAgent: ${scenarioData.isAgent} and isWelsh: ${scenarioData.isWelsh} content" when {
        "there are no journeys which have previously updated and no journeys in the tailoring database" which {
          val headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear, validTaxYearList), "Csrf-Token" -> "nocheck")

          val request = FakeRequest("GET", urlPath).withHeaders(headers: _*)

          lazy val result = {
            cleanDatabase(taxYear)
            authoriseAgentOrIndividual(scenarioData.isAgent)
            route(app, request, scenarioData.isWelsh).get
          }

          implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

          "returns a status of OK(200)" in {
            status(result) shouldBe OK
          }

          titleCheck(specific.heading, scenarioData.isWelsh)
          h1Check(specific.heading + " " + caption(taxYearEOY, taxYear))
          captionCheck(caption(taxYearEOY, taxYear))
          textOnPageCheck(specific.youCanAddSections, Selectors.youCanAddLegendSelector)
          textOnPageCheck(selectAllTheSections, Selectors.selectAllHintTextSelector)
          inputFieldValueCheck(addSectionsInputFieldName, Selectors.cisSelector, "cis")
          inputFieldValueCheck(addSectionsInputFieldName, Selectors.dividendsSelector, "dividends")
          textOnPageCheck(dividendsHint, Selectors.dividendsHintSelector)
          inputFieldValueCheck(addSectionsInputFieldName, Selectors.gainsSelector, "gains")
          textOnPageCheck(gainsHint, Selectors.gainsHintSelector)
          inputFieldValueCheck(addSectionsInputFieldName, Selectors.giftAidSelector, "gift-aid")
          textOnPageCheck(giftAidHint, Selectors.giftAidHintSelector)
          inputFieldValueCheck(addSectionsInputFieldName, Selectors.interestSelector, "interest")
          textOnPageCheck(interestHint, Selectors.interestHintSelector)
          inputFieldValueCheck(addSectionsInputFieldName, Selectors.employmentSelector, "employment")
          textOnPageCheck(employmentHint, Selectors.employmentHintSelector)
          inputFieldValueCheck(addSectionsInputFieldName, Selectors.pensionsSelector, "pensions")
          textOnPageCheck(pensionsHint, Selectors.pensionsHintSelector)
          inputFieldValueCheck(addSectionsInputFieldName, Selectors.stateBenefitsSelector, "state-benefits")
          textOnPageCheck(stateBenefitsHint, Selectors.stateBenefitsHintSelector)
          inputFieldValueCheck(addSectionsInputFieldName, Selectors.selfEmploymentSelector, "self-employment")
          textOnPageCheck(specific.useYourSoftwarePackage, Selectors.useYourSoftwareParagraphSelector)
          buttonCheck(saveAndContinue, Selectors.saveAndContinueSelector, None)
        }

        "there are no journeys which have been previously updated but all journeys are present in tailoring database" which {
          val headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear, validTaxYearList), "Csrf-Token" -> "nocheck")

          val request = FakeRequest("GET", urlPath).withHeaders(headers: _*)

          lazy val result = {
            cleanDatabase(taxYear)
            insertAllJourneys()
            authoriseAgentOrIndividual(scenarioData.isAgent)
            route(app, request, scenarioData.isWelsh).get
          }

          implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

          "returns a status of OK(200)" in {
            status(result) shouldBe OK
          }

          titleCheck(specific.heading, scenarioData.isWelsh)
          h1Check(specific.heading + " " + caption(taxYearEOY, taxYear))
          captionCheck(caption(taxYearEOY, taxYear))
          textOnPageCheck(specific.noMoreIncomeSourcesInset, Selectors.noMoreIncomeSourcesInsetSelector)
          textOnPageCheck(specific.useYourSoftwarePackage, Selectors.useYourSoftwareParagraphNoSourcesSelector)
          buttonCheck(saveAndContinue, Selectors.saveAndContinueSelector, None)
        }

        "the user has data which has previously been entered for the dividends, interest and employment income sources" which {
          val incomeSources = incomeSourcesModel.copy(
            giftAid = None,
            cis = None,
            stateBenefits = None
          )

          val headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear, validTaxYearList), "Csrf-Token" -> "nocheck")

          val request = FakeRequest("GET", urlPath).withHeaders(headers: _*)

          lazy val result = {
            cleanDatabase(taxYear)
            stubIncomeSources(incomeSources)
            authoriseAgentOrIndividual(scenarioData.isAgent)
            route(app, request, scenarioData.isWelsh).get
          }

          implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

          "returns a status of OK(200)" in {
            status(result) shouldBe OK
          }

          titleCheck(specific.heading, scenarioData.isWelsh)
          h1Check(specific.heading + " " + caption(taxYearEOY, taxYear))
          captionCheck(caption(taxYearEOY, taxYear))
          textOnPageCheck(specific.youCanAddSections, Selectors.youCanAddLegendSelector)
          textOnPageCheck(selectAllTheSections, Selectors.selectAllHintTextSelector)
          inputFieldValueCheck(addSectionsInputFieldName, Selectors.cisSelector, "cis")
          inputFieldValueCheck(addSectionsInputFieldName, Selectors.giftAidSelector, "gift-aid")
          textOnPageCheck(giftAidHint, Selectors.giftAidHintSelector)
          textOnPageCheck(specific.useYourSoftwarePackage, Selectors.useYourSoftwareParagraphSelector)
          buttonCheck(saveAndContinue, Selectors.saveAndContinueSelector, None)
        }

        "the user has prior data for all the income sources available" which {
          val incomeSources = incomeSourcesModel
          val headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear, validTaxYearList), "Csrf-Token" -> "nocheck")
          val request = FakeRequest("GET", urlPath).withHeaders(headers: _*)

          lazy val result = {
            cleanDatabase(taxYear)
            stubIncomeSources(incomeSources)
            authoriseAgentOrIndividual(scenarioData.isAgent)
            route(app, request, scenarioData.isWelsh).get
          }

          implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

          "returns a status of OK(200)" in {
            status(result) shouldBe OK
          }

          titleCheck(specific.heading, scenarioData.isWelsh)
          h1Check(specific.heading + " " + caption(taxYearEOY, taxYear))
          captionCheck(caption(taxYearEOY, taxYear))
          textOnPageCheck(specific.noMoreIncomeSourcesInset, Selectors.noMoreIncomeSourcesInsetSelector)
          textOnPageCheck(specific.useYourSoftwarePackage, Selectors.useYourSoftwareParagraphNoSourcesSelector)
          buttonCheck(saveAndContinue, Selectors.saveAndContinueSelector, None)
        }
      }
    }

    "return a INTERNAL SERVER ERROR " when {
      "getting incomeSources fails" which {

        val headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear, validTaxYearList), "Csrf-Token" -> "nocheck")

        val request = FakeRequest("GET", urlPath).withHeaders(headers: _*)

        lazy val result = {
          cleanDatabase(taxYear)
          stubIncomeInvalidSources
          authoriseAgentOrIndividual(false)
          route(app, request, false).get
        }

        "returns a status of INTERNAL_SERVER_ERROR(500)" in {
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }
      }
    }

    "redirect to Overview page" when {
      "tailoring is turned off" which {

        val headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear, validTaxYearList), "Csrf-Token" -> "nocheck")

        val request = FakeRequest("GET", urlPath).withHeaders(headers: _*)

        lazy val result = {
          cleanDatabase(taxYear)
          stubIncomeInvalidSources
          authoriseAgentOrIndividual(false)
          route(customApp(tailoringEnabled = false), request, false).get
        }

        "returns a status of SEE_OTHER(303)" in {
          status(result) shouldBe SEE_OTHER
        }

        "redirect url location is pointing to the overview page" in {
          redirectUrl(result) shouldBe controllers.routes.OverviewPageController.show(taxYear).url
        }
      }
    }
  }

  ".submit" should {
    "redirect to the overview page when submitting the form successfully" when {
      lazy val form = Map(s"${AddSectionsForm.addSections}[]" -> Seq("dividends"))

      lazy val playSessionCookies = PlaySessionCookieBaker.bakeSessionCookie(Map(
        SessionKeys.authToken -> "mock-bearer-token",
        SessionValues.TAX_YEAR -> taxYear.toString,
        SessionValues.VALID_TAX_YEARS -> validTaxYearList.mkString(",")
      ))

      val headers = Seq(HeaderNames.COOKIE -> playSessionCookies, "Csrf-Token" -> "nocheck")

      lazy val result: WSResponse = {
        authoriseIndividual()
        stubPost(urlPath, OK, "{}")

        await(wsClient
          .url(s"http://localhost:$port" + urlPath)
          .withHttpHeaders(headers: _*)
          .withFollowRedirects(false)
          .post(form))
      }

      "returns a SEE_OTHER (303) result" in {
        result.status shouldBe SEE_OTHER
      }

      "redirect url location is pointing to the overview page" in {
        result.header(HeaderNames.LOCATION) shouldBe Some(controllers.routes.OverviewPageController.show(taxYear).url)
      }
    }

    "redirect to the overview page when submitting the form successfully with prior Tailoring Data" when {

      lazy val form = Map(s"${AddSectionsForm.addSections}[]" -> Seq("dividends"))

      lazy val playSessionCookies = PlaySessionCookieBaker.bakeSessionCookie(Map(
        SessionKeys.authToken -> "mock-bearer-token",
        SessionValues.TAX_YEAR -> taxYear.toString,
        SessionValues.VALID_TAX_YEARS -> validTaxYearList.mkString(",")
      ))

      val headers = Seq(HeaderNames.COOKIE -> playSessionCookies, "Csrf-Token" -> "nocheck")

      lazy val result: WSResponse = {
        authoriseIndividual()
        insertAllJourneys()
        stubPost(urlPath, OK, "{}")

        await(wsClient
          .url(s"http://localhost:$port" + urlPath)
          .withHttpHeaders(headers: _*)
          .withFollowRedirects(false)
          .post(form))
      }

      "returns a SEE_OTHER (303) result" in {
        result.status shouldBe SEE_OTHER
      }

      "redirect url location is pointing to the overview page" in {
        result.header(HeaderNames.LOCATION) shouldBe Some(controllers.routes.OverviewPageController.show(taxYear).url)
      }
    }

    "redirect to the overview page when submitting the form successfully with prior Tailoring Data and incomeSources" when {

      lazy val form = Map(s"${AddSectionsForm.addSections}[]" -> Seq("dividends"))

      lazy val playSessionCookies = PlaySessionCookieBaker.bakeSessionCookie(Map(
        SessionKeys.authToken -> "mock-bearer-token",
        SessionValues.TAX_YEAR -> taxYear.toString,
        SessionValues.VALID_TAX_YEARS -> validTaxYearList.mkString(",")
      ))

      val headers = Seq(HeaderNames.COOKIE -> playSessionCookies, "Csrf-Token" -> "nocheck")

      lazy val result: WSResponse = {
        authoriseIndividual()
        stubIncomeSources(incomeSourcesModel)
        insertAllJourneys()
        stubPost(urlPath, OK, "{}")

        await(wsClient
          .url(s"http://localhost:$port" + urlPath)
          .withHttpHeaders(headers: _*)
          .withFollowRedirects(false)
          .post(form))
      }

      "returns a SEE_OTHER (303) result" in {
        result.status shouldBe SEE_OTHER
      }

      "redirect url location is pointing to the overview page" in {
        result.header(HeaderNames.LOCATION) shouldBe Some(controllers.routes.OverviewPageController.show(taxYear).url)
      }
    }
    "redirect to the same page when the form fails" when {

      lazy val form = Map("wrongvalue" -> "",
        "anotherWrongValue" -> "")

      lazy val playSessionCookies = PlaySessionCookieBaker.bakeSessionCookie(Map(
        SessionValues.TAX_YEAR -> taxYear.toString,
        SessionValues.VALID_TAX_YEARS -> validTaxYearList.mkString(",")
      ))

      val headers = Seq(HeaderNames.COOKIE -> playSessionCookies, "Csrf-Token" -> "nocheck")

      lazy val result: WSResponse = {
        authoriseIndividual()
        stubIncomeSources(incomeSourcesModel)
        insertAllJourneys()
        stubPost(urlPath, OK, "{}")

        await(wsClient
          .url(s"http://localhost:$port" + urlPath)
          .withHttpHeaders(headers: _*)
          .withFollowRedirects(false)
          .post(form))
      }

      "returns a SEE_OTHER (303) result" in {
        result.status shouldBe SEE_OTHER
      }

    }
    "redirect to the overview page when tailoring is switched off" when {

      lazy val playSessionCookies = PlaySessionCookieBaker.bakeSessionCookie(Map(
        SessionKeys.authToken -> "mock-bearer-token",
        SessionValues.TAX_YEAR -> taxYear.toString,
        SessionValues.VALID_TAX_YEARS -> validTaxYearList.mkString(",")
      ))

      def wsClientNoTailoring: WSClient = customApp(tailoringEnabled = false).injector.instanceOf[WSClient]

      val headers = Seq(HeaderNames.COOKIE -> playSessionCookies, "Csrf-Token" -> "nocheck")

      lazy val result: WSResponse = {
        authoriseIndividual()
        stubPost(urlPath, OK, "{}")

        await(wsClientNoTailoring
          .url(s"http://localhost:$port" + urlPath)
          .withHttpHeaders(headers: _*)
          .withFollowRedirects(false)
          .post(Map("" -> "")))
      }

      "returns a SEE_OTHER (303) result" in {
        result.status shouldBe SEE_OTHER
      }

      "redirect url location is pointing to the overview page" in {
        result.header(HeaderNames.LOCATION) shouldBe Some(controllers.routes.OverviewPageController.show(taxYear).url)
      }

    }
    "Return a Internal Server Error when incomeSources call fails" when {

      lazy val form = Map(s"${AddSectionsForm.addSections}[]" -> Seq("dividends"))

      lazy val playSessionCookies = PlaySessionCookieBaker.bakeSessionCookie(Map(
        SessionKeys.authToken -> "mock-bearer-token",
        SessionValues.TAX_YEAR -> taxYear.toString,
        SessionValues.VALID_TAX_YEARS -> validTaxYearList.mkString(",")
      ))

      val headers = Seq(HeaderNames.COOKIE -> playSessionCookies, "Csrf-Token" -> "nocheck")

      lazy val result: WSResponse = {
        authoriseIndividual()
        stubIncomeInvalidSources
        insertAllJourneys()
        stubPost(urlPath, OK, "{}")

        await(wsClient
          .url(s"http://localhost:$port" + urlPath)
          .withHttpHeaders(headers: _*)
          .withFollowRedirects(false)
          .post(form))
      }

      "returns a INTERNAL_SERVER_ERROR (500) result" in {
        result.status shouldBe INTERNAL_SERVER_ERROR
      }
    }
  }
}