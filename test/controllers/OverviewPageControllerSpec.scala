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


import audit.IntentToCrystalliseDetail
import common.SessionValues
import common.SessionValues._
import config.{AppConfig, ErrorHandler, MockAuditService}
import connectors.httpParsers.IncomeSourcesHttpParser.IncomeSourcesResponse
import connectors.httpParsers.LiabilityCalculationHttpParser.LiabilityCalculationResponse
import models._
import org.scalamock.handlers.{CallHandler2, CallHandler4}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.Results._
import play.api.mvc.{Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Configuration, Environment}
import services.{IncomeSourcesService, LiabilityCalculationService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils.UnitTest
import views.html.OverviewPageView
import views.html.errors.{InternalServerErrorPage, NoUpdatesProvidedPage, ReturnTaxYearExistsView, ServiceUnavailablePage}

import scala.concurrent.Future

class OverviewPageControllerSpec extends UnitTest with GuiceOneAppPerSuite with MockAuditService {

  private val fakeGetRequest = FakeRequest("GET", "/").withSession(
    SessionValues.CLIENT_MTDITID -> "1234567890",
    SessionValues.CLIENT_NINO -> "AA123456A",
    SessionValues.TAX_YEAR -> "2022"
  ).withHeaders("X-Session-ID" -> sessionId)

  private val fakeGetRequestEndOfYear = FakeRequest("GET", "/").withSession(
    SessionValues.CLIENT_MTDITID -> "1234567890",
    SessionValues.CLIENT_NINO -> "AA123456A",
    SessionValues.TAX_YEAR -> "2021"
  ).withHeaders("X-Session-ID" -> sessionId)

  private val env = Environment.simple()
  private val configuration = Configuration.load(env)

  private val serviceConfig = new ServicesConfig(configuration)
  private implicit val frontendAppConfig: AppConfig = new AppConfig(serviceConfig)
  implicit lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit lazy val messages: Messages = messagesApi.preferred(FakeRequest())
  private val overviewPageView: OverviewPageView = app.injector.instanceOf[OverviewPageView]
  private val serviceUnavailablePageView: ServiceUnavailablePage = app.injector.instanceOf[ServiceUnavailablePage]
  private val internalServerErrorPageView: InternalServerErrorPage = app.injector.instanceOf[InternalServerErrorPage]
  private val noUpdatesProvidedPage: NoUpdatesProvidedPage = app.injector.instanceOf[NoUpdatesProvidedPage]
  private val returnTaxYearExistsView: ReturnTaxYearExistsView = app.injector.instanceOf[ReturnTaxYearExistsView]
  private val mockIncomeSourcesService = mock[IncomeSourcesService]
  private val mockErrorHandler = mock[ErrorHandler]
  private val mockLiabilityCalculationService = mock[LiabilityCalculationService]
  
  private val nino = Some("AA123456A")
  private val taxYear = 2022
  private val taxYearEndOfYear = taxYear - 1

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
    val invalidIncomeSource: IncomeSourcesResponse = Left(error500)
    (mockIncomeSourcesService.getIncomeSources(_: String, _: Int, _: String)(_: HeaderCarrier))
      .expects(*, *, *, *)
      .returning(Future.successful(invalidIncomeSource))
  }

  def mockHandleError(result: Result): CallHandler2[Int, Request[_], Result] = {
    (mockErrorHandler.handleError(_: Int)(_: Request[_]))
      .expects(*, *)
      .returning(result)
  }

  def mockHandleIntentToCrystalliseError(result: Result): CallHandler4[Int, Boolean, Int, Request[_], Result] = {
    (mockErrorHandler.handleIntentToCrystalliseError(_: Int, _: Boolean, _: Int)(_: Request[_]))
      .expects(*, *, *, *)
      .returning(result)
  }

  def mockGetCalculationId():CallHandler4[String, Int, String, HeaderCarrier, Future[LiabilityCalculationResponse]] = {
    (mockLiabilityCalculationService.getCalculationId(_: String, _: Int, _: String)(_: HeaderCarrier))
      .expects(*, *, *, *)
      .returning(Future.successful(Right(LiabilityCalculationIdModel("calculationId"))))
  }

  def mockGetCalculationIdInternalServerError():CallHandler4[String, Int, String, HeaderCarrier, Future[LiabilityCalculationResponse]] = {
    (mockLiabilityCalculationService.getCalculationId(_: String, _: Int, _: String)(_: HeaderCarrier))
      .expects(*, *, *, *)
      .returning(Future.successful(Left(error500)))
  }

  def mockGetCalculationIdServiceUnavailableError():CallHandler4[String, Int, String, HeaderCarrier, Future[LiabilityCalculationResponse]] = {
    (mockLiabilityCalculationService.getCalculationId(_: String, _: Int, _: String)(_: HeaderCarrier))
      .expects(*, *, *, *)
      .returning(Future.successful(Left(error503)))
  }

  def mockPostIntentToCrystallise():CallHandler4[String, Int, String, HeaderCarrier, Future[LiabilityCalculationResponse]] = {
    (mockLiabilityCalculationService.getIntentToCrystallise(_: String, _: Int, _: String)(_: HeaderCarrier))
      .expects(*, *, *, *)
      .returning(Future.successful(Right(LiabilityCalculationIdModel("calculationId"))))
  }

  def mockPostIntentToCrystalliseInternalServerError():CallHandler4[String, Int, String, HeaderCarrier, Future[LiabilityCalculationResponse]] = {
    (mockLiabilityCalculationService.getIntentToCrystallise(_: String, _: Int, _: String)(_: HeaderCarrier))
      .expects(*, *, *, *)
      .returning(Future.successful(Left(error500)))
  }
  def mockPostIntentToCrystalliseServiceUnavailableError():CallHandler4[String, Int, String, HeaderCarrier, Future[LiabilityCalculationResponse]] = {
    (mockLiabilityCalculationService.getIntentToCrystallise(_: String, _: Int, _: String)(_: HeaderCarrier))
      .expects(*, *, *, *)
      .returning(Future.successful(Left(error503)))
  }

  def mockPostIntentToCrystalliseNoUpdatesProvidedError():CallHandler4[String, Int, String, HeaderCarrier, Future[LiabilityCalculationResponse]] = {
    (mockLiabilityCalculationService.getIntentToCrystallise(_: String, _: Int, _: String)(_: HeaderCarrier))
      .expects(*, *, *, *)
      .returning(Future.successful(Left(intentToCrystalliseError403)))
  }
  def mockPostIntentToCrystalliseReturnTaxYearExistsError():CallHandler4[String, Int, String, HeaderCarrier, Future[LiabilityCalculationResponse]] = {
    (mockLiabilityCalculationService.getIntentToCrystallise(_: String, _: Int, _: String)(_: HeaderCarrier))
      .expects(*, *, *, *)
      .returning(Future.successful(Left(intentToCrystalliseError409)))
  }

  private val controller = new OverviewPageController(
    frontendAppConfig, stubMessagesControllerComponents(),mockExecutionContext, inYearAction, mockIncomeSourcesService, mockLiabilityCalculationService,
    overviewPageView, authorisedAction, mockErrorHandler, mockAuditService
  )

  private val controllerEndOfYear = new OverviewPageController(
    mockAppConfigTaxYearFeatureOff, stubMessagesControllerComponents(),mockExecutionContext, inYearAction, mockIncomeSourcesService, mockLiabilityCalculationService,
    overviewPageView, authorisedAction, mockErrorHandler, mockAuditService
  )


  "calling the individual action" when {

    "the user is in year and with existing income sources" should {

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
    }

    "the user is in year and without existing income sources" should {

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

    "the user is an individual and at the end of year" should {

      "GET '/' and redirect to the postCrystallisation Overview" in {

        val result = {
          mockAuth(nino)
          
          controllerEndOfYear.show(taxYearEndOfYear)(fakeGetRequestEndOfYear)
        }
        status(result) shouldBe SEE_OTHER
        redirectUrl(result) shouldBe controllers.routes.OverviewPageController.showCrystallisation(taxYearEndOfYear).url
      }

      "GET '/' for an individual at and return 200" in {

        val result = {
          mockAuth(nino)
          mockGetIncomeSourcesValid()

          controllerEndOfYear.showCrystallisation(taxYearEndOfYear)(fakeGetRequestEndOfYear)
        }

        status(result) shouldBe Status.OK
      }

      "GET '/' for an individual and return 500 if connector returns 500" in {

        val internalServerErrorPage: InternalServerErrorPage = app.injector.instanceOf[InternalServerErrorPage]

        val result = {
          mockAuth(nino)
          mockGetIncomeSourcesError()
          mockHandleError(InternalServerError(internalServerErrorPage()))
          controllerEndOfYear.showCrystallisation(taxYearEndOfYear)(fakeGetRequestEndOfYear)
        }

        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
      }

      "return HTML" in {

        val result = {
          mockAuth(nino)
          mockGetIncomeSourcesValid()

          controllerEndOfYear.showCrystallisation(taxYearEndOfYear)(fakeGetRequestEndOfYear)
        }
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }

  "calling the agent action" when {

    "the user is an agent with existing income sources and in year" should {

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
    "the user is an agent without existing income sources and in year" should {

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

  "Calling the .getCalculation method" when {

    "The user is an individual" should {

      "GET '/' for an individual and return a redirect with calculationId in session" in {

        val result = {
          mockAuth(nino)
          mockGetCalculationId()
          controller.getCalculation(taxYear)(fakeGetRequest)
        }
        status(result) shouldBe Status.SEE_OTHER
        redirectUrl(result) shouldBe frontendAppConfig.viewAndChangeCalculationUrl(taxYear)
        session(result).get(CALCULATION_ID) shouldBe Some("calculationId")
      }

      "return a serviceUnavailableErrorPage" in {

        val result = {
          mockAuth(nino)
          mockGetCalculationIdServiceUnavailableError()
          mockHandleError(ServiceUnavailable(serviceUnavailablePageView()))

          controller.getCalculation(taxYear)(fakeGetRequest)

        }
        status(result) shouldBe Status.SERVICE_UNAVAILABLE
      }

      "return a InternalServerErrorPage" in {

        val result = {
          mockAuth(nino)
          mockGetCalculationIdInternalServerError()
          mockHandleError(InternalServerError(internalServerErrorPageView()))
          controller.getCalculation(taxYear)(fakeGetRequest)
        }
        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
      }
    }

    "The user is an agent" should {

      "GET '/' for an agent and return a redirect with the calculationId in session" in {

        val result = {
          mockAuthAsAgent()
          mockGetCalculationId()
          controller.getCalculation(taxYear)(fakeGetRequest)
        }
        status(result) shouldBe Status.SEE_OTHER
        redirectUrl(result) shouldBe frontendAppConfig.viewAndChangeCalculationUrlAgent(taxYear)
        session(result).get(CALCULATION_ID) shouldBe Some("calculationId")
      }

      "return a serviceUnavailableErrorPage" in {

        val result = {
          mockAuth(nino)
          mockGetCalculationIdServiceUnavailableError()
          mockHandleError(ServiceUnavailable(serviceUnavailablePageView()))
          controller.getCalculation(taxYear)(fakeGetRequest)

        }
        status(result) shouldBe Status.SERVICE_UNAVAILABLE
      }

      "return a InternalServerErrorPage" in {

        val result = {
          mockAuth(nino)
          mockGetCalculationIdInternalServerError()
          mockHandleError(InternalServerError(internalServerErrorPageView()))
          controller.getCalculation(taxYear)(fakeGetRequest)
        }
        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
      }

    }

  }

  "calling the .getFinalCalculation method" when {

    "the user is an individual and at the end of year" should {

      "GET '/' and return a redirect with a calculationId in session" in {

        val result = {
          mockAuth(nino)
          mockPostIntentToCrystallise()
          verifyAuditEvent[IntentToCrystalliseDetail]
          controllerEndOfYear.finalCalculation(taxYearEndOfYear)(fakeGetRequestEndOfYear)
        }
        status(result) shouldBe Status.SEE_OTHER
        session(result).get(CALCULATION_ID) shouldBe Some("calculationId")
      }

      "return ServiceUnavailable error page when the service is unavailable" in {

        val result = {
          mockAuth(nino)
          mockPostIntentToCrystalliseServiceUnavailableError()
          mockHandleIntentToCrystalliseError(ServiceUnavailable(serviceUnavailablePageView()))

          controllerEndOfYear.finalCalculation(taxYearEndOfYear)(fakeGetRequestEndOfYear)
        }
        status(result) shouldBe Status.SERVICE_UNAVAILABLE
      }

      "return an InternalServerError page when there is a problem with the service" in {

        val result = {
          mockAuth(nino)
          mockPostIntentToCrystalliseInternalServerError()
          mockHandleIntentToCrystalliseError(InternalServerError(internalServerErrorPageView()))

          controllerEndOfYear.finalCalculation(taxYearEndOfYear)(fakeGetRequestEndOfYear)
        }
        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
      }

      "return a NoUpdatesProvided error page when no submissions exist" in {

        val result = {
          mockAuth(nino)
          mockPostIntentToCrystalliseNoUpdatesProvidedError()
          mockHandleIntentToCrystalliseError(Forbidden(noUpdatesProvidedPage(false, taxYear)))

          controllerEndOfYear.finalCalculation(taxYearEndOfYear)(fakeGetRequestEndOfYear)
        }
        status(result) shouldBe Status.FORBIDDEN
      }

      "return an Conflict page when there is a final declaration already rceived" in {

        val result = {
          mockAuth(nino)
          mockPostIntentToCrystalliseReturnTaxYearExistsError()
          mockHandleIntentToCrystalliseError(Conflict(returnTaxYearExistsView(false, taxYear)))

          controllerEndOfYear.finalCalculation(taxYearEndOfYear)(fakeGetRequestEndOfYear)
        }
        status(result) shouldBe Status.CONFLICT
      }
    }

    "the user is an agent and at the end of year" should {

      "GET '/' and return a redirect with a calculationId in session" in {

        val result = {
          mockAuthAsAgent()
          mockPostIntentToCrystallise()
          verifyAuditEvent[IntentToCrystalliseDetail]
          controllerEndOfYear.finalCalculation(taxYearEndOfYear)(fakeGetRequestEndOfYear)
        }
        status(result) shouldBe Status.SEE_OTHER
        session(result).get(CALCULATION_ID) shouldBe Some("calculationId")
      }

      "return a ServiceUnavailable error page when the service is unavailable" in {

        val result = {
          mockAuthAsAgent()
          mockPostIntentToCrystalliseServiceUnavailableError()
          mockHandleIntentToCrystalliseError(ServiceUnavailable(serviceUnavailablePageView()))

          controllerEndOfYear.finalCalculation(taxYearEndOfYear)(fakeGetRequestEndOfYear)
        }
        status(result) shouldBe Status.SERVICE_UNAVAILABLE
      }

      "return an InternalServerError page when there is a problem with the service" in {

        val result = {
          mockAuthAsAgent()
          mockPostIntentToCrystalliseInternalServerError()
          mockHandleIntentToCrystalliseError(InternalServerError(internalServerErrorPageView()))

          controllerEndOfYear.finalCalculation(taxYearEndOfYear)(fakeGetRequestEndOfYear)
        }
        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
      }

      "return a NoUpdatesProvided error page when no submissions exist" in {

        val result = {
          mockAuthAsAgent()
          mockPostIntentToCrystalliseNoUpdatesProvidedError()
          mockHandleIntentToCrystalliseError(Forbidden(noUpdatesProvidedPage(true, taxYear)))

          controllerEndOfYear.finalCalculation(taxYearEndOfYear)(fakeGetRequestEndOfYear)
        }
        status(result) shouldBe Status.FORBIDDEN
      }

      "return an Conflict page when there is a final declaration already received" in {

        val result = {
          mockAuthAsAgent()
          mockPostIntentToCrystalliseReturnTaxYearExistsError()
          mockHandleIntentToCrystalliseError(Conflict(returnTaxYearExistsView(true, taxYear)))

          controllerEndOfYear.finalCalculation(taxYearEndOfYear)(fakeGetRequestEndOfYear)
        }
        status(result) shouldBe Status.CONFLICT
      }
    }
  }
}
