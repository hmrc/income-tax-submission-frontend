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

import utils.UnitTest
import play.api.libs.json.{JsObject, Json}

class AuthLoginRequestSpec extends UnitTest {

  val model: AuthLoginRequest = AuthLoginRequest(
    credId = "someId",
    affinityGroup = AG_Individual,
    confidenceLevel = L250,
    credentialStrength = CS_Strong,
    enrolments = Seq(Enrolment(
      key = "someKey", identifiers = Seq(Identifier(key = "ik", value = "iv")), state = Activated
    )),
    delegatedEnrolments = Seq(),
    nino = Some("AA123456A")
  )
  
  val json: JsObject = Json.obj(
    "credId" -> "someId",
    "affinityGroup" -> "Individual",
    "confidenceLevel" -> 250,
    "credentialStrength" -> "strong",
    "enrolments" -> Json.arr(Json.obj(
      "key" -> "someKey",
      "identifiers" -> Json.arr(Json.obj("key" -> "ik", "value" -> "iv")),
      "state" -> "Activated"
    )),
    "nino" -> "AA123456A"
  )
  
  "AuthLoginRequest" should {
    
    "correctly parse to json" in {
      Json.toJson(model) shouldBe json
    }
    
  }
  
}
