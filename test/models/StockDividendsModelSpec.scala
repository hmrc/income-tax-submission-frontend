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

  val aStockDividends: StockDividendsModel = StockDividendsModel(
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
    "stockDividend" -> Json.toJson(aStockDividends.stockDividend),
    "redeemableShares" -> Json.toJson(aStockDividends.redeemableShares),
    "bonusIssuesOfSecurities" -> Json.toJson(aStockDividends.bonusIssuesOfSecurities),
    "closeCompanyLoansWrittenOff" -> Json.toJson(aStockDividends.closeCompanyLoansWrittenOff)
  )


  val validModel: StockDividendsModel = aStockDividends

  "StockDividendsResponseModel" should {

    "correctly parse from Json" in {
      validJson.as[StockDividendsModel] mustBe validModel
    }

    "correctly parse to Json" in {
      Json.toJson(validModel) mustBe validJson
    }

  }

  "StockDividends.hasNonZeroData" should {

    "return true" when {

      "foreignDividend has non zero data" in {
        aStockDividends.foreignDividend.exists(_.exists(_.hasNonZeroData)) shouldBe true
      }

      "dividendIncomeReceivedWhilstAbroad has non zero data" in {
        aStockDividends.dividendIncomeReceivedWhilstAbroad.exists(_.exists(_.hasNonZeroData)) shouldBe true
      }

      "stockDividend has non zero data" in {
        aStockDividends.stockDividend.exists(_.hasNonZeroData) shouldBe true
      }

      "redeemableShares has non zero data" in {
        aStockDividends.redeemableShares.exists(_.hasNonZeroData) shouldBe true
      }

      "bonusIssuesOfSecurities has non zero data" in {
        aStockDividends.bonusIssuesOfSecurities.exists(_.hasNonZeroData) shouldBe true
      }

      "closeCompanyLoansWrittenOff has non zero data" in {
        aStockDividends.closeCompanyLoansWrittenOff.exists(_.hasNonZeroData) shouldBe true
      }

    }

    "return false" when {

      "all fields have no values" in {
        StockDividendsModel().hasNonZeroData shouldBe false

        aStockDividends.copy(
          foreignDividend = None,
          dividendIncomeReceivedWhilstAbroad = None,
          stockDividend = None,
          redeemableShares = None,
          bonusIssuesOfSecurities = None,
          closeCompanyLoansWrittenOff = None
        ).hasNonZeroData shouldBe false
      }

      "all fields have zero values" in {

        aStockDividends.copy(
          foreignDividend = Some(Seq(ForeignInterestModel("GB", Some(0), Some(0), Some(0), None, 0))),
          dividendIncomeReceivedWhilstAbroad = Some(Seq(ForeignInterestModel("GB", Some(0), Some(0), Some(0), None, 0))),
          stockDividend = Some(Dividend(grossAmount = Some(0))),
          redeemableShares = Some(Dividend(grossAmount = Some(0))),
          bonusIssuesOfSecurities = Some(Dividend(grossAmount = Some(0))),
          closeCompanyLoansWrittenOff = Some(Dividend(grossAmount = Some(0)))
        ).hasNonZeroData shouldBe false

      }

    }

    "foreignDividend.hasNonZeroData" should {

      "return true" when {

        "amountBeforeTax has non zero data" in {
          foreignInterest.exists(_.hasNonZeroData) shouldBe true
        }

        "taxTakenOff has non zero data" in {
          foreignInterest.exists(_.copy("GB", None, Some(2), None, None, 0).hasNonZeroData) shouldBe true
        }

        "specialWithholdingTax has non zero data" in {
          foreignInterest.exists(_.copy("GB", None, None, Some(2), None, 0).hasNonZeroData) shouldBe true
        }

        "taxableAmount has non zero data" in {
          foreignInterest.exists(_.copy("GB", None, None, None, None, 2).hasNonZeroData) shouldBe true
        }

      }

      "return false" when {

        "all fields except countryCode and taxableAmount have no values" in {
          foreignInterest.exists(_.copy("GB", None, None, None, None, 0).hasNonZeroData) shouldBe false
        }

        "all fields except countryCode have zero values" in {
          foreignInterest.exists(_.copy("GB", Some(0), Some(0), Some(0), None, 0).hasNonZeroData) shouldBe false
        }

      }

    }

  }

  "dividendIncomeReceivedWhilstAbroad.hasNonZeroData" should {

    "return true" when {

      "amountBeforeTax has non zero data" in {
        foreignInterest.exists(_.hasNonZeroData) shouldBe true
      }

      "taxTakenOff has non zero data" in {
        foreignInterest.exists(_.copy("GB", None, Some(2), None, None, 0).hasNonZeroData) shouldBe true
      }

      "specialWithholdingTax has non zero data" in {
        foreignInterest.exists(_.copy("GB", None, None, Some(2), None, 0).hasNonZeroData) shouldBe true
      }

      "taxableAmount has non zero data" in {
        foreignInterest.exists(_.copy("GB", None, None, None, None, 2).hasNonZeroData) shouldBe true
      }

    }

    "return false" when {

      "all fields except countryCode and taxableAmount have no values" in {
        foreignInterest.exists(_.copy("GB", None, None, None, None, 0).hasNonZeroData) shouldBe false
      }

      "all fields except countryCode have zero values" in {
        foreignInterest.exists(_.copy("GB", Some(0), Some(0), Some(0), None, 0).hasNonZeroData) shouldBe false
      }

    }

  }

  "stockDividend.hasNonZeroData" should {

    "return true" when {

      "stockDividend has non zero data" in {
        dividend.hasNonZeroData shouldBe true
      }

    }

    "return false" when {

      "all fields have no values" in {
        dividend.copy(customerReference = None, grossAmount = None).hasNonZeroData shouldBe false
      }

      "all fields have zero values" in {
        dividend.copy(customerReference = Some(""), grossAmount = Some(0)).hasNonZeroData shouldBe false
      }

    }

  }

  "redeemableShares.hasNonZeroData" should {

    "return true" when {

      "redeemableShares has non zero data" in {
        dividend.hasNonZeroData shouldBe true
      }

    }

    "return false" when {

      "all fields have no values" in {
        dividend.copy(customerReference = None, grossAmount = None).hasNonZeroData shouldBe false
      }

      "all fields have zero values" in {
        dividend.copy(customerReference = Some(""), grossAmount = Some(0)).hasNonZeroData shouldBe false
      }

    }

  }

  "bonusIssuesOfSecurities.hasNonZeroData" should {

    "return true" when {

      "bonusIssuesOfSecurities has non zero data" in {
        dividend.hasNonZeroData shouldBe true
      }

    }

    "return false" when {

      "all fields have no values" in {
        dividend.copy(customerReference = None, grossAmount = None).hasNonZeroData shouldBe false
      }

      "all fields have zero values" in {
        dividend.copy(customerReference = Some(""), grossAmount = Some(0)).hasNonZeroData shouldBe false
      }

    }

  }

  "closeCompanyLoansWrittenOff.hasNonZeroData" should {

    "return true" when {

      "closeCompanyLoansWrittenOff has non zero data" in {
        dividend.hasNonZeroData shouldBe true
      }

    }

    "return false" when {

      "all fields have no values" in {
        dividend.copy(customerReference = None, grossAmount = None).hasNonZeroData shouldBe false
      }

      "all fields have zero values" in {
        dividend.copy(customerReference = Some(""), grossAmount = Some(0)).hasNonZeroData shouldBe false
      }

    }

  }

}
