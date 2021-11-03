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

class NrsSubmissionModelSpec extends UnitTest {

  val model: NrsSubmissionModel = NrsSubmissionModel("2dd537bc-4244-4ebf-bac9-96321be13cdc")

  val jsModel: JsObject = Json.obj("calculationId" -> "2dd537bc-4244-4ebf-bac9-96321be13cdc")

  "NrsSubmissionModel" should {

    "parse to json" in {

      Json.toJson(model) shouldBe jsModel
    }

    "parse from json" in {

      jsModel.as[NrsSubmissionModel]
    }

  }


}
