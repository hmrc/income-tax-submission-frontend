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
import models._
import play.api.http.Status.{IM_A_TEAPOT, INTERNAL_SERVER_ERROR, NO_CONTENT, OK}
import play.api.libs.json.Json

class ExcludedJourneysConnectorISpec extends IntegrationTest {

  val connector: ExcludedJourneysConnector = app.injector.instanceOf[ExcludedJourneysConnector]

  ".getExcludedJourneys" should {

    "return a list of excluded journeys" when {

      "the endpoint returns an OK with a list of excluded journeys in the body" in {

        val result = {
          stubGet(s"/income-tax-submission-service/income-tax/nino/AA123456A/sources/excluded-journeys/$taxYear", OK, Json.stringify(Json.obj(
            "journeys" -> Json.arr(
              Json.obj("journey" -> "interest", "hash" -> "thisIsAHash"),
              Json.obj("journey" -> "dividends")
            )
          )))
          await(connector.getExcludedJourneys(taxYear, nino))
        }

        result shouldBe Right(GetExcludedJourneysResponseModel(Seq(
          ExcludeJourneyModel("interest", Some("thisIsAHash")),
          ExcludeJourneyModel("dividends", None)
        )))
      }

    }

    "return an API Error Model with status Internal Server Error" when {

      "the JSON received from the backend is invalid" in {
        val result = {
          stubGet(s"/income-tax-submission-service/income-tax/nino/AA123456A/sources/excluded-journeys/$taxYear", OK, Json.stringify(Json.obj(
            "aFieldKey" -> "aValue"
          )))
          await(connector.getExcludedJourneys(taxYear, nino))
        }

        result shouldBe Left(APIErrorModel(INTERNAL_SERVER_ERROR, APIErrorBodyModel.parsingError))
      }

      "the backend returns an internal server error" in {
        val result = {
          stubGet(
            s"/income-tax-submission-service/income-tax/nino/AA123456A/sources/excluded-journeys/$taxYear",
            INTERNAL_SERVER_ERROR,
            Json.stringify(Json.obj(
              "code" -> "SUMMET_WENT_WRONG",
              "reason" -> "The backend is feeling unwell"
            ))
          )
          await(connector.getExcludedJourneys(taxYear, nino))
        }

        result shouldBe Left(APIErrorModel(INTERNAL_SERVER_ERROR, APIErrorBodyModel("SUMMET_WENT_WRONG", "The backend is feeling unwell")))
      }

      "the backend returns a status that isn't an internal server error" in {
        val result = {
          stubGet(
            s"/income-tax-submission-service/income-tax/nino/AA123456A/sources/excluded-journeys/$taxYear",
            IM_A_TEAPOT,
            Json.stringify(Json.obj(
              "code" -> "IM_A_TEAPOT",
              "reason" -> "This is not the coffee machine"
            ))
          )
          await(connector.getExcludedJourneys(taxYear, nino))
        }

        result shouldBe Left(APIErrorModel(INTERNAL_SERVER_ERROR, APIErrorBodyModel(
          "IM_A_TEAPOT",
          "This is not the coffee machine"
        )))
      }

    }
  }

  ".clearExcludedJourneys" should {

    "return a NO_CONTENT(204)" when {

      "receiving a NO_CONTENT from the backend" in {
        val result = {
          stubPost(
            s"/income-tax-submission-service/income-tax/nino/AA123456A/sources/clear-excluded-journeys/$taxYear",
            NO_CONTENT, "{}"
          )
          await(connector.clearExcludedJourneys(taxYear, nino, ClearExcludedJourneysRequestModel(
            Seq("interest", "dividends")
          )))
        }

        result shouldBe Right(NO_CONTENT)
      }

    }

    "return an INTERNAL_SERVER_ERROR" when {

      "the backend returns an internal server error" in {
        val result = {
          stubPost(
            s"/income-tax-submission-service/income-tax/nino/AA123456A/sources/clear-excluded-journeys/$taxYear",
            INTERNAL_SERVER_ERROR,
            Json.stringify(Json.obj(
              "code" -> "SUMMET_WENT_WRONG",
              "reason" -> "The backend is feeling unwell"
            ))
          )
          await(connector.clearExcludedJourneys(taxYear, nino, ClearExcludedJourneysRequestModel(
            Seq("interest", "dividends")
          )))
        }

        result shouldBe Left(APIErrorModel(INTERNAL_SERVER_ERROR, APIErrorBodyModel("SUMMET_WENT_WRONG", "The backend is feeling unwell")))
      }

      "the backend returns an unknown error" in {
        val result = {
          stubPost(
            s"/income-tax-submission-service/income-tax/nino/AA123456A/sources/clear-excluded-journeys/$taxYear",
            IM_A_TEAPOT,
            Json.stringify(Json.obj(
              "code" -> "IM_A_TEAPOT",
              "reason" -> "This is not the coffee machine"
            ))
          )
          await(connector.clearExcludedJourneys(taxYear, nino, ClearExcludedJourneysRequestModel(
            Seq("interest", "dividends")
          )))
        }

        result shouldBe Left(APIErrorModel(INTERNAL_SERVER_ERROR, APIErrorBodyModel(
          "IM_A_TEAPOT",
          "This is not the coffee machine"
        )))
      }

    }

  }

}
