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

package connectors

import itUtils.IntegrationTest
import models.{APIErrorBodyModel, APIErrorModel, ValidTaxYearListModel}
import play.api.libs.json.Json
import play.mvc.Http.Status._

class ValidTaxYearListConnectorSpec extends IntegrationTest {

  lazy val connector: ValidTaxYearListConnector = app.injector.instanceOf[ValidTaxYearListConnector]

  ".ValidTaxYearListConnector" should {

    "while calling .getValidTaxYearList" should {

      "return a ValidTaxYearListModel" in {
        val expectedResult = ValidTaxYearListModel(Seq(2020, 2021, 2022, 2023))

        stubGet(s"/income-tax-calculation/income-tax/nino/$nino/tax-years", OK, Json.toJson(expectedResult).toString())

        val result = await(connector.getValidTaxYearList(nino))

        result shouldBe Right(expectedResult)
      }

      "return a PARSING_ERROR" in {
        val expectedResult = APIErrorModel(INTERNAL_SERVER_ERROR, APIErrorBodyModel("PARSING_ERROR", "Error parsing response from API"))

        val invalidJson = Json.obj(
          "NotId" -> ""
        )

        stubGet(s"/income-tax-calculation/income-tax/nino/$nino/tax-years", OK, Json.toJson(invalidJson).toString())

        val result = await(connector.getValidTaxYearList(nino))

        result shouldBe Left(expectedResult)
      }

      "return a ValidTaxYearListErrorServiceUnavailableError" in {
        val expectedResult = APIErrorModel(SERVICE_UNAVAILABLE, APIErrorBodyModel("SERVICE_UNAVAILABLE", "Service unavailable"))

        stubGet(s"/income-tax-calculation/income-tax/nino/$nino/tax-years", SERVICE_UNAVAILABLE, expectedResult.toJson.toString())

        val result = await(connector.getValidTaxYearList(nino))

        result shouldBe Left(expectedResult)
      }

      "return a INTERNAL_SERVER_ERROR" in {
        val expectedResult = APIErrorModel(INTERNAL_SERVER_ERROR, APIErrorBodyModel("INTERNAL_SERVER_ERROR", "Internal server error"))

        stubGet(s"/income-tax-calculation/income-tax/nino/$nino/tax-years", INTERNAL_SERVER_ERROR, expectedResult.toJson.toString())

        val result = await(connector.getValidTaxYearList(nino))

        result shouldBe Left(expectedResult)
      }

      "return a UNPROCESSABLE_ENTITY/422" in {
        val expectedResult = APIErrorModel(UNPROCESSABLE_ENTITY, APIErrorBodyModel(
          "UNPROCESSABLE_ENTITY", "The remote endpoint has indicated that crystallisation can not occur until after the end of tax year."))

        stubGet(s"/income-tax-calculation/income-tax/nino/$nino/tax-years", UNPROCESSABLE_ENTITY, expectedResult.toJson.toString())

        val result = await(connector.getValidTaxYearList(nino))

        result shouldBe Left(expectedResult)
      }

      "return a FORBIDDEN" in {
        val expectedResult = APIErrorModel(FORBIDDEN, APIErrorBodyModel(
          "FORBIDDEN", "The remote endpoint has indicated that no income submissions exist."))

        stubGet(s"/income-tax-calculation/income-tax/nino/$nino/tax-years", FORBIDDEN, expectedResult.toJson.toString())

        val result = await(connector.getValidTaxYearList(nino))

        result shouldBe Left(expectedResult)
      }

      "return a PARSING_ERROR when unexpected response code" in {
        val expectedResult = APIErrorModel(INTERNAL_SERVER_ERROR, APIErrorBodyModel("PARSING_ERROR", "Error parsing response from API"))

        stubGet(s"/income-tax-calculation/income-tax/nino/$nino/tax-years", GONE, "{}")

        val result = await(connector.getValidTaxYearList(nino))

        result shouldBe Left(expectedResult)
      }

      "return a INVALID_IDTYPE" in {
        val expectedResult = APIErrorModel(BAD_REQUEST, APIErrorBodyModel("INVALID_IDTYPE", "Invalid id type"))

        stubGet(s"/income-tax-calculation/income-tax/nino/$nino/tax-years", BAD_REQUEST, expectedResult.toJson.toString())

        val result = await(connector.getValidTaxYearList(nino))

        result shouldBe Left(expectedResult)
      }
    }

  }

}

