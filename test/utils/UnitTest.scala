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

package utils

import com.codahale.metrics.SharedMetricRegistries
import common.{EnrolmentIdentifiers, EnrolmentKeys, SessionValues}
import config.{AppConfig, MockAppConfig, MockAppConfigTaxYearFeatureOff}
import controllers.predicates.InYearAction
import models._
import models.employment._
import models.session.UserSessionData
import models.tasklist._
import org.apache.pekko.actor.ActorSystem
import org.scalamock.scalatest.MockFactory
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.mvc.{AnyContentAsEmpty, ControllerComponents, Result}
import play.api.test.Helpers.{CONFLICT, FORBIDDEN, INTERNAL_SERVER_ERROR, SERVICE_UNAVAILABLE}
import play.api.test.{FakeRequest, Helpers}
import services.AuthService
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.Retrieval
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.syntax.retrieved.authSyntaxForRetrieved
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}

import scala.concurrent.{ExecutionContext, Future}

trait UnitTest extends AnyWordSpec with Matchers with MockFactory with BeforeAndAfterEach {

  override def beforeEach(): Unit = {
    super.beforeEach()
    SharedMetricRegistries.clear()
  }
  implicit val mockAppConfig: AppConfig = new MockAppConfig()
  val inYearAction = new InYearAction

  implicit val actorSystem: ActorSystem = ActorSystem()

  val mtdItId: String = "1234567890"
  val nino: String = "AA123456A"
  val utr: String = "9999912345"
  val sessionId = "eb3158c2-0aff-4ce8-8d1b-f2208ace52fe"
  val sessionData: UserSessionData = UserSessionData(sessionId, mtdItId, nino, Some(utr))

  def await[T](awaitable: Future[T]): T = Helpers.await(awaitable)

  implicit val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withHeaders("sessionId" -> sessionId)
  lazy val fakeRequestWithMtditidAndNino: FakeRequest[AnyContentAsEmpty.type] = fakeRequest.withSession(
    SessionValues.TAX_YEAR -> "2022",
    SessionValues.CLIENT_MTDITID -> "1234567890",
    SessionValues.CLIENT_NINO -> "A123456A"
  )
  val fakeRequestWithNino: FakeRequest[AnyContentAsEmpty.type] = fakeRequest.withSession(
    SessionValues.CLIENT_NINO -> "AA123456A"
  )
  implicit val headerCarrierWithSession: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId(sessionId)))
  val emptyHeaderCarrier: HeaderCarrier = HeaderCarrier()

  val mockAppConfigTaxYearFeatureOff: AppConfig = new MockAppConfigTaxYearFeatureOff()
  implicit val mockControllerComponents: ControllerComponents = Helpers.stubControllerComponents()
  implicit val mockExecutionContext: ExecutionContext = ExecutionContext.Implicits.global
  implicit val mockAuthConnector: AuthConnector = mock[AuthConnector]
  implicit val mockAuthService: AuthService = new AuthService(mockAuthConnector)
  
  def status(awaitable: Future[Result]): Int = await(awaitable).header.status
  def redirectUrl(awaitable: Future[Result]): String = await(awaitable).header.headers("Location")

  def bodyOf(awaitable: Future[Result]): String = {
    val awaited = await(awaitable)
    await(awaited.body.consumeData.map(_.utf8String))
  }

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
      .returning(Future.successful(enrolments and ConfidenceLevel.L250))
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

  val intentToCrystalliseError403: APIErrorModel = APIErrorModel(FORBIDDEN, APIErrorBodyModel("NO_SUBMISSION_EXIST", "The remote endpoint has indicated that no income submissions exist"))
  val intentToCrystalliseError409: APIErrorModel = APIErrorModel(CONFLICT, APIErrorBodyModel("CONFLICT", "The remote endpoint has indicated that final declaration has already been received"))

  lazy val dividendsModel:Option[DividendsModel] = Some(DividendsModel(Some(100.00), Some(100.00)))
  lazy val interestsModel:Option[Seq[InterestModel]] = Some(Seq(InterestModel("TestName", "TestSource", Some(100.00), Some(100.00))))
  lazy val employmentsModel: AllEmploymentData = AllEmploymentData(
    hmrcEmploymentData = Seq(
      HmrcEmploymentSource(
        employmentId = "001",
        employerName = "maggie",
        employerRef = Some("223/AB12399"),
        payrollId = Some("123456789999"),
        startDate = Some("2019-04-21"),
        cessationDate = Some("2020-03-11"),
        dateIgnored = Some("2020-04-04T01:01:01Z"),
        submittedOn = Some("2020-01-04T05:01:01Z"),
        hmrcEmploymentFinancialData = Some(
          EmploymentFinancialData(
            employmentData = Some(EmploymentData(
              submittedOn = ("2020-02-12"),
              employmentSequenceNumber = Some("123456789999"),
              companyDirector = Some(true),
              closeCompany = Some(false),
              directorshipCeasedDate = Some("2020-02-12"),
              disguisedRemuneration = Some(false),
              pay = Some(Pay(Some(34234.15), Some(6782.92), Some("CALENDAR MONTHLY"), Some("2020-04-23"), Some(32), Some(2)))
            )),
            None
          )
        ),
        customerEmploymentFinancialData = None
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

  val taskListModel: TaskListModel = TaskListModel(
    Seq[TaskListSection](
      TaskListSection(
        SectionTitle.EmploymentTitle,
        Some(Seq(TaskListSectionItem(TaskTitle.PayeEmployment, TaskStatus.Completed, Some(""))))
      ),
      TaskListSection(
        SectionTitle.JsaTitle,
        Some(Seq(TaskListSectionItem(TaskTitle.JSA, TaskStatus.InProgress, Some(""))))
      ),
      TaskListSection(
        SectionTitle.EsaTitle,
        Some(Seq(TaskListSectionItem(TaskTitle.ESA, TaskStatus.NotStarted, Some(""))))
      ),
      TaskListSection(
        SectionTitle.AboutYouTitle,
        Some(Seq(TaskListSectionItem(TaskTitle.UkResidenceStatus, TaskStatus.CheckNow, Some(""))))
      )
    )
  )

  val taskListModelCompleted: TaskListModel = TaskListModel(
    Seq[TaskListSection](
      TaskListSection(
        SectionTitle.EmploymentTitle,
        Some(Seq(TaskListSectionItem(TaskTitle.PayeEmployment, TaskStatus.Completed, Some(""))))
      )
    )
  )
}
