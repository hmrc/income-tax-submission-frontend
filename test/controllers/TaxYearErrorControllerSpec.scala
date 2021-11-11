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

import config.{AppConfig, MockAppConfig}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Configuration, Environment}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils.UnitTest
import views.html.errors.WrongTaxYearPage

class TaxYearErrorControllerSpec extends UnitTest with GuiceOneAppPerSuite {

  private val fakeGetRequest = FakeRequest("GET", "/")
    .withSession("ClientMTDID" -> "1234567890", "ClientNino" -> "AA123456A")
    .withHeaders("X-Session-ID" -> sessionId)
  private val env = Environment.simple()
  private val configuration = Configuration.load(env)

  private val serviceConfig = new ServicesConfig(configuration)
  private val mockFrontendAppConfig = new MockAppConfig
  private val wrongTaxYearPageView: WrongTaxYearPage = app.injector.instanceOf[WrongTaxYearPage]

  private val controller = new TaxYearErrorController(authorisedAction, stubMessagesControllerComponents(), wrongTaxYearPageView, mockFrontendAppConfig)

  private val nino: Option[String] = Some("AA123456A")

  "calling the individual action" when {

    "the user is an individual" should {

      "GET '/' for an individual and return 200" in {

        val result = {
          mockAuth(nino)
          controller.show()(fakeGetRequest)
        }
        status(result) shouldBe Status.OK
      }

      "return HTML" in {
        val result = {
          mockAuth(nino)
          controller.show()(fakeGetRequest)
        }
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }
  }

  "calling the agent action" when {

    "the user is an agent" should {

      "GET '/' for an agent and return 200" in {

        val result = {
          mockAuthAsAgent()
          controller.show()(fakeGetRequest)
        }
        status(result) shouldBe Status.OK
      }

      "return HTML" in {
        val result = {
          mockAuthAsAgent()
          controller.show()(fakeGetRequest)
        }
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }
  }
}
