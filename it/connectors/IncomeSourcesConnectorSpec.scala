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

import itUtils.IntegrationTest
import models.{APIErrorBodyModel, APIErrorModel, APIErrorsBodyModel, DividendsModel, GiftAidModel, GiftAidPaymentsModel, GiftsModel, IncomeSourcesModel, InterestModel}
import play.api.libs.json.Json
import play.mvc.Http.Status._

class IncomeSourcesConnectorSpec extends IntegrationTest {

  lazy val connector: IncomeSourcesConnector = app.injector.instanceOf[IncomeSourcesConnector]

  val nino: String = "123456789"
  val taxYear: Int = 1999
  val mtditid: String = "968501689"
  val dividendResult: Option[DividendsModel] = Some(DividendsModel(Some(500), Some(600)))
  val interestResult: Option[Seq[InterestModel]] = Some(Seq(InterestModel("account", "1234567890", Some(500), Some(500))))
  val giftAidPaymentsModel: Option[GiftAidPaymentsModel] = Some(GiftAidPaymentsModel(
    nonUkCharitiesCharityNames = Some(List("non uk charity name","non uk charity name 2")),
    currentYear = Some(1234.56),
    oneOffCurrentYear = Some(1234.56),
    currentYearTreatedAsPreviousYear = Some(1234.56),
    nextYearTreatedAsCurrentYear = Some(1234.56),
    nonUkCharities = Some(1234.56),
  ))

  val giftsModel: Option[GiftsModel] = Some(GiftsModel(
    investmentsNonUkCharitiesCharityNames = Some(List("charity 1", "charity 2")),
    landAndBuildings = Some(10.21),
    sharesOrSecurities = Some(10.21),
    investmentsNonUkCharities = Some(1234.56)
  ))

  val giftAidResult: Option[GiftAidModel] = Some(GiftAidModel(
    giftAidPaymentsModel,
    giftsModel
  ))


  ".IncomeSourcesConnector" should {
    "return a IncomeSourcesModel" when {
      "all optional values are present" in {
        val expectedResult = IncomeSourcesModel(dividendResult, interestResult, giftAidResult)

        stubGet(s"/income-tax-submission-service/income-tax/nino/$nino/sources\\?taxYear=$taxYear", OK, Json.toJson(expectedResult).toString())

        val result = await(connector.getIncomeSources(nino, taxYear))

        result shouldBe Right(expectedResult)
      }
      "no optional values are present" in {
        val expectedResult = IncomeSourcesModel(None, None)

        stubGet(s"/income-tax-submission-service/income-tax/nino/$nino/sources\\?taxYear=$taxYear", OK, Json.toJson(expectedResult).toString())

        val result = await(connector.getIncomeSources(nino, taxYear))

        result shouldBe Right(expectedResult)
      }
    }
    "non json is returned" in {
      stubGet(s"/income-tax-submission-service/income-tax/nino/$nino/sources\\?taxYear=$taxYear", INTERNAL_SERVER_ERROR, "")

      val result = await(connector.getIncomeSources(nino, taxYear))

      result shouldBe Left(APIErrorModel(INTERNAL_SERVER_ERROR, APIErrorBodyModel.parsingError))
    }

    "API Returns multiple errors" in {
      val expectedResult = APIErrorModel(INTERNAL_SERVER_ERROR, APIErrorsBodyModel(Seq(
        APIErrorBodyModel("INVALID_IDTYPE","ID is invalid"),
        APIErrorBodyModel("INVALID_IDTYPE_2","ID 2 is invalid"))))

      val responseBody = Json.obj(
        "failures" -> Json.arr(
          Json.obj("code" -> "INVALID_IDTYPE",
            "reason" -> "ID is invalid"),
          Json.obj("code" -> "INVALID_IDTYPE_2",
            "reason" -> "ID 2 is invalid")
        )
      )
      stubGet(s"/income-tax-submission-service/income-tax/nino/$nino/sources\\?taxYear=$taxYear", BAD_REQUEST, responseBody.toString())

      val result = await(connector.getIncomeSources(nino, taxYear))

      result shouldBe Left(expectedResult)
    }

    "return a PARSING_ERROR" in {
      val invalidJson = Json.obj(
        "dividends" -> ""
      )

      val expectedResult = APIErrorModel(500,APIErrorBodyModel("PARSING_ERROR","Error parsing response from API"))

      stubGet(s"/income-tax-submission-service/income-tax/nino/$nino/sources\\?taxYear=$taxYear", OK, invalidJson.toString())
      val result = await(connector.getIncomeSources(nino, taxYear))

      result shouldBe Left(expectedResult)
    }
    "return a SERVICE_UNAVAILABLE" in {
      val expectedResult = APIErrorModel(503,APIErrorBodyModel("SERVICE_UNAVAILABLE","Service unavailable"))

      stubGet(s"/income-tax-submission-service/income-tax/nino/$nino/sources\\?taxYear=$taxYear", SERVICE_UNAVAILABLE, expectedResult.toJson.toString())
      val result = await(connector.getIncomeSources(nino, taxYear))

      result shouldBe Left(expectedResult)
    }
    "return a Right(IncomeSourcesModel())" in {
      val expectedResult = Right(IncomeSourcesModel())

      stubGet(s"/income-tax-submission-service/income-tax/nino/$nino/sources\\?taxYear=$taxYear", NOT_FOUND, "{}")
      val result = await(connector.getIncomeSources(nino, taxYear))

      result shouldBe expectedResult
    }
    "return a Right(IncomeSourcesModel()) from a 204" in {
      val expectedResult = Right(IncomeSourcesModel())

      stubGet(s"/income-tax-submission-service/income-tax/nino/$nino/sources\\?taxYear=$taxYear", NO_CONTENT, "{}")
      val result = await(connector.getIncomeSources(nino, taxYear))

      result shouldBe expectedResult
    }
    "return a INTERNAL_SERVER_ERROR" in {
      val expectedResult = APIErrorModel(500,APIErrorBodyModel("INTERNAL_SERVER_ERROR","Internal server error"))

      stubGet(s"/income-tax-submission-service/income-tax/nino/$nino/sources\\?taxYear=$taxYear", INTERNAL_SERVER_ERROR,expectedResult.toJson.toString())
      val result = await(connector.getIncomeSources(nino, taxYear))

      result shouldBe Left(expectedResult)
    }
    "return a PARSING_ERROR when unexpected status 408" in {
      val expectedResult = APIErrorModel(500,APIErrorBodyModel("PARSING_ERROR","Error parsing response from API"))

      stubGet(s"/income-tax-submission-service/income-tax/nino/$nino/sources\\?taxYear=$taxYear", REQUEST_TIMEOUT, "")
      val result = await(connector.getIncomeSources(nino, taxYear))

      result shouldBe Left(expectedResult)
    }

  }

}
