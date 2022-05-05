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

package connectors

import itUtils.IntegrationTest
import models.{APIErrorBodyModel, APIErrorModel}
import play.api.http.Status.{NO_CONTENT, SERVICE_UNAVAILABLE}
import play.mvc.Http.Status._

class DeclareCrystallisationConnectorSpec extends IntegrationTest {

  private lazy val connector: DeclareCrystallisationConnector = app.injector.instanceOf[DeclareCrystallisationConnector]

  private val calculationId: String = "041f7e4d-87b9-4d4a-a296-3cfbdf92f7e2"

  private val declareUrl: String = s"/income-tax-calculation/income-tax/nino/$nino/taxYear/$taxYearEOY/$calculationId/declare-crystallisation"

  ".DeclareCrystallisationConnector" should {

    "return a NO_CONTENT" in {
      val expectedResult = Right()

      stubPost(declareUrl, NO_CONTENT, "")

      val result = await(connector.postDeclareCrystallisation(nino, taxYearEOY, calculationId))

      result shouldBe expectedResult
    }

    "return a INTERNAL_SERVER_ERROR" in {
      val expectedResult = APIErrorModel(INTERNAL_SERVER_ERROR, APIErrorBodyModel("INTERNAL_SERVER_ERROR", "Internal server error"))

      stubPost(declareUrl, INTERNAL_SERVER_ERROR, expectedResult.toJson.toString())

      val result = await(connector.postDeclareCrystallisation(nino, taxYearEOY, calculationId))

      result shouldBe Left(expectedResult)
    }
    "return a SERVICE_UNAVAILABLE" in {
      val expectedResult = APIErrorModel(SERVICE_UNAVAILABLE, APIErrorBodyModel("SERVICE_UNAVAILABLE", "Dependent systems are currently not responding."))

      stubPost(declareUrl, SERVICE_UNAVAILABLE, expectedResult.toJson.toString())

      val result = await(connector.postDeclareCrystallisation(nino, taxYearEOY, calculationId))

      result shouldBe Left(expectedResult)
    }
    "return a NOT_FOUND" in {
      val expectedResult = APIErrorModel(NOT_FOUND, APIErrorBodyModel("NOT_FOUND",
        "The remote endpoint has indicated that no calculation exist for given calcId or calcid does not relate to an intent-to-crystallise calculation"))

      stubPost(declareUrl, NOT_FOUND, expectedResult.toJson.toString())

      val result = await(connector.postDeclareCrystallisation(nino, taxYearEOY, calculationId))

      result shouldBe Left(expectedResult)
    }
    "return a CONFLICT" in {
      val expectedResult = APIErrorModel(CONFLICT, APIErrorBodyModel("CONFLICT",
        "The remote endpoint has indicated Income Sources changed - please recalculate before crystallising."))

      stubPost(declareUrl, CONFLICT, expectedResult.toJson.toString())

      val result = await(connector.postDeclareCrystallisation(nino, taxYearEOY, calculationId))

      result shouldBe Left(expectedResult)
    }
    "return a INVALID_IDTYPE" in {
      val expectedResult = APIErrorModel(BAD_REQUEST, APIErrorBodyModel("INVALID_IDTYPE", "Invalid id type"))

      stubPost(declareUrl, BAD_REQUEST, expectedResult.toJson.toString())

      val result = await(connector.postDeclareCrystallisation(nino, taxYearEOY, calculationId))

      result shouldBe Left(expectedResult)
    }
    "return a PARSING_ERROR when unexpected response code" in {
      val expectedResult = APIErrorModel(INTERNAL_SERVER_ERROR,APIErrorBodyModel("PARSING_ERROR","Error parsing response from API"))

      stubPost(declareUrl, GONE, expectedResult.toJson.toString())

      val result = await(connector.postDeclareCrystallisation(nino, taxYearEOY, calculationId))

      result shouldBe Left(expectedResult)
    }
  }

}

