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

package connectors

import itUtils.IntegrationTest
import models.{APIErrorBodyModel, APIErrorModel, LiabilityCalculationIdModel}
import play.api.libs.json.Json
import play.mvc.Http.Status._

class LiabilityCalculationConnectorSpec extends IntegrationTest {

  lazy val connector: LiabilityCalculationConnector = app.injector.instanceOf[LiabilityCalculationConnector]

  private val crystallise: Boolean = true


  ".LiabilityCalculationConnector" should {

    "while calling .getCalculationId" should {

      "return a CalculationIdModel" in {
        val expectedResult = LiabilityCalculationIdModel("00000000-0000-1000-8000-000000000000")

        stubGet(s"/income-tax-calculation/income-tax/nino/$nino/taxYear/$taxYearEOY/tax-calculation", OK, Json.toJson(expectedResult).toString())

        val result = await(connector.getCalculationId(nino, taxYearEOY))

        result shouldBe Right(expectedResult)
      }

      "return a PARSING_ERROR" in {
        val expectedResult = APIErrorModel(INTERNAL_SERVER_ERROR, APIErrorBodyModel("PARSING_ERROR", "Error parsing response from API"))

        val invalidJson = Json.obj(
          "NotId" -> ""
        )

        stubGet(s"/income-tax-calculation/income-tax/nino/$nino/taxYear/$taxYearEOY/tax-calculation", OK, Json.toJson(invalidJson).toString())

        val result = await(connector.getCalculationId(nino, taxYearEOY))

        result shouldBe Left(expectedResult)
      }

      "return a CalculationIdErrorServiceUnavailableError" in {
        val expectedResult = APIErrorModel(SERVICE_UNAVAILABLE, APIErrorBodyModel("SERVICE_UNAVAILABLE", "Service unavailable"))

        stubGet(s"/income-tax-calculation/income-tax/nino/$nino/taxYear/$taxYearEOY/tax-calculation", SERVICE_UNAVAILABLE, expectedResult.toJson.toString())

        val result = await(connector.getCalculationId(nino, taxYearEOY))

        result shouldBe Left(expectedResult)
      }

      "return a INTERNAL_SERVER_ERROR" in {
        val expectedResult = APIErrorModel(INTERNAL_SERVER_ERROR, APIErrorBodyModel("INTERNAL_SERVER_ERROR", "Internal server error"))

        stubGet(s"/income-tax-calculation/income-tax/nino/$nino/taxYear/$taxYearEOY/tax-calculation", INTERNAL_SERVER_ERROR, expectedResult.toJson.toString())

        val result = await(connector.getCalculationId(nino, taxYearEOY))

        result shouldBe Left(expectedResult)
      }

      "return a UNPROCESSABLE_ENTITY/422" in {
        val expectedResult = APIErrorModel(UNPROCESSABLE_ENTITY, APIErrorBodyModel(
          "UNPROCESSABLE_ENTITY", "The remote endpoint has indicated that crystallisation can not occur until after the end of tax year."))

        stubGet(s"/income-tax-calculation/income-tax/nino/$nino/taxYear/$taxYearEOY/tax-calculation", UNPROCESSABLE_ENTITY, expectedResult.toJson.toString())

        val result = await(connector.getCalculationId(nino, taxYearEOY))

        result shouldBe Left(expectedResult)
      }

      "return a FORBIDDEN" in {
        val expectedResult = APIErrorModel(FORBIDDEN, APIErrorBodyModel(
          "FORBIDDEN", "The remote endpoint has indicated that no income submissions exist."))

        stubGet(s"/income-tax-calculation/income-tax/nino/$nino/taxYear/$taxYearEOY/tax-calculation", FORBIDDEN, expectedResult.toJson.toString())

        val result = await(connector.getCalculationId(nino, taxYearEOY))

        result shouldBe Left(expectedResult)
      }

      "return a PARSING_ERROR when unexpected response code" in {
        val expectedResult = APIErrorModel(INTERNAL_SERVER_ERROR, APIErrorBodyModel("PARSING_ERROR", "Error parsing response from API"))

        stubGet(s"/income-tax-calculation/income-tax/nino/$nino/taxYear/$taxYearEOY/tax-calculation", GONE, "{}")

        val result = await(connector.getCalculationId(nino, taxYearEOY))

        result shouldBe Left(expectedResult)
      }

      "return a INVALID_IDTYPE" in {
        val expectedResult = APIErrorModel(BAD_REQUEST, APIErrorBodyModel("INVALID_IDTYPE", "Invalid id type"))

        stubGet(s"/income-tax-calculation/income-tax/nino/$nino/taxYear/$taxYearEOY/tax-calculation", BAD_REQUEST, expectedResult.toJson.toString())

        val result = await(connector.getCalculationId(nino, taxYearEOY))

        result shouldBe Left(expectedResult)
      }
    }

    "while calling .getIntentToCrystallise" should {

      "return a CalculationIdModel" in {
        val expectedResult = LiabilityCalculationIdModel("00000000-0000-1000-8000-000000000000")

        stubGet(s"/income-tax-calculation/income-tax/nino/$nino/taxYear/$taxYearEOY/tax-calculation\\?crystallise=true", OK, Json.toJson(expectedResult).toString())

        val result = await(connector.getIntentToCrystallise(nino, taxYearEOY, crystallise))

        result shouldBe Right(expectedResult)
      }

      "return a PARSING_ERROR" in {
        val expectedResult = APIErrorModel(INTERNAL_SERVER_ERROR, APIErrorBodyModel("PARSING_ERROR", "Error parsing response from API"))

        val invalidJson = Json.obj(
          "NotId" -> ""
        )

        stubGet(s"/income-tax-calculation/income-tax/nino/$nino/taxYear/$taxYearEOY/tax-calculation\\?crystallise=true", OK, Json.toJson(invalidJson).toString())

        val result = await(connector.getIntentToCrystallise(nino, taxYearEOY, crystallise))

        result shouldBe Left(expectedResult)
      }

      "return a CalculationIdErrorServiceUnavailableError" in {
        val expectedResult = APIErrorModel(SERVICE_UNAVAILABLE, APIErrorBodyModel("SERVICE_UNAVAILABLE", "Service unavailable"))

        stubGet(s"/income-tax-calculation/income-tax/nino/$nino/taxYear/$taxYearEOY/tax-calculation\\?crystallise=true", SERVICE_UNAVAILABLE, expectedResult.toJson.toString())

        val result = await(connector.getIntentToCrystallise(nino, taxYearEOY, crystallise))

        result shouldBe Left(expectedResult)
      }

      "return a INTERNAL_SERVER_ERROR" in {
        val expectedResult = APIErrorModel(INTERNAL_SERVER_ERROR, APIErrorBodyModel("INTERNAL_SERVER_ERROR", "Internal server error"))

        stubGet(s"/income-tax-calculation/income-tax/nino/$nino/taxYear/$taxYearEOY/tax-calculation\\?crystallise=true", INTERNAL_SERVER_ERROR, expectedResult.toJson.toString())

        val result = await(connector.getIntentToCrystallise(nino, taxYearEOY, crystallise))

        result shouldBe Left(expectedResult)
      }

      "return a UNPROCESSABLE_ENTITY/422" in {
        val expectedResult = APIErrorModel(UNPROCESSABLE_ENTITY, APIErrorBodyModel(
          "UNPROCESSABLE_ENTITY", "The remote endpoint has indicated that crystallisation can not occur until after the end of tax year."))

        stubGet(s"/income-tax-calculation/income-tax/nino/$nino/taxYear/$taxYearEOY/tax-calculation\\?crystallise=true", UNPROCESSABLE_ENTITY, expectedResult.toJson.toString())

        val result = await(connector.getIntentToCrystallise(nino, taxYearEOY, crystallise))

        result shouldBe Left(expectedResult)
      }

      "return a FORBIDDEN" in {
        val expectedResult = APIErrorModel(FORBIDDEN, APIErrorBodyModel(
          "FORBIDDEN", "The remote endpoint has indicated that no income submissions exist."))

        stubGet(s"/income-tax-calculation/income-tax/nino/$nino/taxYear/$taxYearEOY/tax-calculation\\?crystallise=true", FORBIDDEN, expectedResult.toJson.toString())

        val result = await(connector.getIntentToCrystallise(nino, taxYearEOY, crystallise))

        result shouldBe Left(expectedResult)
      }

      "return a PARSING_ERROR when unexpected response code" in {
        val expectedResult = APIErrorModel(INTERNAL_SERVER_ERROR, APIErrorBodyModel("PARSING_ERROR", "Error parsing response from API"))

        stubGet(s"/income-tax-calculation/income-tax/nino/$nino/taxYear/$taxYearEOY/tax-calculation\\?crystallise=true", GONE, "{}")

        val result = await(connector.getIntentToCrystallise(nino, taxYearEOY, crystallise))

        result shouldBe Left(expectedResult)
      }

      "return a INVALID_IDTYPE" in {
        val expectedResult = APIErrorModel(BAD_REQUEST, APIErrorBodyModel("INVALID_IDTYPE", "Invalid id type"))

        stubGet(s"/income-tax-calculation/income-tax/nino/$nino/taxYear/$taxYearEOY/tax-calculation\\?crystallise=true", BAD_REQUEST, expectedResult.toJson.toString())

        val result = await(connector.getIntentToCrystallise(nino, taxYearEOY, crystallise))

        result shouldBe Left(expectedResult)
      }
    }

  }

}

