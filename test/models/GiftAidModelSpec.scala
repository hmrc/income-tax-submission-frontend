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

class GiftAidModelSpec extends UnitTest {

  val giftAidPaymentsModel: Option[GiftAidPaymentsModel] = Some(GiftAidPaymentsModel(
    nonUkCharitiesCharityNames = Some(List("non uk charity name", "non uk charity name 2")),
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

  val giftAidModel: GiftAidModel = GiftAidModel(
    giftAidPaymentsModel,
    giftsModel
  )

  val validJsonModel: JsObject = Json.obj(
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

  "GiftAidModel" should {

    "parse from Json" in {

      validJsonModel.as[GiftAidModel]
    }

    "parse to Json" in {

      Json.toJson(giftAidModel) shouldBe validJsonModel
    }
  }
}
