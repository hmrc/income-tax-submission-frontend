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

package models.userResearch

import play.api.libs.json.{JsObject, Json}
import utils.UnitTest

class IdentifierSpec extends UnitTest {

  val validModel: Identifier = Identifier("A Key", "A Value")
  val validJson: JsObject = Json.obj("key" -> "A Key", "value" -> "A Value")
  
  "Identifier" should {
    
    "correctly parse to Json" in {
      
      Json.toJson(validModel) shouldBe validJson
      
    }
    
  }
  
}
