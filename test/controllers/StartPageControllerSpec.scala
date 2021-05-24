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

import audit.EnterUpdateAndSubmissionServiceAuditDetail
import common.SessionValues
import config.{AppConfig, MockAuditService}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Configuration, Environment}
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.Retrieval
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils.UnitTest
import views.html.StartPage

import scala.concurrent.{ExecutionContext, Future}

class StartPageControllerSpec extends UnitTest with MockAuditService with GuiceOneAppPerSuite {

  private val fakeGetRequest = FakeRequest("GET", "/").withSession("ClientMTDID" -> "1234567890", "ClientNino" -> "AA123456A")
  private val env = Environment.simple()
  private val configuration = Configuration.load(env)

  private val serviceConfig = new ServicesConfig(configuration)
  private val mockFrontendAppConfig = new AppConfig(serviceConfig)
  private val startPageView: StartPage = app.injector.instanceOf[StartPage]

  private val controller = new StartPageController(authorisedAction,
    mockAuthService,
    startPageView,
    mockAuditService,
    mockFrontendAppConfig,
    stubMessagesControllerComponents(),
    mockExecutionContext
  )

  private val nino: Option[String] = Some("AA123456A")
  private val taxYear = 2022

  "calling the individual action" when {

    "the user is an individual" should {

      "GET '/' for an individual and return 200" in {

        val result = {
          mockAuth(nino)
          controller.show(taxYear)(fakeGetRequest)
        }
        status(result) shouldBe Status.OK
      }

      "return HTML" in {
        val result = {
          mockAuth(nino)
          controller.show(taxYear)(fakeGetRequest)
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
          controller.show(taxYear)(fakeGetRequest)
        }
        status(result) shouldBe Status.OK
      }

      "return HTML" in {
        val result = {
          mockAuthAsAgent()
          controller.show(taxYear)(fakeGetRequest)
        }
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }
  }

  ".submit" should {

    "redirect to the overview page" when {

      "the user is an individual" which {
        lazy val result = {
          mockAuth(Some("AA123456A"))
          (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
            .expects(*, *, *, *)
            .returns(Future.successful(Some(AffinityGroup.Individual)))

          verifyAuditEvent[EnterUpdateAndSubmissionServiceAuditDetail]

          controller.submit(taxYear)(fakeRequest.withSession(SessionValues.TAX_YEAR -> "2022"))
        }

        "has a result of SEE_OTHER(303)" in {
          status(result) shouldBe SEE_OTHER
        }

        "has overview page as the redirect url" in {
          redirectUrl(result) shouldBe controllers.routes.OverviewPageController.show(taxYear).url
        }
      }

    }

  }

}
