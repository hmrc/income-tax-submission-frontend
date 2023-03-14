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

package models

import models.employment.{AllEmploymentData, EmploymentData, EmploymentFinancialData, HmrcEmploymentSource, Pay}
import play.api.libs.json.{JsObject, Json}
import utils.UnitTest

class IncomeSourcesModelSpec extends UnitTest {


  val dividendModel: DividendsModel = DividendsModel(Some(11111), Some(11111))
  val jsonDividendsModel: JsObject = Json.obj(
    "ukDividends" -> 11111,
    "otherUkDividends" -> 11111
  )

  val interestModel: Seq[InterestModel] = Seq(InterestModel("account", "1234567890", Some(500), Some(500)))
  val jsonInterestModel: Seq[JsObject] = Seq(Json.obj(
    "accountName" -> "account",
    "incomeSourceId" -> "1234567890",
    "taxedUkInterest" -> 500,
    "untaxedUkInterest" -> 500,
  ))

  val jsonGiftAidModel: JsObject = Json.obj(
    "giftAidPayments" -> Json.obj(
      "nonUkCharitiesCharityNames" -> Json.arr(
        "non uk charity name",
        "non uk charity name 2"
      ),
      "currentYear" -> 1234.56,
      "oneOffCurrentYear" -> 1234.56,
      "currentYearTreatedAsPreviousYear" -> 1234.56,
      "nextYearTreatedAsCurrentYear" -> 1234.56,
      "nonUkCharities" -> 1234.56
    ),
    "gifts" -> Json.obj(
      "investmentsNonUkCharitiesCharityNames" -> Json.arr(
        "charity 1",
        "charity 2"
      ),
      "landAndBuildings" -> 10.21,
      "sharesOrSecurities" -> 10.21,
      "investmentsNonUkCharities" -> 1234.56
    ))

  val model: IncomeSourcesModel = IncomeSourcesModel(None, Some(dividendModel), Some(interestModel), Some(giftAidModel))
  val jsonModel: JsObject = Json.obj(
    "dividends" -> jsonDividendsModel,
    "interest" -> jsonInterestModel,
    "giftAid" -> jsonGiftAidModel
  )

  "IncomeSourcesModel" should {

    "parse to Json" in {
      Json.toJson(model) shouldBe jsonModel
    }

    "parse from Json" in {
      jsonModel.as[IncomeSourcesModel]
    }

    "excludeNotRelevantEmploymentData" when {

      def model(ignored: Boolean): IncomeSourcesModel = IncomeSourcesModel(None, None,None,None,Some(
        AllEmploymentData(
          hmrcEmploymentData = Seq(
            HmrcEmploymentSource(
              employmentId = "001",
              employerName = "maggie",
              employerRef = Some("223/AB12399"),
              payrollId = Some("123456789999"),
              startDate = Some("2019-04-21"),
              cessationDate = Some("2020-03-11"),
              dateIgnored = if(ignored) Some("2020-01-04T05:01:01Z") else None,
              submittedOn = Some("2020-01-04T05:01:01Z"),
              hmrcEmploymentFinancialData = Some(
                EmploymentFinancialData(
                  employmentData = Some(EmploymentData(
                    submittedOn = ("2020-02-12"),
                    employmentSequenceNumber = Some("123456789999"),
                    companyDirector = Some(true),
                    closeCompany = Some(false),
                    directorshipCeasedDate = Some("2020-02-12"),
                    disguisedRemuneration = Some(false),
                    pay = Some(Pay(Some(34234.15), Some(6782.92), Some("CALENDAR MONTHLY"), Some("2020-04-23"), Some(32), Some(2)))
                  )),
                  None
                )
              ),
              customerEmploymentFinancialData = None
            )
          ),
          hmrcExpenses = None,
          customerEmploymentData = Seq(),
          customerExpenses = None
        )
      ))

      "there is no data to exclude" in {
        model(ignored = false).excludeNotRelevantEmploymentData shouldBe model(ignored = false)
      }
      "there is ignored data to exclude" in {
        model(ignored = true).excludeNotRelevantEmploymentData shouldBe IncomeSourcesModel(None,None,None,None)
      }
    }
  }
}
