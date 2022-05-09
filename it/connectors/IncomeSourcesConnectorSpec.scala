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
import models._
import models.employment._
import play.api.libs.json.Json
import play.mvc.Http.Status._

class IncomeSourcesConnectorSpec extends IntegrationTest {

  private lazy val connector: IncomeSourcesConnector = app.injector.instanceOf[IncomeSourcesConnector]
  private lazy val connectorWithSourcesTurnedOff: IncomeSourcesConnector = customApp(
    dividendsEnabled = false,
    interestEnabled = false,
    giftAidEnabled = false,
    employmentEnabled = false,
    studentLoansEnabled = false,
    employmentEOYEnabled = false,
    cisEnabled = false,
    crystallisationEnabled = false
  ).injector.instanceOf[IncomeSourcesConnector]

  private val dividendResult = Some(DividendsModel(Some(500.0), Some(600.0)))
  private val interestResult = Some(Seq(InterestModel("account", "1234567890", Some(500.0), Some(500.0))))

  private val giftAidResult: Option[GiftAidModel] = Some(GiftAidModel(
    giftAidPaymentsModel,
    giftsModel
  ))

  val employment: Option[AllEmploymentData] = Some(AllEmploymentData(
    Seq(
      HmrcEmploymentSource(
        employmentId = "00000000-0000-0000-1111-000000000000",
        employerRef = Some("666/66666"),
        employerName = "Business",
        payrollId = Some("1234567890"),
        startDate = Some("2020-01-01"),
        cessationDate = Some("2020-01-01"),
        dateIgnored = None,
        submittedOn = None,
        hmrcEmploymentFinancialData = Some(
          EmploymentFinancialData(
            employmentData = Some(EmploymentData(
              "2020-01-04T05:01:01Z",
              employmentSequenceNumber = Some("1002"),
              companyDirector = Some(false),
              closeCompany = Some(true),
              directorshipCeasedDate = Some("2020-02-12"),
              disguisedRemuneration = Some(false),
              pay = Some(Pay(
                taxablePayToDate = Some(34234.15),
                totalTaxToDate = Some(6782.92),
                payFrequency = Some("CALENDAR MONTHLY"),
                paymentDate = Some("2020-04-23"),
                taxWeekNo = Some(32),
                taxMonthNo = Some(2)
              ))
            )),
            employmentBenefits = Some(
              EmploymentBenefits(
                "2020-01-04T05:01:01Z",
                benefits = Some(Benefits(
                  Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100),
                  Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100),
                  Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100)
                ))
              )
            )
          )
        ),
        customerEmploymentFinancialData = None
      )
    ),
    hmrcExpenses = Some(
      EmploymentExpenses(
        Some("2020-01-04T05:01:01Z"),
        totalExpenses = Some(800),
        expenses = Some(Expenses(
          Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100)
        ))
      )
    ),
    Seq(
      EmploymentSource(
        employmentId = "00000000-0000-0000-2222-000000000000",
        employerRef = Some("666/66666"),
        employerName = "Business",
        payrollId = Some("1234567890"),
        startDate = Some("2020-01-01"),
        cessationDate = Some("2020-01-01"),
        dateIgnored = None,
        submittedOn = Some("2020-01-01T10:00:38Z"),
        employmentData = Some(
          EmploymentData(
            "2020-01-04T05:01:01Z",
            employmentSequenceNumber = Some("1002"),
            companyDirector = Some(false),
            closeCompany = Some(true),
            directorshipCeasedDate = Some("2020-02-12"),
            disguisedRemuneration = Some(false),
            pay = Some(Pay(
              taxablePayToDate = Some(34234.15),
              totalTaxToDate = Some(6782.92),
              payFrequency = Some("CALENDAR MONTHLY"),
              paymentDate = Some("2020-04-23"),
              taxWeekNo = Some(32),
              taxMonthNo = Some(2)
            ))
          )
        ),
        employmentBenefits = Some(
          EmploymentBenefits(
            "2020-01-04T05:01:01Z",
            benefits = Some(Benefits(
              Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100),
              Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100),
              Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100)
            ))
          )
        )
      )
    ),
    customerExpenses = Some(
      EmploymentExpenses(
        Some("2020-01-04T05:01:01Z"),
        totalExpenses = Some(800),
        expenses = Some(Expenses(
          Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100)
        ))
      )
    )
  ))

  ".IncomeSourcesConnector" should {
    "return a IncomeSourcesModel" when {

      "all sources are turned off" in {
        val expectedResult = IncomeSourcesModel(dividendResult, interestResult)

        stubGetWithHeaderCheck(s"/income-tax-submission-service/income-tax/nino/$nino/sources\\?taxYear=$taxYearEOY", OK,
          Json.toJson(expectedResult).toString(), ("excluded-income-sources", "dividends,interest,gift-aid,employment,cis"))

        val result = await(connectorWithSourcesTurnedOff.getIncomeSources(nino, taxYearEOY))

        result shouldBe Right(expectedResult)
      }

      "all sources are turned on" in {
        val expectedResult = IncomeSourcesModel(dividendResult, interestResult)

        stubGet(s"/income-tax-submission-service/income-tax/nino/$nino/sources\\?taxYear=$taxYearEOY", OK, Json.toJson(expectedResult).toString())

        val result = await(connector.getIncomeSources(nino, taxYearEOY))

        result shouldBe Right(expectedResult)
      }

      "all optional values are present" in {
        val expectedResult = IncomeSourcesModel(dividendResult, interestResult, giftAidResult, employment)

        stubGet(s"/income-tax-submission-service/income-tax/nino/$nino/sources\\?taxYear=$taxYearEOY", OK, Json.toJson(expectedResult).toString())

        val result = await(connector.getIncomeSources(nino, taxYearEOY))

        result shouldBe Right(expectedResult)
      }
      "no optional values are present" in {
        val expectedResult = IncomeSourcesModel(None, None)

        stubGet(s"/income-tax-submission-service/income-tax/nino/$nino/sources\\?taxYear=$taxYearEOY", OK, Json.toJson(expectedResult).toString())

        val result = await(connector.getIncomeSources(nino, taxYearEOY))

        result shouldBe Right(expectedResult)
      }
    }
    "non json is returned" in {
      stubGet(s"/income-tax-submission-service/income-tax/nino/$nino/sources\\?taxYear=$taxYearEOY", INTERNAL_SERVER_ERROR, "")

      val result = await(connector.getIncomeSources(nino, taxYearEOY))

      result shouldBe Left(APIErrorModel(INTERNAL_SERVER_ERROR, APIErrorBodyModel.parsingError))
    }

    "API Returns multiple errors" in {
      val expectedResult = APIErrorModel(INTERNAL_SERVER_ERROR, APIErrorsBodyModel(Seq(
        APIErrorBodyModel("INVALID_IDTYPE", "ID is invalid"),
        APIErrorBodyModel("INVALID_IDTYPE_2", "ID 2 is invalid"))))

      val responseBody = Json.obj(
        "failures" -> Json.arr(
          Json.obj("code" -> "INVALID_IDTYPE",
            "reason" -> "ID is invalid"),
          Json.obj("code" -> "INVALID_IDTYPE_2",
            "reason" -> "ID 2 is invalid")
        )
      )
      stubGet(s"/income-tax-submission-service/income-tax/nino/$nino/sources\\?taxYear=$taxYearEOY", BAD_REQUEST, responseBody.toString())

      val result = await(connector.getIncomeSources(nino, taxYearEOY))

      result shouldBe Left(expectedResult)
    }

    "return a PARSING_ERROR" in {
      val invalidJson = Json.obj(
        "dividends" -> ""
      )

      val expectedResult = APIErrorModel(500, APIErrorBodyModel("PARSING_ERROR", "Error parsing response from API"))

      stubGet(s"/income-tax-submission-service/income-tax/nino/$nino/sources\\?taxYear=$taxYearEOY", OK, invalidJson.toString())
      val result = await(connector.getIncomeSources(nino, taxYearEOY))

      result shouldBe Left(expectedResult)
    }
    "return a SERVICE_UNAVAILABLE" in {
      val expectedResult = APIErrorModel(503, APIErrorBodyModel("SERVICE_UNAVAILABLE", "Service unavailable"))

      stubGet(s"/income-tax-submission-service/income-tax/nino/$nino/sources\\?taxYear=$taxYearEOY", SERVICE_UNAVAILABLE, expectedResult.toJson.toString())
      val result = await(connector.getIncomeSources(nino, taxYearEOY))

      result shouldBe Left(expectedResult)
    }
    "return a Right(IncomeSourcesModel())" in {
      val expectedResult = Right(IncomeSourcesModel())

      stubGet(s"/income-tax-submission-service/income-tax/nino/$nino/sources\\?taxYear=$taxYearEOY", NOT_FOUND, "{}")
      val result = await(connector.getIncomeSources(nino, taxYearEOY))

      result shouldBe expectedResult
    }
    "return a Right(IncomeSourcesModel()) from a 204" in {
      val expectedResult = Right(IncomeSourcesModel())

      stubGet(s"/income-tax-submission-service/income-tax/nino/$nino/sources\\?taxYear=$taxYearEOY", NO_CONTENT, "{}")
      val result = await(connector.getIncomeSources(nino, taxYearEOY))

      result shouldBe expectedResult
    }
    "return a INTERNAL_SERVER_ERROR" in {
      val expectedResult = APIErrorModel(500, APIErrorBodyModel("INTERNAL_SERVER_ERROR", "Internal server error"))

      stubGet(s"/income-tax-submission-service/income-tax/nino/$nino/sources\\?taxYear=$taxYearEOY", INTERNAL_SERVER_ERROR, expectedResult.toJson.toString())
      val result = await(connector.getIncomeSources(nino, taxYearEOY))

      result shouldBe Left(expectedResult)
    }
    "return a PARSING_ERROR when unexpected status 408" in {
      val expectedResult = APIErrorModel(500, APIErrorBodyModel("PARSING_ERROR", "Error parsing response from API"))

      stubGet(s"/income-tax-submission-service/income-tax/nino/$nino/sources\\?taxYear=$taxYearEOY", REQUEST_TIMEOUT, "")
      val result = await(connector.getIncomeSources(nino, taxYearEOY))

      result shouldBe Left(expectedResult)
    }

  }

}
