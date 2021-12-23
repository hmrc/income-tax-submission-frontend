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

package connectors.testOnly

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import itUtils.IntegrationTest
import models.userResearch._
import play.api.http.HeaderNames
import play.api.libs.json.Json

class AuthLoginApiConnectorISpec extends IntegrationTest {

  lazy val connector: AuthLoginApiConnector = app.injector.instanceOf[AuthLoginApiConnector]

  def stubAuthCall(status: Int = 201): StubMapping = stubPostWithHeaders(
    url = "/government-gateway/session/login",
    status = status,
    responseBody = Json.prettyPrint(Json.obj("gatewayToken" -> "gg-token")),
    headers = Seq(HeaderNames.AUTHORIZATION -> "some-token", HeaderNames.LOCATION -> "session-id")
  )

  def stubAuthCallWithoutHeaders(status: Int = 201): StubMapping = stubPost(
    url = "/government-gateway/session/login",
    status = status,
    responseBody = Json.prettyPrint(Json.obj("gatewayToken" -> "gg-token"))
  )

  ".submitLoginRequest" should {

    "return an API response" when {

      "the call is successful" in {
        val result: Option[AuthLoginAPIResponse] = {
          stubAuthCall()

          await(connector.submitLoginRequest(ResearchUser(
            2021,
            "AA123456A",
            CS_Strong,
            L250,
            AG_Individual,
            Seq(Enrolment(
              "someKey", Seq(Identifier("ik", "iv")), Activated
            )),
            Seq.empty
          )))
        }

        result shouldBe Some(AuthLoginAPIResponse("some-token", "session-id", "gg-token"))
      }

    }

    "return a None" when {

      "there is a missing header" in {
        val result: Option[AuthLoginAPIResponse] = {
          stubAuthCallWithoutHeaders()

          await(connector.submitLoginRequest(ResearchUser(
            2021,
            "AA123456A",
            CS_Strong,
            L250,
            AG_Individual,
            Seq(Enrolment(
              "someKey", Seq(Identifier("ik", "iv")), Activated
            )),
            Seq.empty
          )))
        }

        result shouldBe None
      }

      "the API call returns a status that isn't 201 (CREATED)" in {
        val result: Option[AuthLoginAPIResponse] = {
          stubAuthCall(status = 200)

          await(connector.submitLoginRequest(ResearchUser(
            2021,
            "AA123456A",
            CS_Strong,
            L250,
            AG_Individual,
            Seq(Enrolment(
              "someKey", Seq(Identifier("ik", "iv")), Activated
            )),
            Seq.empty
          )))
        }

        result shouldBe None
      }

    }

  }

}
