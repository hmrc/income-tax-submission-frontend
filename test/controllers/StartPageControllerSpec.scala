/*
 * Copyright 2020 HM Revenue & Customs
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

import config.AppConfig
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.{Configuration, Environment}
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import views.html.StartPage

class StartPageControllerSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  private val fakeRequest = FakeRequest("GET", "/")
  private val env = Environment.simple()
  private val configuration = Configuration.load(env)

  private val serviceConfig = new ServicesConfig(configuration)
  private val mockAppConfig = new AppConfig(configuration, serviceConfig)
  private val startPageView: StartPage = app.injector.instanceOf[StartPage]

  private val controller = new StartPageController(mockAppConfig, stubMessagesControllerComponents(), startPageView)

  "calling the individual action" when {

    "the user is an individual" should {

      "GET '/' for an individual and return 200" in {

        val result = controller.individual(fakeRequest)
        status(result) shouldBe Status.OK
      }

      "return HTML" in {
        val result = controller.individual(fakeRequest)
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }
  }

  "calling the agent action" when {

    "the user is an agent" should {

      "GET '/' for an agent and return 200" in {

        val result = controller.agent(fakeRequest)
        status(result) shouldBe Status.OK
      }

      "return HTML" in {
        val result = controller.agent(fakeRequest)
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }
  }

}
