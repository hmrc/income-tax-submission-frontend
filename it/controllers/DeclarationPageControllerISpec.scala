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

package controllers

import common.SessionValues
import config.AppConfig
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
import uk.gov.hmrc.http.SessionKeys

import scala.concurrent.Future

class DeclarationPageControllerISpec extends IntegrationTest with ViewHelpers {

  private val crystallisationUrl: String = s"/income-tax-calculation/income-tax/nino/AA123456A/taxYear/$taxYearEOY/string/declare-crystallisation"

  object ExpectedResults {
    val heading: String = "Declaration"
    val subheading: String = "6 April " + taxYearEndOfYearMinusOne + " to " + "5 April " + taxYearEOY

    val agreeButton: String = "Agree and submit"

    val headingWelsh: String = "Datganiad"
    val subheadingWelsh: String = "6 Ebrill " + taxYearEndOfYearMinusOne + " i " + "5 Ebrill " + taxYearEOY

    val agreeButtonWelsh: String = "Cytuno a chyflwyno"

    val addressHasChangedTitle: String = "Your address has changed"

    val returnTaxYearExistsTitle: String = "We already have an Income Tax Return for that tax year"

    val taxYearReturnUpdatedTitle: String = "Your Income Tax Return has been updated"

    val expectedIncomeTaxSubmissionFrontendOverviewUrl: String = s"/update-and-submit-income-tax-return/$taxYearEOY/view"

    val expectedNoValidIncomeSourcesUrl: String = s"/update-and-submit-income-tax-return/$taxYearEOY/no-business-income"

    val expectedTaxReturnPreviouslyUpdatedUrl: String = s"/update-and-submit-income-tax-return/$taxYearEOY/income-tax-return-updated"

    val expectedTaxReturnExistsUrl: String = s"/update-and-submit-income-tax-return/$taxYearEOY/already-have-income-tax-return"

    val expectedAddressChangedUrl: String = s"/update-and-submit-income-tax-return/$taxYearEOY/address-changed"
  }

  object IndividualExpectedResults {

    val individualSummaryData: DeclarationModel = DeclarationModel(
      "Jerry Individual", 9000, "EE54321", 321, 312, 123
    )

    val informationText: String = "I declare that the information and self-assessment I have filed are (taken together) " +
      "correct and complete to the best of my knowledge. I understand that I may have to pay financial penalties and " +
      "face prosecution if I give false information."

    val informationTextWelsh: String = "Rwy’n datgan bod yr wybodaeth a’r Hunanasesiad a gyflwynwyd gennyf (o’u hystyried gyda’i gilydd)" +
      " yn gywir ac yn gyflawn hyd eithaf fy ngwybodaeth. Rwy’n deall y gallai fod yn rhaid i mi dalu cosbau ariannol" +
      " ac wynebu erlyniad os byddaf yn rhoi gwybodaeth anwir."
  }

  object AgentExpectedResults {

    val agentSummaryData: DeclarationModel = DeclarationModel(
      "Joan Agent", 940.40, "EW62340", 231, 132, 321
    )

    val agentInformationText: String = "I confirm that my client has received a copy of all the information being filed " +
      "and approved the information as being correct and complete to the best of their knowledge and belief. My client " +
      "understands that they may have to pay financial penalties and face prosecution if they give false information."

    val agentInformationTextWelsh: String = "Rwy’n cadarnhau bod fy nghleient wedi cael copi o’r holl wybodaeth sy’n cael ei chyflwyno," +
      " a’i fod wedi cytuno bod yr wybodaeth yn gywir ac yn gyflawn hyd eithaf ei wybodaeth a’i gred. Mae fy nghleient" +
      " yn deall y gallai fod yn rhaid iddo dalu cosbau ariannol ac wynebu erlyniad os bydd yn rhoi gwybodaeth anwir."
  }

  object Selectors {
    val headingSelector = "#main-content > div > div > header > h1"
    val subheadingSelector = "#main-content > div > div > header > p"
    val informationTextSelector = "#main-content > div > div > div.govuk-body > p"
    val agreeButtonSelector = "#agree"
  }

  import ExpectedResults._
  import Selectors._

  private val urlPathEOY = s"/update-and-submit-income-tax-return/$taxYearEOY/declaration"
  private val urlPathInYear = s"/update-and-submit-income-tax-return/$taxYear/declaration"

  lazy val frontendAppConfig: AppConfig = app.injector.instanceOf[AppConfig]

  "As an individual, the page should correctly render" when {
    import IndividualExpectedResults._

    "the language is specified as English and" should {
      lazy val playSessionCookies = PlaySessionCookieBaker.bakeSessionCookie(Map(
        SessionKeys.authToken -> "mock-bearer-token",
        SessionValues.SUMMARY_DATA -> individualSummaryData.asJsonString,
        SessionValues.TAX_YEAR -> taxYearEOY.toString,
        SessionValues.VALID_TAX_YEARS -> validTaxYearList.mkString(",")
      ))

      val headers = Seq(HeaderNames.COOKIE -> playSessionCookies, "Csrf-Token" -> "nocheck")

      "display the declaration page" which {
        val request = FakeRequest("GET", urlPathEOY).withHeaders(headers: _*)

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
        SessionKeys.authToken -> "mock-bearer-token",
        SessionValues.SUMMARY_DATA -> individualSummaryData.asJsonString,
        SessionValues.TAX_YEAR -> taxYearEOY.toString,
        SessionValues.VALID_TAX_YEARS -> validTaxYearList.mkString(",")
      ))

      val headers = Seq(HeaderNames.COOKIE -> playSessionCookies, "Csrf-Token" -> "nocheck", HeaderNames.ACCEPT_LANGUAGE -> "cy")

      "display the declaration page in Welsh" which {
        val request = FakeRequest("GET", urlPathEOY).withHeaders(headers: _*)

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

  "/show" should {
    "redirect back to the overview page when trying to access in year" which {
      import IndividualExpectedResults.individualSummaryData
      "the calc id is missing" which {
        lazy val playSessionCookies = PlaySessionCookieBaker.bakeSessionCookie(Map(
          SessionKeys.authToken -> "mock-bearer-token",
          SessionValues.SUMMARY_DATA -> individualSummaryData.asJsonString,
          SessionValues.TAX_YEAR -> taxYear.toString,
          SessionValues.VALID_TAX_YEARS -> validTaxYearList.mkString(",")
        ))
        val headers = Seq(HeaderNames.COOKIE -> playSessionCookies, "Csrf-Token" -> "nocheck")

        lazy val result = {
          authoriseIndividual()
          stubPost(crystallisationUrl, NO_CONTENT, "{}")

          await(wsClient
            .url(s"http://localhost:$port" + urlPathInYear)
            .withHttpHeaders(headers: _*)
            .withFollowRedirects(false)
            .post("{}"))
        }

        "returns a status of SEE_OTHER(303)" in {
          result.status shouldBe SEE_OTHER
        }

        "has a redirect url pointing at the confirmation page" in {
          result.headers("Location").head shouldBe s"http://localhost:9302/update-and-submit-income-tax-return/$taxYear/view"
        }
      }
    }
  }

  "/submit" should {
    "Redirect to the confirmation page" when {
      import IndividualExpectedResults.individualSummaryData

      lazy val playSessionCookies = PlaySessionCookieBaker.bakeSessionCookie(Map(
        SessionKeys.authToken -> "mock-bearer-token",
        SessionValues.SUMMARY_DATA -> individualSummaryData.asJsonString,
        SessionValues.TAX_YEAR -> taxYearEOY.toString,
        SessionValues.VALID_TAX_YEARS -> validTaxYearList.mkString(","),
        SessionValues.CALCULATION_ID -> "string"
      ))

      val headers = Seq(HeaderNames.COOKIE -> playSessionCookies, "Csrf-Token" -> "nocheck")

      "there is summary data and a calculation id in session" which {
        lazy val result = {
          authoriseIndividual()
          stubPost(crystallisationUrl, NO_CONTENT, "{}")

          await(wsClient
            .url(s"http://localhost:$port" + urlPathEOY)
            .withHttpHeaders(headers: _*)
            .withFollowRedirects(false)
            .post("{}"))
        }

        "returns a status of SEE_OTHER(303)" in {
          result.status shouldBe SEE_OTHER
        }

        "has a redirect url pointing at the confirmation page" in {
          result.headers("Location").head shouldBe controllers.routes.TaxReturnReceivedController.show(taxYearEOY).url
        }
      }

    }

    "Redirect to the overview page" when {
      import IndividualExpectedResults.individualSummaryData

      "the calc id is missing" which {
        lazy val playSessionCookies = PlaySessionCookieBaker.bakeSessionCookie(Map(
          SessionKeys.authToken -> "mock-bearer-token",
          SessionValues.SUMMARY_DATA -> individualSummaryData.asJsonString,
          SessionValues.TAX_YEAR -> taxYearEOY.toString,
          SessionValues.VALID_TAX_YEARS -> validTaxYearList.mkString(",")
        ))
        val headers = Seq(HeaderNames.COOKIE -> playSessionCookies, "Csrf-Token" -> "nocheck")

        lazy val result = {
          authoriseIndividual()
          stubPost(crystallisationUrl, NO_CONTENT, "{}")

          await(wsClient
            .url(s"http://localhost:$port" + urlPathEOY)
            .withHttpHeaders(headers: _*)
            .withFollowRedirects(false)
            .post("{}"))
        }

        "returns a status of SEE_OTHER(303)" in {
          result.status shouldBe SEE_OTHER
        }

        "has a redirect url pointing at the confirmation page" in {
          result.headers("Location").head shouldBe controllers.routes.OverviewPageController.show(taxYearEOY).url
        }
      }

      "the summary data is missing" which {
        lazy val playSessionCookies = PlaySessionCookieBaker.bakeSessionCookie(Map(
          SessionKeys.authToken -> "mock-bearer-token",
          SessionValues.TAX_YEAR -> taxYearEOY.toString,
          SessionValues.VALID_TAX_YEARS -> validTaxYearList.mkString(","),
          SessionValues.CALCULATION_ID -> "string"
        ))
        val headers = Seq(HeaderNames.COOKIE -> playSessionCookies, "Csrf-Token" -> "nocheck")

        lazy val result = {
          authoriseIndividual()
          stubPost(crystallisationUrl, NO_CONTENT, "{}")

          await(wsClient
            .url(s"http://localhost:$port" + urlPathEOY)
            .withHttpHeaders(headers: _*)
            .withFollowRedirects(false)
            .post("{}"))
        }

        "returns a status of SEE_OTHER(303)" in {
          result.status shouldBe SEE_OTHER
        }

        "has a redirect url pointing at the confirmation page" in {
          result.headers("Location").head shouldBe controllers.routes.OverviewPageController.show(taxYearEOY).url
        }

      }
    }

    "redirect back to the overview page when in year" which {
      lazy val playSessionCookies = PlaySessionCookieBaker.bakeSessionCookie(Map(
        SessionKeys.authToken -> "mock-bearer-token",
        SessionValues.TAX_YEAR -> taxYear.toString,
        SessionValues.VALID_TAX_YEARS -> validTaxYearList.mkString(","),
        SessionValues.CALCULATION_ID -> "string"
      ))
      val headers = Seq(HeaderNames.COOKIE -> playSessionCookies, "Csrf-Token" -> "nocheck")

      lazy val result = {
        authoriseIndividual()
        stubPost(crystallisationUrl, NO_CONTENT, "{}")

        await(wsClient
          .url(s"http://localhost:$port" + urlPathInYear)
          .withHttpHeaders(headers: _*)
          .withFollowRedirects(false)
          .post("{}"))
      }

      "returns a status of SEE_OTHER(303)" in {
        result.status shouldBe SEE_OTHER
      }

      "has a redirect url to the overview page" in {
        result.headers("Location").head shouldBe s"http://localhost:9302/update-and-submit-income-tax-return/$taxYear/view"
      }
    }
  }

  "As an agent, the page should correctly render" when {
    import AgentExpectedResults._

    "the language is specified as English and" should {
      lazy val playSessionCookies = PlaySessionCookieBaker.bakeSessionCookie(Map(
        SessionKeys.authToken -> "mock-bearer-token",
        SessionValues.SUMMARY_DATA -> agentSummaryData.asJsonString,
        SessionValues.TAX_YEAR -> taxYearEOY.toString,
        SessionValues.VALID_TAX_YEARS -> validTaxYearList.mkString(","),
        SessionValues.CLIENT_NINO -> nino,
        SessionValues.CLIENT_MTDITID -> mtditid
      ))

      val headers = Seq(HeaderNames.COOKIE -> playSessionCookies, "Csrf-Token" -> "nocheck")

      "display the agent declaration page" which {
        val request = FakeRequest("GET", urlPathEOY).withHeaders(headers: _*)

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
        buttonCheck(agreeButton, agreeButtonSelector)
      }

    }

    "the language is specified as Welsh and" should {
      lazy val playSessionCookies = PlaySessionCookieBaker.bakeSessionCookie(Map(
        SessionKeys.authToken -> "mock-bearer-token",
        SessionValues.SUMMARY_DATA -> agentSummaryData.asJsonString,
        SessionValues.TAX_YEAR -> taxYearEOY.toString,
        SessionValues.VALID_TAX_YEARS -> validTaxYearList.mkString(","),
        SessionValues.CLIENT_NINO -> nino,
        SessionValues.CLIENT_MTDITID -> mtditid
      ))

      val headers = Seq(HeaderNames.COOKIE -> playSessionCookies, "Csrf-Token" -> "nocheck", HeaderNames.ACCEPT_LANGUAGE -> "cy")

      "display the agent declaration page in Welsh" which {
        val request = FakeRequest("GET", urlPathEOY).withHeaders(headers: _*)

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
        buttonCheck(agreeButtonWelsh, agreeButtonSelector)
      }
    }
  }

  "As an individual" when {

    import IndividualExpectedResults._

    "there is no Summary Data in session which" should {
      lazy val playSessionCookies = PlaySessionCookieBaker.bakeSessionCookie(Map(
        SessionKeys.authToken -> "mock-bearer-token",
        SessionValues.TAX_YEAR -> taxYearEOY.toString,
        SessionValues.VALID_TAX_YEARS -> validTaxYearList.mkString(",")
      ))

      val headers = Seq(HeaderNames.COOKIE -> playSessionCookies, "Csrf-Token" -> "nocheck")

      "redirect to the overview page" which {
        val request = FakeRequest("GET", urlPathEOY).withHeaders(headers: _*)

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
        SessionKeys.authToken -> "mock-bearer-token",
        SessionValues.SUMMARY_DATA -> individualSummaryData.asJsonString,
        SessionValues.TAX_YEAR -> taxYearEOY.toString,
        SessionValues.VALID_TAX_YEARS -> validTaxYearList.mkString(","),
        SessionValues.CALCULATION_ID -> "string"
      ))

      val headers = Seq(HeaderNames.COOKIE -> playSessionCookies, "Csrf-Token" -> "nocheck")

      lazy val returnedError = Json.toJson(APIErrorBodyModel(CONFLICT.toString, "RESIDENCY_CHANGED")).toString()
      lazy val request = {
        stubPost(crystallisationUrl, CONFLICT, returnedError)
        FakeRequest("POST", urlPathEOY).withHeaders(headers: _*)
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
        SessionKeys.authToken -> "mock-bearer-token",
        SessionValues.SUMMARY_DATA -> individualSummaryData.asJsonString,
        SessionValues.TAX_YEAR -> taxYearEOY.toString,
        SessionValues.VALID_TAX_YEARS -> validTaxYearList.mkString(","),
        SessionValues.CALCULATION_ID -> "string"
      ))

      val headers = Seq(HeaderNames.COOKIE -> playSessionCookies, "Csrf-Token" -> "nocheck")

      lazy val returnedError = Json.toJson(APIErrorBodyModel(CONFLICT.toString, "FINAL_DECLARATION_RECEIVED")).toString()
      lazy val request = {
        stubPost(crystallisationUrl, CONFLICT, returnedError)
        FakeRequest("POST", urlPathEOY).withHeaders(headers: _*)
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
        SessionKeys.authToken -> "mock-bearer-token",
        SessionValues.SUMMARY_DATA -> individualSummaryData.asJsonString,
        SessionValues.TAX_YEAR -> taxYearEOY.toString,
        SessionValues.VALID_TAX_YEARS -> validTaxYearList.mkString(","),
        SessionValues.CALCULATION_ID -> "string"
      ))

      val headers = Seq(HeaderNames.COOKIE -> playSessionCookies, "Csrf-Token" -> "nocheck")

      lazy val returnedError = Json.toJson(APIErrorBodyModel(CONFLICT.toString, "")).toString()
      lazy val request = {
        stubPost(crystallisationUrl, CONFLICT, returnedError)
        FakeRequest("POST", urlPathEOY).withHeaders(headers: _*)
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
        SessionKeys.authToken -> "mock-bearer-token",
        SessionValues.SUMMARY_DATA -> individualSummaryData.asJsonString,
        SessionValues.TAX_YEAR -> taxYearEOY.toString,
        SessionValues.VALID_TAX_YEARS -> validTaxYearList.mkString(","),
        SessionValues.CALCULATION_ID -> "string"
      ))

      val headers = Seq(HeaderNames.COOKIE -> playSessionCookies, "Csrf-Token" -> "nocheck")

      lazy val returnedError = Json.toJson(APIErrorBodyModel(UNPROCESSABLE_ENTITY.toString, "")).toString()
      lazy val request = {
        stubPost(crystallisationUrl, UNPROCESSABLE_ENTITY, returnedError)
        FakeRequest("POST", urlPathEOY).withHeaders(headers: _*)
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
