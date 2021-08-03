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

package models

import org.joda.time.{DateTime, DateTimeZone}
import org.joda.time.format.DateTimeFormat
import play.api.libs.json.{JsObject, Json}
import utils.UnitTest

class TaxReturnReceivedModelSpec extends UnitTest {

  val model: TaxReturnReceivedModel = TaxReturnReceivedModel(
    "Jeffery Jones",
    500,
    "SA123456",
    500,
    500,
    500
  )

  val jsonModel: JsObject = Json.obj(
    "name" -> "Jeffery Jones",
    "incomeTaxAndNationalInsuranceContributions" -> 500,
    "saUTR" -> "SA123456",
    "income" -> 500,
    "allowancesAndDeductions" -> 500,
    "totalTaxableIncome" -> 500
  )

  "TaxReturnReceivedModel" should {
    "parse to Json" in {
      Json.toJson(model) shouldBe jsonModel

    }
    "parse from Json" in {
      jsonModel.as[TaxReturnReceivedModel]
    }
  }
}
