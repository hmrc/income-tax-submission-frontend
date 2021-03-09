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


import common.SessionValues._
import config.{AppConfig, ErrorHandler}
import connectors.httpparsers.CalculationIdHttpParser.{CalculationIdErrorInternalServerError, CalculationIdErrorServiceUnavailableError, CalculationIdResponse}
import connectors.httpparsers.IncomeSourcesHttpParser.{IncomeSourcesError, IncomeSourcesInternalServerError, IncomeSourcesResponse}
import models.{LiabilityCalculationIdModel, DividendsModel, IncomeSourcesModel, InterestModel}
import org.scalamock.handlers.{CallHandler2, CallHandler4}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.i18n.{Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.Results._
import play.api.mvc.{Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Configuration, Environment}
import services.{CalculationIdService, IncomeSourcesService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils.UnitTest
import views.html.OverviewPageView
import views.html.errors.{InternalServerErrorPage, ServiceUnavailablePage}

import scala.concurrent.Future

class OverviewPageControllerSpec extends UnitTest with GuiceOneAppPerSuite {

  private val fakeGetRequest = FakeRequest("GET", "/").withSession("ClientMTDID" -> "12234567890", "ClientNino" -> "AA123456A")
  private val env = Environment.simple()
  private val configuration = Configuration.load(env)

  private val serviceConfig = new ServicesConfig(configuration)
  private implicit val frontendAppConfig: AppConfig = new AppConfig(serviceConfig)
  implicit lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit lazy val messages: Messages = messagesApi.preferred(FakeRequest())
  private val overviewPageView: OverviewPageView = app.injector.instanceOf[OverviewPageView]
  private val serviceUnavailablePageView: ServiceUnavailablePage = app.injector.instanceOf[ServiceUnavailablePage]
  private val internalServerErrorPageView: InternalServerErrorPage = app.injector.instanceOf[InternalServerErrorPage]
  private val mockIncomeSourcesService = mock[IncomeSourcesService]
  private val mockErrorHandler = mock[ErrorHandler]
  private val mockCalculationIdService = mock[CalculationIdService]

  private val nino = Some("AA123456A")
  private val taxYear = 2020

  def mockGetIncomeSourcesValid(): CallHandler4[String, Int, String, HeaderCarrier, Future[IncomeSourcesResponse]] = {
    val validIncomeSource: IncomeSourcesResponse = Right(IncomeSourcesModel(
      Some(DividendsModel(None,None)),
      Some(Seq(InterestModel("", "", None, Some(500.00))))
    ))
    (mockIncomeSourcesService.getIncomeSources(_: String, _: Int, _: String)(_: HeaderCarrier))
      .expects(*, *, *, *)
      .returning(Future.successful(validIncomeSource))
  }

  def mockGetIncomeSourcesDividends(): CallHandler4[String, Int, String, HeaderCarrier, Future[IncomeSourcesResponse]] = {
    val validIncomeSource: IncomeSourcesResponse = Right(IncomeSourcesModel(
      Some(DividendsModel(None,None)),
      None
    ))
    (mockIncomeSourcesService.getIncomeSources(_: String, _: Int, _: String)(_: HeaderCarrier))
      .expects(*, *, *, *)
      .returning(Future.successful(validIncomeSource))
  }

  def mockGetIncomeSourcesInterest(): CallHandler4[String, Int, String, HeaderCarrier, Future[IncomeSourcesResponse]] = {
    val validIncomeSource: IncomeSourcesResponse = Right(IncomeSourcesModel(
      None,
      Some(Seq(InterestModel("", "", None, Some(500.00))))
    ))
    (mockIncomeSourcesService.getIncomeSources(_: String, _: Int, _: String)(_: HeaderCarrier))
      .expects(*, *, *, *)
      .returning(Future.successful(validIncomeSource))
  }

  def mockGetIncomeSourcesNone(): CallHandler4[String, Int, String, HeaderCarrier, Future[IncomeSourcesResponse]] = {
    val invalidIncomeSource: IncomeSourcesResponse = Right(IncomeSourcesModel())
    (mockIncomeSourcesService.getIncomeSources(_: String, _: Int, _: String)(_: HeaderCarrier))
      .expects(*, *, *, *)
      .returning(Future.successful(invalidIncomeSource))
  }

  def mockGetIncomeSourcesError(): CallHandler4[String, Int, String, HeaderCarrier, Future[IncomeSourcesResponse]] = {
    val invalidIncomeSource: IncomeSourcesResponse = Left(IncomeSourcesInternalServerError)
    (mockIncomeSourcesService.getIncomeSources(_: String, _: Int, _: String)(_: HeaderCarrier))
      .expects(*, *, *, *)
      .returning(Future.successful(invalidIncomeSource))
  }

  def mockHandleError(result: Result): CallHandler2[IncomeSourcesError, Request[_], Result] = {
    (mockErrorHandler.handleError(_: IncomeSourcesError)(_: Request[_]))
      .expects(*, *)
      .returning(result)
  }

  def mockGetCalculationId():CallHandler4[String, Int, String, HeaderCarrier, Future[CalculationIdResponse]] = {
    (mockCalculationIdService.getCalculationId(_: String, _: Int, _: String)(_: HeaderCarrier))
      .expects(*, *, *, *)
      .returning(Future.successful(Right(LiabilityCalculationIdModel("calculationId"))))
  }
  def mockGetCalculationIdInternalServiceError():CallHandler4[String, Int, String, HeaderCarrier, Future[CalculationIdResponse]] = {
    (mockCalculationIdService.getCalculationId(_: String, _: Int, _: String)(_: HeaderCarrier))
      .expects(*, *, *, *)
      .returning(Future.successful(Left(CalculationIdErrorInternalServerError)))
  }
  def mockGetCalculationIdServiceUnavailableError():CallHandler4[String, Int, String, HeaderCarrier, Future[CalculationIdResponse]] = {
    (mockCalculationIdService.getCalculationId(_: String, _: Int, _: String)(_: HeaderCarrier))
      .expects(*, *, *, *)
      .returning(Future.successful(Left(CalculationIdErrorServiceUnavailableError)))
  }

  private val controller = new OverviewPageController(
    frontendAppConfig, stubMessagesControllerComponents(),mockExecutionContext, mockIncomeSourcesService, mockCalculationIdService,
    overviewPageView, authorisedAction, internalServerErrorPageView, serviceUnavailablePageView, mockErrorHandler
  )

  "calling the individual action" when {

    "the user is an individual with existing income sources" should {

      "GET '/' for an individual and return 200" in {

        val result = {
          mockAuth(nino)
          mockGetIncomeSourcesValid()
          controller.show(taxYear)(fakeGetRequest)
        }
        status(result) shouldBe Status.OK
      }

      "return HTML" in {
        val result = {
          mockAuth(nino)
          mockGetIncomeSourcesValid()
          controller.show(taxYear)(fakeGetRequest)
        }
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }

      "Set the session value for All prior sub" in {
        val result = {
          mockAuth(nino)
          mockGetIncomeSourcesValid()
          controller.show(taxYear)(fakeGetRequest)
        }
        session(result).get(DIVIDENDS_PRIOR_SUB) shouldBe Some(Json.toJson((DividendsModel(None,None))).toString())
        session(result).get(INTEREST_PRIOR_SUB) shouldBe Some(Json.toJson(Seq(InterestModel("", "", None, Some(500.00)))).toString())
      }

      "Set the session value for dividends prior sub" in {
        val result = {
          mockAuth(nino)
          mockGetIncomeSourcesDividends()
          controller.show(taxYear)(fakeGetRequest)
        }
        session(result).get(DIVIDENDS_PRIOR_SUB) shouldBe Some(Json.toJson((DividendsModel(None,None))).toString())
        session(result).get(INTEREST_PRIOR_SUB) shouldBe None
      }

      "Set the session value for interests prior sub" in {
        val result = {
          mockAuth(nino)
          mockGetIncomeSourcesInterest()
          controller.show(taxYear)(fakeGetRequest)
        }
        session(result).get(DIVIDENDS_PRIOR_SUB) shouldBe None
        session(result).get(INTEREST_PRIOR_SUB) shouldBe Some(Json.toJson(Seq(InterestModel("", "", None, Some(500.00)))).toString())
      }
    }

    "the user is an individual without existing income sources" should {

      "GET '/' for an individual and return 200" in {

        val result = {
          mockAuth(nino)
          mockGetIncomeSourcesNone()
          controller.show(taxYear)(fakeGetRequest)
        }
        status(result) shouldBe Status.OK
      }

      "GET '/' for an individual and return 500 if connector returns 500" in {
        val internalServerErrorPage: InternalServerErrorPage = app.injector.instanceOf[InternalServerErrorPage]

        val result = {

          mockAuth(nino)
          mockGetIncomeSourcesError()
          mockHandleError(InternalServerError(internalServerErrorPage()))
          controller.show(taxYear)(fakeGetRequest)
        }
        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
      }

      "return HTML" in {
        val result = {
          mockAuth(nino)
          mockGetIncomeSourcesNone()
          controller.show(taxYear)(fakeGetRequest)
        }
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }

    "there is no nino in session" should {

      s"GET '/' for an individual and return $SEE_OTHER" in {

        val result = {
          mockAuth(None)
          controller.show(taxYear)(fakeGetRequest)
        }
        status(result) shouldBe Status.SEE_OTHER
      }
    }
  }

  "calling the agent action" when {

    "the user is an agent with existing income sources" should {

      "GET '/' for an agent and return 200" in {

        val result = {
          mockAuthAsAgent()
          mockGetIncomeSourcesValid()
          controller.show(taxYear)(fakeGetRequest)
        }
        status(result) shouldBe Status.OK
      }

      "return HTML" in {
        val result = {
          mockAuthAsAgent()
          mockGetIncomeSourcesValid()
          controller.show(taxYear)(fakeGetRequest)
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
          controller.show(taxYear)(fakeGetRequest)
        }
        status(result) shouldBe Status.OK
      }

      "return HTML" in {
        val result = {
          mockAuthAsAgent()
          mockGetIncomeSourcesNone()
          controller.show(taxYear)(fakeGetRequest)
        }
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }
  }

  "Calling the .getCalculation method" when {

    "The user is an individual" should {

      "GET '/' for an individual and return a redirect with calculationId in session" in {

        val result = {
          mockAuth(nino)
          mockGetCalculationId()
          controller.getCalculation(taxYear)(fakeGetRequest)
        }
        status(result) shouldBe Status.SEE_OTHER
        session(result).get(CALCULATION_ID) shouldBe Some("calculationId")
      }

      "return a serviceUnavailableErrorPage" in {

        val result = {
          mockAuth(nino)
          mockGetCalculationIdServiceUnavailableError()
          controller.getCalculation(taxYear)(fakeGetRequest)

        }
        status(result) shouldBe Status.SERVICE_UNAVAILABLE
      }

      "return a InternalServerErrorPage" in {

        val result = {
          mockAuth(nino)
          mockGetCalculationIdInternalServiceError()
          controller.getCalculation(taxYear)(fakeGetRequest)
        }
        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
      }


    }

    "The user is an agent" should {

      "GET '/' for an individual and return a redirect" in {

        val result = {
          mockAuthAsAgent()
          mockGetCalculationId()
          controller.getCalculation(taxYear)(fakeGetRequest)
        }
        status(result) shouldBe Status.SEE_OTHER
        session(result).get(CALCULATION_ID) shouldBe Some("calculationId")
      }

      "return a serviceUnavailableErrorPage" in {

        val result = {
          mockAuth(nino)
          mockGetCalculationIdServiceUnavailableError()
          controller.getCalculation(taxYear)(fakeGetRequest)

        }
        status(result) shouldBe Status.SERVICE_UNAVAILABLE
      }

      "return a InternalServerErrorPage" in {

        val result = {
          mockAuth(nino)
          mockGetCalculationIdInternalServiceError()
          controller.getCalculation(taxYear)(fakeGetRequest)
        }
        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
      }

    }
  }
}
