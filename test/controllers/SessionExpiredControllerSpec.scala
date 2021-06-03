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

package controllers

import play.api.http.Status.{NO_CONTENT, OK}
import play.api.test.Helpers.{charset, contentType, stubMessagesControllerComponents}
import play.api.test.{DefaultAwaitTimeout, FakeRequest}
import utils.UnitTest
import views.html.errors.TimeoutPage

class SessionExpiredControllerSpec extends UnitTest with DefaultAwaitTimeout {

  private val timeoutPage: TimeoutPage = app.injector.instanceOf[TimeoutPage]
  val controller = new SessionExpiredController(stubMessagesControllerComponents, mockAppConfig, timeoutPage)

  "SessionExpiredController" should {

    "redirect user to keep session" when {

      "keepAlive() is called it" should {

        val request = FakeRequest("GET", "/sign-out")

        val responseF = controller.keepAlive()(request)

        "return status code 303" in {
          status(responseF) shouldBe NO_CONTENT
        }
      }

      "timeout() is called it" should {

        val request = FakeRequest("GET", "/sign-out")

        val responseF = controller.timeout()(request)

        "return status code OK" in {
          status(responseF) shouldBe OK
        }

        "return HTML" in {
          contentType(responseF) shouldBe Some("text/html")
          charset(responseF) shouldBe Some("utf-8")
        }
      }
      "timeout() is called with a tax year key it" should {

        val request = FakeRequest("GET", "/sign-out")

        val responseF = controller.timeout()(request.withSession("TAX_YEAR" -> "2022"))

        "return status code OK" in {
          status(responseF) shouldBe OK
        }

        "return HTML" in {
          contentType(responseF) shouldBe Some("text/html")
          charset(responseF) shouldBe Some("utf-8")
        }
      }

    }
  }

}
