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

package filters

import com.google.inject.Inject
import itUtils.IntegrationTest
import org.scalatestplus.play.components.OneAppPerSuiteWithComponents
import play.api.http.DefaultHttpFilters
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Application, BuiltInComponents, BuiltInComponentsFromContext, NoHttpFiltersComponents}
import uk.gov.hmrc.http.{HeaderNames, SessionKeys}

class SessionIdFilterSpec extends IntegrationTest with OneAppPerSuiteWithComponents {

  override val sessionId = "28836767-a008-46be-ac18-695ab140e705"

  class Filters @Inject()(sessionId: SessionIdFilter) extends DefaultHttpFilters(sessionId)

  override def components: BuiltInComponents = new BuiltInComponentsFromContext(context) with NoHttpFiltersComponents {

    import play.api.mvc.Results
    import play.api.routing.Router
    import play.api.routing.sird._

    lazy val router: Router = Router.from {
      case GET(p"/test") => defaultActionBuilder.apply {
        request =>
          val fromHeader = request.headers.get(HeaderNames.xSessionId).getOrElse("")
          val fromSession = request.session.get(SessionKeys.sessionId).getOrElse("")
          Results.Ok(
            Json.obj(
              "fromHeader" -> fromHeader,
              "fromSession" -> fromSession
            )
          )
      }
      case GET(p"/test2") => defaultActionBuilder.apply {
        Results.Ok.addingToSession("foo" -> "bar")
      }
    }
  }

  override lazy val app: Application = {

    new GuiceApplicationBuilder()
      .configure(
        "play.filters.disabled" -> List(
          "uk.gov.hmrc.play.bootstrap.frontend.filters.crypto.SessionCookieCryptoFilter",
          "filters.AllowlistFilter"
        )
      )
      .router(components.router)
      .build()
  }

  "session id filter" must {

    "add a sessionId if one doesn't already exist" in {

      val result = route(app, FakeRequest(GET, "/test")).value

      val body = contentAsJson(result)

      (body \ "fromHeader").as[String].take(7) shouldBe "session"
      (body \ "fromHeader").as[String].length shouldBe 44

      session(result).data.get(SessionKeys.sessionId).map{
        s =>
          s.take(7) shouldBe "session"
          s.length shouldBe 44
      }
    }

    "not override a sessionId if one doesn't already exist" in {

      val result = route(app, FakeRequest(GET, "/test").withSession(SessionKeys.sessionId -> "foo")).value

      val body = contentAsJson(result)

      (body \ "fromHeader").as[String] shouldEqual ""
      (body \ "fromSession").as[String] shouldEqual "foo"
    }
  }
}