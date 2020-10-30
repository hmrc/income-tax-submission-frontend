/*
 * Copyright 2020 HM Revenue & Customs
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

import connectors.httpparsers.IncomeSourcesHttpParser.{IncomeSourcesInvalidJsonException, IncomeSourcesNotFoundException, IncomeSourcesServiceUnavailableException, IncomeSourcesUnhandledException}
import models.{DividendsModel, IncomeSourcesModel}
import play.api.libs.json.Json
import play.mvc.Http.Status._
import utils.IntegrationTest

class IncomeSourcesConnectorSpec extends IntegrationTest {

  lazy val connector: IncomeSourcesConnector = app.injector.instanceOf[IncomeSourcesConnector]

  val nino: String = "123456789"
  val taxYear: Int = 1999
  val dividendResult: Option[BigDecimal] = Some(1111111111)


  ".IncomeSourcesConnector" should {
    "return a IncomeSourcesModel" when {
      "all optional values are present" in {
        val expectedResult = IncomeSourcesModel(Some(DividendsModel(dividendResult, dividendResult)))

        stubGet(s"/income-tax-submission-service/income-tax/nino/$nino/sources\\?taxYear=$taxYear", OK, Json.toJson(expectedResult).toString())

        val result = await(connector.getIncomeSources(nino, taxYear))

        result shouldBe Right(expectedResult)
      }
      "no optional values are present" in {
        val expectedResult = IncomeSourcesModel(None)

        stubGet(s"/income-tax-submission-service/income-tax/nino/$nino/sources\\?taxYear=$taxYear", OK, Json.toJson(expectedResult).toString())

        val result = await(connector.getIncomeSources(nino, taxYear))

        result shouldBe Right(expectedResult)
      }
    }
    "return a IncomeSourcesInvalidJsonException" in {
      val invalidJson = Json.obj(
        "dividends" -> ""
      )

      val expectedResult = IncomeSourcesInvalidJsonException

      stubGet(s"/income-tax-submission-service/income-tax/nino/$nino/sources\\?taxYear=$taxYear", OK, invalidJson.toString())
      val result = await(connector.getIncomeSources(nino, taxYear))

      result shouldBe Left(expectedResult)
    }
    "return a IncomeSourcesServiceUnavailableException" in {
      val expectedResult = IncomeSourcesServiceUnavailableException

      stubGet(s"/income-tax-submission-service/income-tax/nino/$nino/sources\\?taxYear=$taxYear", SERVICE_UNAVAILABLE, "{}")
      val result = await(connector.getIncomeSources(nino, taxYear))

      result shouldBe Left(expectedResult)
    }
    "return a IncomeSourcesNotFoundException" in {
      val expectedResult = IncomeSourcesNotFoundException

      stubGet(s"/income-tax-submission-service/income-tax/nino/$nino/sources\\?taxYear=$taxYear", NOT_FOUND, "{}")
      val result = await(connector.getIncomeSources(nino, taxYear))

      result shouldBe Left(expectedResult)
    }
    "return a IncomeSourcesUnhandledException" in {
      val expectedResult = IncomeSourcesUnhandledException

      stubGet(s"/income-tax-submission-service/income-tax/nino/$nino/sources\\?taxYear=$taxYear", BAD_REQUEST, "{}")
      val result = await(connector.getIncomeSources(nino, taxYear))

      result shouldBe Left(expectedResult)
    }

  }

}
