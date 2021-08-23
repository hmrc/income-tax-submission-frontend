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

package utils

import akka.actor.ActorSystem
import com.codahale.metrics.SharedMetricRegistries
import common.{EnrolmentIdentifiers, EnrolmentKeys}
import config.{AppConfig, MockAppConfig, MockAppConfigTaxYearFeatureOff}
import controllers.predicates.{AuthorisedAction, InYearAction}
import models.employment.{AllEmploymentData, EmploymentData, EmploymentSource, Pay}
import models.{APIErrorBodyModel, APIErrorModel, DividendsModel, GiftAidModel, GiftAidPaymentsModel, GiftsModel, InterestModel}
import org.scalamock.scalatest.MockFactory
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc.{AnyContentAsEmpty, ControllerComponents, Result}
import play.api.test.Helpers.{INTERNAL_SERVER_ERROR, SERVICE_UNAVAILABLE}
import play.api.test.{FakeRequest, Helpers}
import services.AuthService
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.Retrieval
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.syntax.retrieved.authSyntaxForRetrieved
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import views.html.authErrorPages.AgentAuthErrorPageView

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Awaitable, ExecutionContext, Future}

trait UnitTest extends AnyWordSpec with Matchers with MockFactory with BeforeAndAfterEach with GuiceOneAppPerSuite{

  override def beforeEach(): Unit = {
    super.beforeEach()
    SharedMetricRegistries.clear()
  }
  implicit val mockAppConfig: AppConfig = new MockAppConfig().config
  val inYearAction = new InYearAction


  implicit val actorSystem: ActorSystem = ActorSystem()

  def await[T](awaitable: Awaitable[T]): T = Await.result(awaitable, Duration.Inf)

  val sessionId = "sessionId-1618a1e8-4979-41d8-a32e-5ffbe69fac81"

  implicit val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withHeaders("X-Session-ID" -> sessionId)
  val fallBackSessionIdFakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withHeaders("sessionId" -> sessionId)
  val fakeRequestAgent: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
    .withSession("ClientMTDID" -> "1234567890", "ClientNino" -> "AA123456A").withHeaders("X-Session-ID" -> sessionId)
  implicit val headerCarrierWithSession: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId(sessionId)))
  val emptyHeaderCarrier: HeaderCarrier = HeaderCarrier()

  val mockAppConfigTaxYearFeatureOff: AppConfig = new MockAppConfigTaxYearFeatureOff().config
  implicit val mockControllerComponents: ControllerComponents = Helpers.stubControllerComponents()
  implicit val mockExecutionContext: ExecutionContext = ExecutionContext.Implicits.global
  implicit val mockAuthConnector: AuthConnector = mock[AuthConnector]
  implicit val mockAuthService: AuthService = new AuthService(mockAuthConnector)
  val agentAuthErrorPageView: AgentAuthErrorPageView = app.injector.instanceOf[AgentAuthErrorPageView]

  val authorisedAction = new AuthorisedAction(mockAppConfig, agentAuthErrorPageView)(mockAuthService, stubMessagesControllerComponents())

  def status(awaitable: Future[Result]): Int = await(awaitable).header.status
  def redirectUrl(awaitable: Future[Result]): String = await(awaitable).header.headers("Location")

  def bodyOf(awaitable: Future[Result]): String = {
    val awaited = await(awaitable)
    await(awaited.body.consumeData.map(_.utf8String))
  }

  val fakeRequestAgentNoMtditid: FakeRequest[AnyContentAsEmpty.type] = fakeRequest.withSession("ClientNino" -> "AA123456A")
  val fakeRequestAgentNoNino: FakeRequest[AnyContentAsEmpty.type] = fakeRequest.withSession("ClientMTDID" -> "1234567890")

  //noinspection ScalaStyle
  def mockAuth(nino: Option[String]) = {
    val ninoEnrolment: Seq[Enrolment] = nino.fold(Seq.empty[Enrolment])(unwrappedNino => Seq(
      Enrolment(EnrolmentKeys.nino, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.ninoId, unwrappedNino)), "Activated")
    ))

    val enrolments = Enrolments(Set(
      Enrolment(EnrolmentKeys.Individual, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.individualId, "1234567890")), "Activated"),
      Enrolment(EnrolmentKeys.Agent, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.agentReference, "0987654321")), "Activated")
    ) ++ ninoEnrolment)

    (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, Retrievals.affinityGroup, *, *)
      .returning(Future.successful(Some(AffinityGroup.Individual)))

    (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, Retrievals.allEnrolments and Retrievals.confidenceLevel, *, *)
      .returning(Future.successful(enrolments and ConfidenceLevel.L200))
  }

  //noinspection ScalaStyle
  def mockIVCredentials(affinityGroup: AffinityGroup, confidenceLevel: Int) = {
    val confidenceLevelResponse = confidenceLevel match {
      case 500 => ConfidenceLevel.L500
      case 200 => ConfidenceLevel.L200
      case 50 => ConfidenceLevel.L50
    }

    (
    mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
    .expects(*, *, *, *)
    .returning(Future.successful(Some(affinityGroup) and confidenceLevelResponse))
  }

  //noinspection ScalaStyle
  def mockAuthAsAgent() = {
    val enrolments = Enrolments(Set(
      Enrolment(EnrolmentKeys.Individual, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.individualId, "1234567890")), "Activated"),
      Enrolment(EnrolmentKeys.Agent, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.agentReference, "0987654321")), "Activated")
    ))

    (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, Retrievals.affinityGroup, *, *)
      .returning(Future.successful(Some(AffinityGroup.Agent)))

    (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, Retrievals.allEnrolments, *, *)
      .returning(Future.successful(enrolments))
  }

  //noinspection ScalaStyle
  def mockAuthReturnException(exception: Exception) = {
    (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *, *)
      .returning(Future.failed(exception))
  }

  val error500: APIErrorModel = APIErrorModel(INTERNAL_SERVER_ERROR,APIErrorBodyModel("INTERNAL_SERVER_ERROR","Internal server error"))
  val error503: APIErrorModel = APIErrorModel(SERVICE_UNAVAILABLE,APIErrorBodyModel("SERVICE_UNAVAILABLE","Service unavailable"))


  lazy val dividendsModel:Option[DividendsModel] = Some(DividendsModel(Some(100.00), Some(100.00)))
  lazy val interestsModel:Option[Seq[InterestModel]] = Some(Seq(InterestModel("TestName", "TestSource", Some(100.00), Some(100.00))))
  lazy val employmentsModel: AllEmploymentData = AllEmploymentData(
    hmrcEmploymentData = Seq(
      EmploymentSource(
        employmentId = "001",
        employerName = "maggie",
        employerRef = Some("223/AB12399"),
        payrollId = Some("123456789999"),
        startDate = Some("2019-04-21"),
        cessationDate = Some("2020-03-11"),
        dateIgnored = Some("2020-04-04T01:01:01Z"),
        submittedOn = Some("2020-01-04T05:01:01Z"),
        employmentData = Some(EmploymentData(
          submittedOn = ("2020-02-12"),
          employmentSequenceNumber = Some("123456789999"),
          companyDirector = Some(true),
          closeCompany = Some(false),
          directorshipCeasedDate = Some("2020-02-12"),
          occPen = Some(false),
          disguisedRemuneration = Some(false),
          pay = Some(Pay(Some(34234.15), Some(6782.92), Some("CALENDAR MONTHLY"), Some("2020-04-23"), Some(32), Some(2)))
        )),
        None
      )
    ),
    hmrcExpenses = None,
    customerEmploymentData = Seq(),
    customerExpenses = None
  )
  val giftAidPaymentsModel: Option[GiftAidPaymentsModel] = Some(GiftAidPaymentsModel(
    nonUkCharitiesCharityNames = Some(List("non uk charity name", "non uk charity name 2")),
    currentYear = Some(1234.56),
    oneOffCurrentYear = Some(1234.56),
    currentYearTreatedAsPreviousYear = Some(1234.56),
    nextYearTreatedAsCurrentYear = Some(1234.56),
    nonUkCharities = Some(1234.56),
  ))

  val giftsModel: Option[GiftsModel] = Some(GiftsModel(
    investmentsNonUkCharitiesCharityNames = Some(List("charity 1", "charity 2")),
    landAndBuildings = Some(10.21),
    sharesOrSecurities = Some(10.21),
    investmentsNonUkCharities = Some(1234.56)
  ))

  val giftAidModel: GiftAidModel = GiftAidModel(
    giftAidPaymentsModel,
    giftsModel
  )
}
