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

  val model: IncomeSourcesModel = IncomeSourcesModel(Some(dividendModel), Some(interestModel))
  val jsonModel: JsObject = Json.obj(
    "dividends" -> jsonDividendsModel,
    "interest" -> jsonInterestModel
  )

  "IncomeSourcesModel" should {

    "parse to Json" in {
      Json.toJson(model) shouldBe jsonModel
    }

    "parse from Json" in {
      jsonModel.as[IncomeSourcesModel]
    }
  }

}
