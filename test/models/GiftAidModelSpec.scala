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

package models

import play.api.libs.json.{JsObject, Json}
import utils.UnitTest

class GiftAidModelSpec extends UnitTest {

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

  "GiftsModel.hasNonZeroData" should {

    "return true" when {

      "landsAndBuildings has non zero data" in {
        GiftsModel(landAndBuildings = Some(2)).hasNonZeroData shouldBe true
      }

      "sharesOrSecurities has non zero data" in {
        GiftsModel(sharesOrSecurities = Some(2)).hasNonZeroData shouldBe true
      }

      "investmentsNonUkCharities has non zero data" in {
        GiftsModel(investmentsNonUkCharities = Some(2)).hasNonZeroData shouldBe true
      }

    }

    "return false" when {

      "all fields have no values" in {
        GiftsModel().hasNonZeroData shouldBe false
      }

      "all fields have zero values" in {
        GiftsModel(None, Some(0), Some(0), Some(0)).hasNonZeroData shouldBe false
      }

    }

  }

  "GiftAidPaymentsModel.hasNonZeroData" should {

    "return true" when {

      "current year is non zero" in {
        GiftAidPaymentsModel(currentYear = Some(1)).hasNonZeroData shouldBe true
      }

      "oneOffCurrentYear is non zero" in {
        GiftAidPaymentsModel(oneOffCurrentYear = Some(1)).hasNonZeroData shouldBe true
      }

      "currentYearTreatedAsPreviousYear is non zero" in {
        GiftAidPaymentsModel(currentYearTreatedAsPreviousYear = Some(1)).hasNonZeroData shouldBe true
      }

      "nextYearTreatedAsCurrentYear is non zero" in {
        GiftAidPaymentsModel(nextYearTreatedAsCurrentYear = Some(1)).hasNonZeroData shouldBe true
      }

      "nonUkCharities is non zero" in {
        GiftAidPaymentsModel(nonUkCharities = Some(1)).hasNonZeroData shouldBe true
      }

    }

    "return false" when {

      "there is no data in the model" in {
        GiftAidPaymentsModel().hasNonZeroData shouldBe false
      }

      "all the values in the model are set to 0" in {
        GiftAidPaymentsModel(None, Some(0), Some(0), Some(0), Some(0), Some(0))
      }

    }

  }

  "GiftAidModel.hasNonZeroData" should {

    "return true" when {

      "the GiftAidPaymentsModel has non zero data" in {
        GiftAidModel(Some(GiftAidPaymentsModel(currentYear = Some(1)))).hasNonZeroData shouldBe true
      }

      "the GiftsModel has non zero data" in {
        GiftAidModel(gifts = Some(GiftsModel(landAndBuildings = Some(2)))).hasNonZeroData shouldBe true
      }

    }

    "return false" when {

      "the model is empty" in {
        GiftAidModel().hasNonZeroData shouldBe false
      }

      "both models have only zero data" in {
        GiftAidModel(Some(GiftAidPaymentsModel()), Some(GiftsModel())).hasNonZeroData shouldBe false
      }

    }

  }
}
