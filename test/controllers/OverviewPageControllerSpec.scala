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

import common.SessionValues.DIVIDENDS_PRIOR_SUB
import config.FrontendAppConfig
import connectors.httpparsers.IncomeSourcesHttpParser.{IncomeSourcesNotFoundException, IncomeSourcesResponse}
import models.{DividendsModel, IncomeSourcesModel}
import org.scalamock.handlers.CallHandler3
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Configuration, Environment}
import services.IncomeSourcesService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils.UnitTest
import views.html.OverviewPageView

import scala.concurrent.Future

class OverviewPageControllerSpec extends UnitTest with GuiceOneAppPerSuite {

  private val fakeGetRequest = FakeRequest("GET", "/").withSession("MTDITID" -> "12234567890")
  private val env = Environment.simple()
  private val configuration = Configuration.load(env)

  private val serviceConfig = new ServicesConfig(configuration)
  private val mockFrontendAppConfig = new FrontendAppConfig(configuration, serviceConfig)
  private val overviewPageView: OverviewPageView = app.injector.instanceOf[OverviewPageView]
  private val mockIncomeSourcesService = mock[IncomeSourcesService]

  def mockGetIncomeSourcesValid(): CallHandler3[String, Int, HeaderCarrier, Future[IncomeSourcesResponse]] = {
    val validIncomeSource: IncomeSourcesResponse = Right(IncomeSourcesModel(Some(DividendsModel(None,None))))
    (mockIncomeSourcesService.getIncomeSources(_: String, _: Int)(_: HeaderCarrier))
      .expects(*, *, *)
      .returning(Future.successful(validIncomeSource))
  }
  def mockGetIncomeSourcesNone(): CallHandler3[String, Int, HeaderCarrier, Future[IncomeSourcesResponse]] = {
    val invalidIncomeSource: IncomeSourcesResponse = Left(IncomeSourcesNotFoundException)
    (mockIncomeSourcesService.getIncomeSources(_: String, _: Int)(_: HeaderCarrier))
      .expects(*, *, *)
      .returning(Future.successful(invalidIncomeSource))
  }


  private val controller = new OverviewPageController(
    mockFrontendAppConfig, stubMessagesControllerComponents(),mockExecutionContext, mockIncomeSourcesService, overviewPageView, authorisedAction
  )

  "calling the individual action" when {

    "the user is an individual with existing income sources" should {

      "GET '/' for an individual and return 200" in {

        val result = {
          mockAuth()
          mockGetIncomeSourcesValid()
          controller.show(2020)(fakeGetRequest)
        }
        status(result) shouldBe Status.OK
      }

      "return HTML" in {
        val result = {
          mockAuth()
          mockGetIncomeSourcesValid()
          controller.show(2020)(fakeGetRequest)
        }
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }

      "Set the session value for dividends prior sub" in {
        val result = {
          mockAuth()
          mockGetIncomeSourcesValid()
          controller.show(2020)(fakeGetRequest)
        }
        session(result).get(DIVIDENDS_PRIOR_SUB) shouldBe Some(Json.toJson((DividendsModel(None,None))).toString())
      }
    }
    "the user is an individual without existing income sources" should {

      "GET '/' for an individual and return 200" in {

        val result = {
          mockAuth()
          mockGetIncomeSourcesNone()
          controller.show(2020)(fakeGetRequest)
        }
        status(result) shouldBe Status.OK
      }

      "return HTML" in {
        val result = {
          mockAuth()
          mockGetIncomeSourcesNone()
          controller.show(2020)(fakeGetRequest)
        }
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }
  }

  "calling the agent action" when {

    "the user is an agent with existing income sources" should {

      "GET '/' for an agent and return 200" in {

        val result = {
          mockAuthAsAgent()
          mockGetIncomeSourcesValid()
          controller.show(2020)(fakeGetRequest)
        }
        status(result) shouldBe Status.OK
      }

      "return HTML" in {
        val result = {
          mockAuthAsAgent()
          mockGetIncomeSourcesValid()
          controller.show(2020)(fakeGetRequest)
        }
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }
    "the user is an agent without existing income sources" should {

      "GET '/' for an agent and return 200" in {

        val result = {
          mockAuthAsAgent()
          mockGetIncomeSourcesNone()
          controller.show(2020)(fakeGetRequest)
        }
        status(result) shouldBe Status.OK
      }

      "return HTML" in {
        val result = {
          mockAuthAsAgent()
          mockGetIncomeSourcesNone()
          controller.show(2020)(fakeGetRequest)
        }
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }
  }

}
