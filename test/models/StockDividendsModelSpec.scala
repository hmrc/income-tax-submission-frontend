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

import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.libs.json.{JsObject, Json}
import utils.UnitTest

class StockDividendsModelSpec extends UnitTest {

  val foreignInterest: Seq[ForeignInterestModel] = Seq(ForeignInterestModel(
    "BES",
    Some(1232.56),
    Some(3422.22),
    Some(5622.67),
    Some(true),
    2821.92)
  )

  val dividend: Dividend = Dividend(customerReference = Some("reference"), grossAmount = Some(123.45))

  val anStockDividends: StockDividendsModel = StockDividendsModel(
    submittedOn = Some(""),
    foreignDividend = Some(foreignInterest),
    dividendIncomeReceivedWhilstAbroad = Some(foreignInterest),
    stockDividend = Some(dividend),
    redeemableShares = Some(dividend),
    bonusIssuesOfSecurities = Some(dividend),
    closeCompanyLoansWrittenOff = Some(dividend)
  )

  val validJson: JsObject = Json.obj(
    "submittedOn" -> "",
    "foreignDividend" -> Json.toJson(foreignInterest),
    "dividendIncomeReceivedWhilstAbroad" -> Json.toJson(foreignInterest),
    "stockDividend" -> Json.toJson(anStockDividends.stockDividend),
    "redeemableShares" -> Json.toJson(anStockDividends.redeemableShares),
    "bonusIssuesOfSecurities" -> Json.toJson(anStockDividends.bonusIssuesOfSecurities),
    "closeCompanyLoansWrittenOff" -> Json.toJson(anStockDividends.closeCompanyLoansWrittenOff)
  )


  val validModel: StockDividendsModel = anStockDividends

  "StockDividendsResponseModel" should {

    "correctly parse from Json" in {
      validJson.as[StockDividendsModel] mustBe validModel
    }

    "correctly parse to Json" in {
      Json.toJson(validModel) mustBe validJson
    }

  }

}
