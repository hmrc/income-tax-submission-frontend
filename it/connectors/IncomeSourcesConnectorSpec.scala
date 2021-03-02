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

import connectors.httpparsers.IncomeSourcesHttpParser.{IncomeSourcesInternalServerError, IncomeSourcesInvalidJsonError, IncomeSourcesServiceUnavailableError, IncomeSourcesUnhandledError}
import itUtils.IntegrationTest
import models.{DividendsModel, IncomeSourcesModel, InterestModel}
import play.api.libs.json.Json
import play.mvc.Http.Status._

class IncomeSourcesConnectorSpec extends IntegrationTest {

  lazy val connector: IncomeSourcesConnector = app.injector.instanceOf[IncomeSourcesConnector]

  val nino: String = "123456789"
  val taxYear: Int = 1999
  val mtditid: String = "968501689"
  val dividendResult: Option[DividendsModel] = Some(DividendsModel(Some(500), Some(600)))
  val interestResult: Option[Seq[InterestModel]] = Some(Seq(InterestModel("account", "1234567890", Some(500), Some(500))))


  ".IncomeSourcesConnector" should {
    "return a IncomeSourcesModel" when {
      "all optional values are present" in {
        val expectedResult = IncomeSourcesModel(dividendResult, interestResult)

        stubGet(s"/income-tax-submission-service/income-tax/nino/$nino/sources\\?taxYear=$taxYear&mtditid=968501689", OK, Json.toJson(expectedResult).toString())

        val result = await(connector.getIncomeSources(nino, taxYear, mtditid))

        result shouldBe Right(expectedResult)
      }
      "no optional values are present" in {
        val expectedResult = IncomeSourcesModel(None, None)

        stubGet(s"/income-tax-submission-service/income-tax/nino/$nino/sources\\?taxYear=$taxYear&mtditid=968501689", OK, Json.toJson(expectedResult).toString())

        val result = await(connector.getIncomeSources(nino, taxYear, mtditid))

        result shouldBe Right(expectedResult)
      }
    }
    "return a IncomeSourcesInvalidJsonException" in {
      val invalidJson = Json.obj(
        "dividends" -> ""
      )

      val expectedResult = IncomeSourcesInvalidJsonError

      stubGet(s"/income-tax-submission-service/income-tax/nino/$nino/sources\\?taxYear=$taxYear&mtditid=968501689", OK, invalidJson.toString())
      val result = await(connector.getIncomeSources(nino, taxYear, mtditid))

      result shouldBe Left(expectedResult)
    }
    "return a IncomeSourcesServiceUnavailableException" in {
      val expectedResult = IncomeSourcesServiceUnavailableError

      stubGet(s"/income-tax-submission-service/income-tax/nino/$nino/sources\\?taxYear=$taxYear&mtditid=968501689", SERVICE_UNAVAILABLE, "{}")
      val result = await(connector.getIncomeSources(nino, taxYear, mtditid))

      result shouldBe Left(expectedResult)
    }
    "return a Right(IncomeSourcesModel())" in {
      val expectedResult = Right(IncomeSourcesModel())

      stubGet(s"/income-tax-submission-service/income-tax/nino/$nino/sources\\?taxYear=$taxYear&mtditid=968501689", NOT_FOUND, "{}")
      val result = await(connector.getIncomeSources(nino, taxYear, mtditid))

      result shouldBe expectedResult
    }
    "return a Right(IncomeSourcesModel()) from a 204" in {
      val expectedResult = Right(IncomeSourcesModel())

      stubGet(s"/income-tax-submission-service/income-tax/nino/$nino/sources\\?taxYear=$taxYear&mtditid=968501689", NO_CONTENT, "{}")
      val result = await(connector.getIncomeSources(nino, taxYear, mtditid))

      result shouldBe expectedResult
    }
    "return a IncomeSourcesInternalServerError" in {
      val expectedResult = IncomeSourcesInternalServerError

      stubGet(s"/income-tax-submission-service/income-tax/nino/$nino/sources\\?taxYear=$taxYear&mtditid=968501689", INTERNAL_SERVER_ERROR, "{}")
      val result = await(connector.getIncomeSources(nino, taxYear, mtditid))

      result shouldBe Left(expectedResult)
    }
    "return a IncomeSourcesUnhandledException" in {
      val expectedResult = IncomeSourcesUnhandledError

      stubGet(s"/income-tax-submission-service/income-tax/nino/$nino/sources\\?taxYear=$taxYear&mtditid=968501689", BAD_REQUEST, "{}")
      val result = await(connector.getIncomeSources(nino, taxYear, mtditid))

      result shouldBe Left(expectedResult)
    }

  }

}
