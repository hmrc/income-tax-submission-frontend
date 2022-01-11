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

class AuthLoginAPIResponseSpec extends UnitTest {

  val validModel: AuthLoginAPIResponse = AuthLoginAPIResponse(
    token = "this-is-a-token",
    sessionId = "some-session-id",
    governmentGatewayToken = "gg-token"
  )
  
  val validJson: JsObject = Json.obj(
    "token" -> "this-is-a-token",
    "sessionId" -> "some-session-id",
    "governmentGatewayToken" -> "gg-token"
  )
  
  "AuthLoginAPIResponse" should {
    
    "correctly read from Json" in {
      validJson.as[AuthLoginAPIResponse] shouldBe validModel
    }
    
  }
  
}
