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

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import common.SessionValues
import config.AppConfig
import helpers.PlaySessionCookieBaker
import itUtils.{IntegrationTest, ViewHelpers}
import models.mongo.{DatabaseError, ExclusionUserDataModel}
import models.{ExcludeJourneyModel, IncomeSourcesModel, User}
import play.api.http.HeaderNames
import play.api.http.Status._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.mvc.AnyContent
import play.api.test.Helpers.OK
import play.api.{Application, Environment, Mode}
import repositories.ExclusionUserDataRepository

class ExcludeJourneyControllerISpec extends IntegrationTest with ViewHelpers {

  lazy val frontendAppConfig: AppConfig = app.injector.instanceOf[AppConfig]

  private val urlPath = s"/update-and-submit-income-tax-return/$taxYear/exclude-journey"

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .in(Environment.simple(mode = Mode.Dev))
    .configure(config(useEncryption = false, tailoringEnabled = true))
    .build


  def stubIncomeSources(incomeSources: IncomeSourcesModel): StubMapping = {
    stubGet(s"/income-tax-submission-service/income-tax/nino/AA123456A/sources\\?taxYear=$taxYear", OK, Json.toJson(incomeSources).toString())
  }
  
  private lazy val repo: ExclusionUserDataRepository = app.injector.instanceOf[ExclusionUserDataRepository]

  private implicit val mtdUser: User[AnyContent] = User("1234567890", None, "AA123456A", "1234567890")
  
  private def cleanDatabase(taxYear: Int) = {
    await(repo.clear(taxYear))
    await(repo.ensureIndexes)
  }
  
  private def insertJourneys(endOfYear: Boolean, journeys: ExcludeJourneyModel*): Either[DatabaseError, Boolean] = {
    await(repo.create(ExclusionUserDataModel(
      "AA123456A",
      if (endOfYear) taxYearEOY else taxYear,
      journeys
    )))
  }
  
  private def insertJourney(endOfYear: Boolean = false) = {
    insertJourneys(
      endOfYear,
      ExcludeJourneyModel("dividends", None)
    )
  }
  "ExcludeJourneyController.excludeJourney" should {
    "redirect to the overview page when submitting the form successfully with Dividends" when {

      lazy val form = Map(s"Journey" -> "dividends")

      lazy val playSessionCookies = PlaySessionCookieBaker.bakeSessionCookie(Map(
        SessionValues.TAX_YEAR -> taxYear.toString,
        SessionValues.VALID_TAX_YEARS -> validTaxYearList.mkString(",")
      ))

      val headers = Seq(HeaderNames.COOKIE -> playSessionCookies, "Csrf-Token" -> "nocheck")

      lazy val result: WSResponse = {
        authoriseIndividual()
        stubIncomeSources(incomeSourcesModel)

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
    "redirect to the overview page when submitting the form successfully with employment" when {

      lazy val form = Map(s"Journey" -> "employment")

      lazy val playSessionCookies = PlaySessionCookieBaker.bakeSessionCookie(Map(
        SessionValues.TAX_YEAR -> taxYear.toString,
        SessionValues.VALID_TAX_YEARS -> validTaxYearList.mkString(",")
      ))

      val headers = Seq(HeaderNames.COOKIE -> playSessionCookies, "Csrf-Token" -> "nocheck")

      lazy val result: WSResponse = {
        authoriseIndividual()
        stubIncomeSources(incomeSourcesModel)

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
    "redirect to the overview page when submitting the form successfully with CIS" when {

      lazy val form = Map(s"Journey" -> "cis")

      lazy val playSessionCookies = PlaySessionCookieBaker.bakeSessionCookie(Map(
        SessionValues.TAX_YEAR -> taxYear.toString,
        SessionValues.VALID_TAX_YEARS -> validTaxYearList.mkString(",")
      ))

      val headers = Seq(HeaderNames.COOKIE -> playSessionCookies, "Csrf-Token" -> "nocheck")

      lazy val result: WSResponse = {
        authoriseIndividual()
        stubIncomeSources(incomeSourcesModel)

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
    "redirect to the overview page when submitting the form successfully with gift-aid" when {

      lazy val form = Map(s"Journey" -> "gift-aid")

      lazy val playSessionCookies = PlaySessionCookieBaker.bakeSessionCookie(Map(
        SessionValues.TAX_YEAR -> taxYear.toString,
        SessionValues.VALID_TAX_YEARS -> validTaxYearList.mkString(",")
      ))

      val headers = Seq(HeaderNames.COOKIE -> playSessionCookies, "Csrf-Token" -> "nocheck")

      lazy val result: WSResponse = {
        authoriseIndividual()
        stubIncomeSources(incomeSourcesModel)

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
    "redirect to the overview page when submitting the form successfully with interests" when {

      lazy val form = Map(s"Journey" -> "interest")

      lazy val playSessionCookies = PlaySessionCookieBaker.bakeSessionCookie(Map(
        SessionValues.TAX_YEAR -> taxYear.toString,
        SessionValues.VALID_TAX_YEARS -> validTaxYearList.mkString(",")
      ))

      val headers = Seq(HeaderNames.COOKIE -> playSessionCookies, "Csrf-Token" -> "nocheck")

      lazy val result: WSResponse = {
        authoriseIndividual()
        stubIncomeSources(incomeSourcesModel)

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
    "redirect to the overview page when submitting an invalid form" when {

      lazy val form = Map(s"failure" -> "interest")

      lazy val playSessionCookies = PlaySessionCookieBaker.bakeSessionCookie(Map(
        SessionValues.TAX_YEAR -> taxYear.toString,
        SessionValues.VALID_TAX_YEARS -> validTaxYearList.mkString(",")
      ))

      val headers = Seq(HeaderNames.COOKIE -> playSessionCookies, "Csrf-Token" -> "nocheck")

      lazy val result: WSResponse = {
        authoriseIndividual()
        stubIncomeSources(incomeSourcesModel)

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
    "redirect to the overview page when submitting an invalid journey" when {

      lazy val form = Map(s"Journey" -> "interestNot")

      lazy val playSessionCookies = PlaySessionCookieBaker.bakeSessionCookie(Map(
        SessionValues.TAX_YEAR -> taxYear.toString,
        SessionValues.VALID_TAX_YEARS -> validTaxYearList.mkString(",")
      ))

      val headers = Seq(HeaderNames.COOKIE -> playSessionCookies, "Csrf-Token" -> "nocheck")

      lazy val result: WSResponse = {
        authoriseIndividual()
        stubIncomeSources(incomeSourcesModel)

        await(wsClient
          .url(s"http://localhost:$port" + urlPath)
          .withHttpHeaders(headers: _*)
          .withFollowRedirects(false)
          .post(form))
      }

      "returns a SEE_OTHER (303) result" in {
        result.status shouldBe INTERNAL_SERVER_ERROR
      }

    }
    "redirect to the overview page tailoring is disabled" when {

      lazy val playSessionCookies = PlaySessionCookieBaker.bakeSessionCookie(Map(
        SessionValues.TAX_YEAR -> taxYear.toString,
        SessionValues.VALID_TAX_YEARS -> validTaxYearList.mkString(",")
      ))

      val headers = Seq(HeaderNames.COOKIE -> playSessionCookies, "Csrf-Token" -> "nocheck")

      lazy val result: WSResponse = {
        authoriseIndividual()
        stubIncomeSources(incomeSourcesModel)

        await(customApp(tailoringEnabled = false).injector.instanceOf[WSClient]
          .url(s"http://localhost:$port" + urlPath)
          .withHttpHeaders(headers: _*)
          .withFollowRedirects(false)
          .post(""))
      }

      "returns a SEE_OTHER (303) result" in {
        result.status shouldBe SEE_OTHER
      }

      "redirect url location is pointing to the overview page" in {
        result.header(HeaderNames.LOCATION) shouldBe Some(controllers.routes.OverviewPageController.show(taxYear).url)
      }
    }
  }


}
