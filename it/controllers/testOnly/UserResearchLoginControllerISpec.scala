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

package controllers.testOnly

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import helpers.PlaySessionCookieBaker
import itUtils.{IntegrationTest, ViewHelpers}
import models.userResearch.AuthLoginAPIResponse
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.http.HeaderNames
import play.api.http.Status.{NO_CONTENT, OK, SEE_OTHER}
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HttpVerbs.GET

import scala.concurrent.Future

class UserResearchLoginControllerISpec extends IntegrationTest with ViewHelpers {

  lazy val controller: UserResearchLoginController = app.injector.instanceOf[UserResearchLoginController]

  def stubAuthCall(status: Int = 201): StubMapping = stubPostWithHeaders(
    url = "/government-gateway/session/login",
    status = status,
    responseBody = Json.prettyPrint(Json.obj("gatewayToken" -> "gg-token")),
    headers = Seq(HeaderNames.AUTHORIZATION -> "some-token", HeaderNames.LOCATION -> "session-id")
  )

  lazy val url: String = s"http://localhost:$port" + controllers.testOnly.routes.UserResearchLoginController.submit().url

  object Selectors {
    val inputField = "#credential"
    val inputLabel = ".govuk-label"
  }

  ".show" should {

    "render the page" which {
      import Selectors._

      lazy val call = wsClient.url(url).withHttpHeaders(HeaderNames.COOKIE -> PlaySessionCookieBaker.bakeSessionCookie(Map("Start" -> "Value"))).get()
      lazy val result = await(call)

      implicit val document: () => Document = () => Jsoup.parse(result.body)

      "has a status of OK (200)" in {
        result.status shouldBe OK
      }

      titleCheck("Sign in to the test service", isWelsh = false)
      textOnPageCheck("User ID", inputLabel)
      inputFieldCheck("credential", inputField)
      buttonCheck("Continue")
    }

  }

  ".submit" should {

    "redirect to the start page" when {

      "the auth api call is successful" which {
        lazy val call: Future[WSResponse] = wsClient.url(url).withFollowRedirects(false).post(Json.obj("credential" -> "70365"))

        lazy val result: WSResponse = {
          stubAuthCall()
          await(call)
        }

        "has a status of SEE_OTHER (303)" in {
          result.status shouldBe SEE_OTHER
        }

        "has a redirect URL to the start page" in {
          result.headers("Location").head shouldBe controllers.routes.StartPageController.show(2022).url
        }
      }

    }

    "redirect to the start page with a given year" when {

      "the auth api call is successful and the credential contains a year modifier" which {
        lazy val call: Future[WSResponse] = wsClient.url(url).withFollowRedirects(false).post(Json.obj("credential" -> "70365::2040"))

        lazy val result: WSResponse = {
          stubAuthCall()
          await(call)
        }

        "has a status of SEE_OTHER (303)" in {
          result.status shouldBe SEE_OTHER
        }

        "has a redirect URL to the start page" in {
          result.headers("Location").head shouldBe controllers.routes.StartPageController.show(2040).url
        }
      }

    }

    "redirect to the login page" when {

      "the auth call returns a None" which {
        lazy val call: Future[WSResponse] = wsClient.url(url).withFollowRedirects(false).post(Json.obj("credential" -> "70365"))

        lazy val result: WSResponse = {
          stubAuthCall(NO_CONTENT)
          await(call)
        }

        "has a status of SEE_OTHER (303)" in {
          result.status shouldBe SEE_OTHER
        }

        "has a redirect URL to the start page" in {
          result.headers("Location").head shouldBe controllers.testOnly.routes.UserResearchLoginController.show().url
        }
      }

    }

  }

  ".buildGGSession" should {

    "add a session id, auth token and last request timestamp to session" in {
      val resultingData: Map[String, String] = controller.buildGGSession(AuthLoginAPIResponse(
        token = "some-token", sessionId = "session-id", governmentGatewayToken = "gg-token"
      )).data

      resultingData("sessionId") shouldBe "session-id"
      resultingData("authToken") shouldBe "some-token"
      resultingData("ts").nonEmpty shouldBe true
    }

    "add a session id, auth token and last request timestamp to session and populate the session id if it is empty" in {
      val resultingData = controller.buildGGSession(AuthLoginAPIResponse(
        token = "some-token", sessionId = "", governmentGatewayToken = "gg-token"
      )).data

      resultingData("sessionId").nonEmpty shouldBe true
      resultingData("authToken") shouldBe "some-token"
      resultingData("ts").nonEmpty shouldBe true
    }

  }

}
