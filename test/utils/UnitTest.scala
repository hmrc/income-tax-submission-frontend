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
import akka.stream.{ActorMaterializer, Materializer}
import com.codahale.metrics.SharedMetricRegistries
import common.{EnrolmentIdentifiers, EnrolmentKeys}
import config.{AppConfig, MockAppConfig}
import controllers.predicates.AuthorisedAction
import org.scalamock.scalatest.MockFactory
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc.{AnyContentAsEmpty, ControllerComponents, Result}
import play.api.test.{FakeRequest, Helpers}
import services.AuthService
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.auth.core.syntax.retrieved.authSyntaxForRetrieved
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import views.html.authErrorPages.AgentAuthErrorPageView

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Awaitable, ExecutionContext, Future}

trait UnitTest extends AnyWordSpec with Matchers with MockFactory with BeforeAndAfterEach with GuiceOneAppPerSuite{

  override def beforeEach(): Unit = {
    super.beforeEach()
    SharedMetricRegistries.clear()
  }

  implicit val actorSystem: ActorSystem = ActorSystem()
  implicit val materializer: Materializer = ActorMaterializer()

  def await[T](awaitable: Awaitable[T]): T = Await.result(awaitable, Duration.Inf)

  implicit val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
  val fakeRequestWithMtditid: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withSession("MTDITID" -> "1234567890", "NINO" -> "AA123456A")
  implicit val emptyHeaderCarrier: HeaderCarrier = HeaderCarrier()

  val mockAppConfig: AppConfig = new MockAppConfig().config
  implicit val mockControllerComponents: ControllerComponents = Helpers.stubControllerComponents()
  implicit val mockExecutionContext: ExecutionContext = ExecutionContext.Implicits.global
  implicit val mockAuthConnector: AuthConnector = mock[AuthConnector]
  implicit val mockAuthService: AuthService = new AuthService(mockAuthConnector)
  val agentAuthErrorPageView: AgentAuthErrorPageView = app.injector.instanceOf[AgentAuthErrorPageView]

  val authorisedAction = new AuthorisedAction(mockAppConfig, agentAuthErrorPageView)(mockAuthService, stubMessagesControllerComponents())

  def status(awaitable: Future[Result]): Int = await(awaitable).header.status

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
      .expects(*, *, *, *)
      .returning(Future.successful(enrolments and Some(AffinityGroup.Individual) and ConfidenceLevel.L200))
  }

  //noinspection ScalaStyle
  def mockAuthAsAgent() = {
    val enrolments = Enrolments(Set(
      Enrolment(EnrolmentKeys.Individual, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.individualId, "1234567890")), "Activated"),
      Enrolment(EnrolmentKeys.Agent, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.agentReference, "0987654321")), "Activated")
    ))
    (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, Retrievals.allEnrolments and Retrievals.affinityGroup and Retrievals.confidenceLevel, *, *)
      .returning(Future.successful(enrolments and Some(AffinityGroup.Agent) and ConfidenceLevel.L200))

    (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *, *)
      .returning(Future.successful(enrolments))
  }

  //noinspection ScalaStyle
  def mockAuthReturnException(exception: Exception) = {
    (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *, *)
      .returning(Future.failed(exception))
  }

}
