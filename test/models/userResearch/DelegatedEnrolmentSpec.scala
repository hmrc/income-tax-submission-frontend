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

package models.userResearch

import play.api.libs.json.{JsObject, Json}
import utils.UnitTest

class DelegatedEnrolmentSpec extends UnitTest {

  val validJson: JsObject = Json.obj(
    "key" -> "someKey",
    "identifiers" -> Json.arr(
      Json.obj("key" -> "identifierKey", "value" -> "1234567890"),
      Json.obj("key" -> "someOtherKey", "value" -> "0987654321")
    ),
    "delegatedAuthRule" -> "mtd-auth"
  )
  
  val validModel: DelegatedEnrolment = DelegatedEnrolment(
    key = "someKey",
    identifiers = Seq(
      Identifier(key = "identifierKey", value = "1234567890"),
      Identifier(key = "someOtherKey", value = "0987654321")
    ),
    delegatedAuthRule = "mtd-auth"
  )
  
  "Enrolment" should {
    
    "parse to json correctly" in {
      Json.toJson(validModel) shouldBe validJson
    }
    
    ".toJson" should {
      
      "produce the correct json" in {
        validModel.toJson shouldBe validJson
      }
      
    }
    
  }
  
}
