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

package connectors


import connectors.httpparsers.CalculationIdHttpParser._
import itUtils.IntegrationTest
import models.LiabilityCalculationIdModel
import play.api.libs.json.Json
import play.mvc.Http.Status._

class CalculationIdConnectorSpec extends IntegrationTest {

  lazy val connector: CalculationIdConnector = app.injector.instanceOf[CalculationIdConnector]

  val nino: String = "123456789"
  val taxYear: Int = 1999
  val mtditid: String = "968501689"


  ".CalculationIdConnector" should {

    "return a CalculationIdModel" in {
        val expectedResult = LiabilityCalculationIdModel("00000000-0000-1000-8000-000000000000")

        stubGet(s"/income-tax-calculation/income-tax/nino/$nino/taxYear/$taxYear/tax-calculation\\?mtditid=$mtditid", OK, Json.toJson(expectedResult).toString())

        val result = await(connector.getCalculationId(nino, taxYear, mtditid))

        result shouldBe Right(expectedResult)
      }

    "return a CalculationIdErrorInvalidJsonError" in {
        val expectedResult = CalculationIdErrorInvalidJsonError

        val invalidJson = Json.obj(
          "NotId" -> ""
        )

        stubGet(s"/income-tax-calculation/income-tax/nino/$nino/taxYear/$taxYear/tax-calculation\\?mtditid=$mtditid", OK, Json.toJson(invalidJson).toString())

        val result = await(connector.getCalculationId(nino, taxYear, mtditid))

        result shouldBe Left(expectedResult)
      }
    "return a CalculationIdErrorServiceUnavailableError" in {
        val expectedResult = CalculationIdErrorServiceUnavailableError

        stubGet(s"/income-tax-calculation/income-tax/nino/$nino/taxYear/$taxYear/tax-calculation\\?mtditid=$mtditid", SERVICE_UNAVAILABLE, "{}")

        val result = await(connector.getCalculationId(nino, taxYear, mtditid))

        result shouldBe Left(expectedResult)
      }
    "return a CalculationIdErrorInternalServerError" in {
        val expectedResult = CalculationIdErrorInternalServerError

        stubGet(s"/income-tax-calculation/income-tax/nino/$nino/taxYear/$taxYear/tax-calculation\\?mtditid=$mtditid", INTERNAL_SERVER_ERROR, "{}")

        val result = await(connector.getCalculationId(nino, taxYear, mtditid))

        result shouldBe Left(expectedResult)
      }
    "return a CalculationIdErrorUnhandledError" in {
        val expectedResult = CalculationIdErrorUnhandledError

        stubGet(s"/income-tax-calculation/income-tax/nino/$nino/taxYear/$taxYear/tax-calculation\\?mtditid=$mtditid", BAD_REQUEST, "{}")

        val result = await(connector.getCalculationId(nino, taxYear, mtditid))

        result shouldBe Left(expectedResult)
      }
  }

}

