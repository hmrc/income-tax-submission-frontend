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

import audit.AuditService
import common.SessionValues
import config.{AppConfig, ErrorHandler}
import controllers.predicates.AuthorisedAction
import helpers.PlaySessionCookieBaker
import itUtils.{IntegrationTest, ViewHelpers}
import models.{APIErrorBodyModel, DeclarationModel}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.Helpers.{CONFLICT, NO_CONTENT, OK, SEE_OTHER, UNPROCESSABLE_ENTITY, status, writeableOf_AnyContentAsEmpty}
import play.api.test.{FakeRequest, Helpers}
import services.{DeclareCrystallisationService, NrsService}
import views.html.DeclarationPageView

import scala.concurrent.Future

class DeclarationPageControllerISpec extends IntegrationTest with ViewHelpers {

  def controller: DeclarationPageController = new DeclarationPageController(
    app.injector.instanceOf[DeclareCrystallisationService],
    app.injector.instanceOf[NrsService],
    frontendAppConfig,
    mcc,
    scala.concurrent.ExecutionContext.Implicits.global,
    app.injector.instanceOf[DeclarationPageView],
    app.injector.instanceOf[AuthorisedAction],
    app.injector.instanceOf[ErrorHandler],
    app.injector.instanceOf[AuditService]
  )

  val nino: String = "AA012345A"
  val mtditid: String = "1234567890"

  val crystallisationUrl: String = "/income-tax-calculation/income-tax/nino/AA123456A/taxYear/2022/string/declare-crystallisation"

  trait SpecificExpectedResults {
    val taxYear: Int = 2022
    val taxYearMinusOne: Int = taxYear - 1
  }

  object ExpectedResults extends SpecificExpectedResults {

    val heading: String = "Declaration"
    val subheading: String = "6 April " + taxYearMinusOne + " to " + "5 April " + taxYear

    val agreeButton: String = "I agree - Submit Income Tax Return"

    val headingWelsh: String = "Datganiad"
    val subheadingWelsh: String = "6 Ebrill " + taxYearMinusOne + " i " + "5 Ebrill " + taxYear

    val agreeButtonWelsh: String = "Cytunaf - Cyflwyno Ffurflen Dreth Incwm"

    val addressHasChangedTitle: String = "Your address has changed"

    val returnTaxYearExistsTitle: String = "We already have an Income Tax Return for that tax year"

    val taxYearReturnUpdatedTitle: String = "Your Income Tax Return has been updated"

    val expectedIncomeTaxSubmissionFrontendOverviewUrl: String = s"/income-through-software/return/$taxYear/view"

    val expectedNoValidIncomeSourcesUrl: String = s"/income-through-software/return/$taxYear/no-business-income"

    val expectedTaxReturnPreviouslyUpdatedUrl: String = s"/income-through-software/return/$taxYear/income-tax-return-updated"

    val expectedTaxReturnExistsUrl: String = s"/income-through-software/return/$taxYear/already-have-income-tax-return"

    val expectedAddressChangedUrl: String = s"/income-through-software/return/$taxYear/address-changed"


  }

  object IndividualExpectedResults {

    val individualSummaryData: DeclarationModel = DeclarationModel(
      "Jerry Individual", 9000, "EE54321", 321, 312, 123
    )

    val informationText: String = "The information I have provided is correct and complete to the best of my knowledge and belief." +
      " If I give false information I may have to pay financial penalties and face prosecution."

    val informationTextWelsh: String = "Mae’r wybodaeth a roddwyd gennyf yn gywir ac yn gyflawn hyd eithaf fy ngwybodaeth " +
      "a’m cred. Os byddaf yn rhoi gwybodaeth ffug, efallai y bydd yn rhaid i mi dalu cosbau ariannol ac wynebu erlyniad."
  }

  object AgentExpectedResults extends SpecificExpectedResults {

    val agentSummaryData: DeclarationModel = DeclarationModel(
      "Joan Agent", 940.40, "EW62340", 231, 132, 321
    )

    val agentInformationText: String = "I confirm that my client has reviewed the information provided and confirmed" +
      " that it is correct and complete to the best of their knowledge to establish the liability for the tax year " +
      taxYearMinusOne + " to " + taxYear +
      ". My client understands that they may have to pay financial penalties or face prosecution if they give false information."

    val agentInformationTextWelsh: String = "Cadarnhaf fod fy nghleient wedi adolygu’r wybodaeth a roddwyd ac yn cadarnhau " +
      "ei bod yn gywir ac yn gyflawn hyd eithaf ei allu i sefydlu atebolrwydd am y flwyddyn dreth " + taxYearMinusOne +
      " i " + taxYear + ". Mae fy nghleient yn deall y gallai fod yn rhaid iddo dalu cosbau ariannol neu wynebu erlyniad os yw’n rhoi gwybodaeth anwir."
  }

  object Selectors {
    val headingSelector = "#main-content > div > div > header > h1"
    val subheadingSelector = "#main-content > div > div > header > p"
    val informationTextSelector = "#main-content > div > div > div.govuk-body > p"
    val agreeButtonSelector = "#agree"
  }

  import ExpectedResults._
  import Selectors._

  private val urlPath = s"/income-through-software/return/$taxYear/declaration"

  lazy val frontendAppConfig: AppConfig = app.injector.instanceOf[AppConfig]

  "As an individual, the page should correctly render" when {
    import IndividualExpectedResults._

    "the language is specified as English and" should {
      lazy val playSessionCookies = PlaySessionCookieBaker.bakeSessionCookie(Map(
        SessionValues.SUMMARY_DATA -> individualSummaryData.asJsonString,
        SessionValues.TAX_YEAR -> taxYear.toString
      ))

      val headers = Seq(HeaderNames.COOKIE -> playSessionCookies, "Csrf-Token" -> "nocheck")

      "display the declaration page" which {
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
        textOnPageCheck(heading, headingSelector)
        textOnPageCheck(subheading, subheadingSelector)

        textOnPageCheck(informationText, informationTextSelector)
        buttonCheck(agreeButton, agreeButtonSelector)
      }

    }

    "the language is specified as Welsh and" should {
      lazy val playSessionCookies = PlaySessionCookieBaker.bakeSessionCookie(Map(
        SessionValues.SUMMARY_DATA -> individualSummaryData.asJsonString,
        SessionValues.TAX_YEAR -> taxYear.toString
      ))

      val headers = Seq(HeaderNames.COOKIE -> playSessionCookies, "Csrf-Token" -> "nocheck", HeaderNames.ACCEPT_LANGUAGE -> "cy")

      "display the declaration page in Welsh" which {
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
        textOnPageCheck(headingWelsh, headingSelector)
        textOnPageCheck(subheadingWelsh, subheadingSelector)

        textOnPageCheck(informationTextWelsh, informationTextSelector)
        buttonCheck(agreeButtonWelsh, agreeButtonSelector)
      }
    }
  }
  
  "/submit" should {
    
    "Redirect to the confirmation page" when {
      import IndividualExpectedResults.individualSummaryData

      lazy val playSessionCookies = PlaySessionCookieBaker.bakeSessionCookie(Map(
        SessionValues.SUMMARY_DATA -> individualSummaryData.asJsonString,
        SessionValues.TAX_YEAR -> taxYear.toString,
        SessionValues.CALCULATION_ID -> "string"
      ))

      val headers = Seq(HeaderNames.COOKIE -> playSessionCookies, "Csrf-Token" -> "nocheck")

      "there is summary data and a calculation id in session" which {
        lazy val result = {
          authoriseIndividual()
          stubPost(crystallisationUrl, NO_CONTENT, "{}")

          await(wsClient
            .url(s"http://localhost:$port" + urlPath)
            .withHttpHeaders(headers:_*)
            .withFollowRedirects(false)
            .post("{}"))
        }

        "returns a status of SEE_OTHER(303)" in {
          result.status shouldBe SEE_OTHER
        }

        "has a redirect url pointing at the confirmation page" in {
          result.headers("Location").head shouldBe controllers.routes.TaxReturnReceivedController.show(taxYear).url
        }
      }

    }
    
    "Redirect to the overview page" when {
      import IndividualExpectedResults.individualSummaryData

      "the calc id is missing" which {
        lazy val playSessionCookies = PlaySessionCookieBaker.bakeSessionCookie(Map(
          SessionValues.SUMMARY_DATA -> individualSummaryData.asJsonString,
          SessionValues.TAX_YEAR -> taxYear.toString
        ))
        val headers = Seq(HeaderNames.COOKIE -> playSessionCookies, "Csrf-Token" -> "nocheck")

        lazy val result = {
          authoriseIndividual()
          stubPost(crystallisationUrl, NO_CONTENT, "{}")

          await(wsClient
            .url(s"http://localhost:$port" + urlPath)
            .withHttpHeaders(headers:_*)
            .withFollowRedirects(false)
            .post("{}"))
        }

        "returns a status of SEE_OTHER(303)" in {
          result.status shouldBe SEE_OTHER
        }

        "has a redirect url pointing at the confirmation page" in {
          result.headers("Location").head shouldBe controllers.routes.OverviewPageController.show(taxYear).url
        }
      }

      "the summary data is missing" which {
        lazy val playSessionCookies = PlaySessionCookieBaker.bakeSessionCookie(Map(
          SessionValues.TAX_YEAR -> taxYear.toString,
          SessionValues.CALCULATION_ID -> "string"
        ))
        val headers = Seq(HeaderNames.COOKIE -> playSessionCookies, "Csrf-Token" -> "nocheck")

        lazy val result = {
          authoriseIndividual()
          stubPost(crystallisationUrl, NO_CONTENT, "{}")

          await(wsClient
            .url(s"http://localhost:$port" + urlPath)
            .withHttpHeaders(headers:_*)
            .withFollowRedirects(false)
            .post("{}"))
        }

        "returns a status of SEE_OTHER(303)" in {
          result.status shouldBe SEE_OTHER
        }

        "has a redirect url pointing at the confirmation page" in {
          result.headers("Location").head shouldBe controllers.routes.OverviewPageController.show(taxYear).url
        }
        
      }
    }
  }

  "As an agent, the page should correctly render" when {
    import AgentExpectedResults._

    "the language is specified as English and" should {
      lazy val playSessionCookies = PlaySessionCookieBaker.bakeSessionCookie(Map(
        SessionValues.SUMMARY_DATA -> agentSummaryData.asJsonString,
        SessionValues.TAX_YEAR -> taxYear.toString,
        SessionValues.CLIENT_NINO -> nino,
        SessionValues.CLIENT_MTDITID -> mtditid
      ))

      val headers = Seq(HeaderNames.COOKIE -> playSessionCookies, "Csrf-Token" -> "nocheck")

      "display the agent declaration page" which {
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
        textOnPageCheck(heading, headingSelector)
        textOnPageCheck(subheading, subheadingSelector)

        textOnPageCheck(agentInformationText, informationTextSelector)
        buttonCheck(agreeButton,agreeButtonSelector)
      }

    }

    "the language is specified as Welsh and" should {
      lazy val playSessionCookies = PlaySessionCookieBaker.bakeSessionCookie(Map(
        SessionValues.SUMMARY_DATA -> agentSummaryData.asJsonString,
        SessionValues.TAX_YEAR -> taxYear.toString,
        SessionValues.CLIENT_NINO -> nino,
        SessionValues.CLIENT_MTDITID -> mtditid
      ))

      val headers = Seq(HeaderNames.COOKIE -> playSessionCookies, "Csrf-Token" -> "nocheck", HeaderNames.ACCEPT_LANGUAGE -> "cy")

      "display the agent declaration page in Welsh" which {
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
        titleCheck(headingWelsh, isWelsh = true)
        textOnPageCheck(headingWelsh, headingSelector)
        textOnPageCheck(subheadingWelsh, subheadingSelector)

        textOnPageCheck(agentInformationTextWelsh, informationTextSelector)
        buttonCheck(agreeButtonWelsh,agreeButtonSelector)
      }
    }
  }

  "As an individual" when {

    import IndividualExpectedResults._

    "there is no Summary Data in session which" should {
      lazy val playSessionCookies = PlaySessionCookieBaker.bakeSessionCookie(Map(
        SessionValues.TAX_YEAR -> taxYear.toString
      ))

      val headers = Seq(HeaderNames.COOKIE -> playSessionCookies, "Csrf-Token" -> "nocheck")

      "redirect to the overview page" which {
        val request = FakeRequest("GET", urlPath).withHeaders(headers: _*)

        lazy val result: Future[Result] = {
          authoriseIndividual()
          route(app, request).get
        }

        "returns status of SEE_OTHER(303)" in {
          status(result) shouldBe SEE_OTHER
        }

        "has the url of" in {
          redirectUrl(result) shouldBe expectedIncomeTaxSubmissionFrontendOverviewUrl
        }
      }
    }

    "the user's residency has changed" should {
      lazy val playSessionCookies = PlaySessionCookieBaker.bakeSessionCookie(Map(
        SessionValues.SUMMARY_DATA -> individualSummaryData.asJsonString,
        SessionValues.TAX_YEAR -> taxYear.toString,
        SessionValues.CALCULATION_ID -> "string"
      ))

      val headers = Seq(HeaderNames.COOKIE -> playSessionCookies, "Csrf-Token" -> "nocheck")

      lazy val returnedError = Json.toJson(APIErrorBodyModel(CONFLICT.toString, "RESIDENCY_CHANGED")).toString()
      lazy val request = {
        stubPost(crystallisationUrl, CONFLICT, returnedError)
        FakeRequest("POST", urlPath).withHeaders(headers: _*)
      }

      "redirect to the address has changed page" which {

        lazy val result: Future[Result] = {
          authoriseIndividual()
          route(app, request).get
        }

        "returns status of SEE_OTHER(303)" in {
          status(result) shouldBe SEE_OTHER
        }

        "redirects to the address changed page" in {
          redirectUrl(result) shouldBe expectedAddressChangedUrl
        }
      }
    }

    "the user's tax year return already exists" should {
      lazy val playSessionCookies = PlaySessionCookieBaker.bakeSessionCookie(Map(
        SessionValues.SUMMARY_DATA -> individualSummaryData.asJsonString,
        SessionValues.TAX_YEAR -> taxYear.toString,
        SessionValues.CALCULATION_ID -> "string"
      ))

      val headers = Seq(HeaderNames.COOKIE -> playSessionCookies, "Csrf-Token" -> "nocheck")

      lazy val returnedError = Json.toJson(APIErrorBodyModel(CONFLICT.toString, "FINAL_DECLARATION_RECEIVED")).toString()
      lazy val request = {
        stubPost(crystallisationUrl, CONFLICT, returnedError)
        FakeRequest("POST", urlPath).withHeaders(headers: _*)
      }

      "redirect to the tax year return already exists page" which {

        lazy val result: Future[Result] = {
          authoriseIndividual()
          route(app, request).get
        }

        "returns status of SEE_OTHER(303)" in {
          status(result) shouldBe SEE_OTHER
        }

        "redirects to the tax return exists page" in {
          redirectUrl(result) shouldBe expectedTaxReturnExistsUrl
        }
      }
    }

    "the user's tax year return is already updated" should {
      lazy val playSessionCookies = PlaySessionCookieBaker.bakeSessionCookie(Map(
        SessionValues.SUMMARY_DATA -> individualSummaryData.asJsonString,
        SessionValues.TAX_YEAR -> taxYear.toString,
        SessionValues.CALCULATION_ID -> "string"
      ))

      val headers = Seq(HeaderNames.COOKIE -> playSessionCookies, "Csrf-Token" -> "nocheck")

      lazy val returnedError = Json.toJson(APIErrorBodyModel(CONFLICT.toString, "")).toString()
      lazy val request = {
        stubPost(crystallisationUrl, CONFLICT, returnedError)
        FakeRequest("POST", urlPath).withHeaders(headers: _*)
      }

      "redirect to the tax year return is already updated page" which {

        lazy val result: Future[Result] = {
          authoriseIndividual()
          route(app, request).get
        }

        "returns status of SEE_OTHER(303)" in {
          status(result) shouldBe SEE_OTHER
        }

        "redirects to the tax return previously updated page" in {
          redirectUrl(result) shouldBe expectedTaxReturnPreviouslyUpdatedUrl
        }
      }
    }

    "the users tax return has no valid income sources" should {
      lazy val playSessionCookies = PlaySessionCookieBaker.bakeSessionCookie(Map(
        SessionValues.SUMMARY_DATA -> individualSummaryData.asJsonString,
        SessionValues.TAX_YEAR -> taxYear.toString,
        SessionValues.CALCULATION_ID -> "string"
      ))

      val headers = Seq(HeaderNames.COOKIE -> playSessionCookies, "Csrf-Token" -> "nocheck")

      lazy val returnedError = Json.toJson(APIErrorBodyModel(UNPROCESSABLE_ENTITY.toString, "")).toString()
      lazy val request = {
        stubPost(crystallisationUrl, UNPROCESSABLE_ENTITY, returnedError)
        FakeRequest("POST", urlPath).withHeaders(headers: _*)
      }

      "redirect to the no valid income sources page" which {

        lazy val result: Future[Result] = {
          authoriseIndividual()
          route(app, request).get
        }

        "returns a status of SEE_OTHER(303)" in {
          status(result) shouldBe SEE_OTHER
        }

        "redirects to the no valid income sources page" in {
          redirectUrl(result) shouldBe expectedNoValidIncomeSourcesUrl
        }
      }
    }
  }
}
