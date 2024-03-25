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
import models.calculation.{CalculationResponseModel, Inputs, Metadata, PersonalInformation}
import models.{APIErrorBodyModel, APIErrorModel}
import play.api.libs.json.{JsObject, Json}
import play.mvc.Http.Status._

class IncomeTaxCalculationConnectorISpec extends IntegrationTest {

  lazy val connector: IncomeTaxCalculationConnector = app.injector.instanceOf[IncomeTaxCalculationConnector]

  val calculationId = "041f7e4d-87b9-4d4a-a296-3cfbdf92f7e2"

  val calculationResponseModel: CalculationResponseModel = CalculationResponseModel(
    inputs = Inputs(PersonalInformation(taxRegime = "UK", class2VoluntaryContributions = None)),
    messages = None,
    metadata = Metadata(Some("2019-02-15T09:35:15.094Z"), Some(false), Some("customerRequest"),None,None),
    calculation = None)

  val calculationJson: JsObject = Json.obj("inputs" -> Json.obj("personalInformation" ->
    Json.obj("taxRegime" -> "UK")),
    "metadata" -> Json.obj("calculationTimestamp" -> "2019-02-15T09:35:15.094Z", "crystallised" -> false,
      "calculationReason" -> "customerRequest"))

  private def getCalculationResponseByCalcIdUrl(nino: String, calcId: String): String =
    s"/income-tax-calculation/income-tax/nino/$nino/calc-id/$calcId/calculation-details"

  ".IncomeTaxCalculationConnector" should {

    "while calling .getCalculationResponseByCalcId" should {

      "return a CalculationDetailResponse" in {
        stubGet(getCalculationResponseByCalcIdUrl(nino,calculationId), OK, Json.toJson(calculationResponseModel).toString())

        val result = await(connector.getCalculationResponseByCalcId(mtditid, nino, calculationId, taxYearEOY))

        result shouldBe Right(calculationResponseModel)
      }

      "return a PARSING_ERROR" in {
        val expectedResult = APIErrorModel(INTERNAL_SERVER_ERROR, APIErrorBodyModel("PARSING_ERROR",
          "Error parsing response from API - List(" +
            "(/metadata,List(JsonValidationError(List(error.path.missing),List()))), (/inputs," +
            "List(JsonValidationError(List(error.path.missing),List()))))"))

        val invalidJson = Json.obj(
          "NotId" -> ""
        )

        stubGet(getCalculationResponseByCalcIdUrl(nino,calculationId), OK, Json.toJson(invalidJson).toString())
        val result = await(connector.getCalculationResponseByCalcId(mtditid, nino, calculationId, taxYearEOY))
        result shouldBe Left(expectedResult)
      }

      "return a CalculationIdErrorServiceUnavailableError" in {
        val expectedResult = APIErrorModel(INTERNAL_SERVER_ERROR, APIErrorBodyModel("SERVICE_UNAVAILABLE", "Service unavailable"))

        stubGet(getCalculationResponseByCalcIdUrl(nino,calculationId), SERVICE_UNAVAILABLE, expectedResult.toJson.toString())

        val result = await(connector.getCalculationResponseByCalcId(mtditid, nino, calculationId, taxYearEOY))

        result shouldBe Left(expectedResult)
      }

      "return a INTERNAL_SERVER_ERROR" in {
        val expectedResult = APIErrorModel(INTERNAL_SERVER_ERROR, APIErrorBodyModel("INTERNAL_SERVER_ERROR", "Internal server error"))

        stubGet(getCalculationResponseByCalcIdUrl(nino,calculationId), INTERNAL_SERVER_ERROR, expectedResult.toJson.toString())

        val result = await(connector.getCalculationResponseByCalcId(mtditid, nino, calculationId, taxYearEOY))

        result shouldBe Left(expectedResult)
      }

      
      "return a UNPROCESSABLE_ENTITY/422" in {
        val expectedResult = APIErrorModel(BAD_REQUEST, APIErrorBodyModel(
          "UNPROCESSABLE_ENTITY", "The remote endpoint has indicated that crystallisation can not occur until after the end of tax year."))

        stubGet(getCalculationResponseByCalcIdUrl(nino,calculationId), UNPROCESSABLE_ENTITY, expectedResult.toJson.toString())

        val result = await(connector.getCalculationResponseByCalcId(mtditid, nino, calculationId, taxYearEOY))

        result shouldBe Left(expectedResult)
      }


      "return a PARSING_ERROR when unexpected response code" in {
        val expectedResult = APIErrorModel(INTERNAL_SERVER_ERROR, APIErrorBodyModel("PARSING_ERROR", "Error parsing response from API"))

        stubGet(getCalculationResponseByCalcIdUrl(nino,calculationId), GONE, "{}")

        val result = await(connector.getCalculationResponseByCalcId(mtditid, nino, calculationId, taxYearEOY))

        result shouldBe Left(expectedResult)
      }

      "return a INVALID_IDTYPE" in {
        val expectedResult = APIErrorModel(BAD_REQUEST, APIErrorBodyModel("INVALID_IDTYPE", "Invalid id type"))

        stubGet(getCalculationResponseByCalcIdUrl(nino,calculationId), BAD_REQUEST, expectedResult.toJson.toString())

        val result = await(connector.getCalculationResponseByCalcId(mtditid, nino, calculationId, taxYearEOY))

        result shouldBe Left(expectedResult)
      }
    }
  }
}

