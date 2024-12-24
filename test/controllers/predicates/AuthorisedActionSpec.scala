/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers.predicates

import common.{DelegatedAuthRules, EnrolmentIdentifiers, EnrolmentKeys, SessionValues}
import config.AppConfig
import mocks.MockErrorHandler
import models.User
import org.scalamock.handlers.{CallHandler0, CallHandler4}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status._
import play.api.mvc.Results._
import play.api.mvc.{AnyContent, AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.Retrieval
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{allEnrolments, confidenceLevel}
import uk.gov.hmrc.auth.core.syntax.retrieved.authSyntaxForRetrieved
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils.UnitTest
import views.html.authErrorPages.AgentAuthErrorPageView

import scala.concurrent.{ExecutionContext, Future}

class AuthorisedActionSpec extends UnitTest
  with GuiceOneAppPerSuite
  with MockErrorHandler {

  val agentAuthErrorPageView: AgentAuthErrorPageView = app.injector.instanceOf[AgentAuthErrorPageView]

  val authorisedAction = new AuthorisedAction(
    appConfig = mockAppConfig,
    agentAuthErrorPage = agentAuthErrorPageView
  )(
    authService = mockAuthService,
    errorHandler = mockErrorHandler,
    mcc = stubMessagesControllerComponents()
  )

  val auth: AuthorisedAction = authorisedAction
  val nino: String = "AA123456A"
  val block: User[AnyContent] => Future[Result] = user => Future.successful(Ok(user.mtditid))
  val mtditid = "1234567890"

  trait AgentTest {

    val arn: String = "0987654321"

    val baseUrl = "/update-and-submit-income-tax-return"
    val viewAndChangeUrl: String = "/report-quarterly/income-and-expenses/view/agents/client-utr"
    val signInUrl: String = s"$baseUrl/signIn"

    val validHeaderCarrier: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionId")))

    val testBlock: User[AnyContent] => Future[Result] = user => Future.successful(Ok(s"${user.mtditid} ${user.arn.get}"))

    val mockAppConfig: AppConfig = mock[AppConfig]

    def primaryAgentPredicate(mtdId: String): Predicate =
      Enrolment(EnrolmentKeys.Individual)
        .withIdentifier(EnrolmentIdentifiers.individualId, mtdId)
        .withDelegatedAuthRule(DelegatedAuthRules.agentDelegatedAuthRule)

    def secondaryAgentPredicate(mtdId: String): Predicate =
      Enrolment(EnrolmentKeys.SupportingAgent)
        .withIdentifier(EnrolmentIdentifiers.individualId, mtdId)
        .withDelegatedAuthRule(DelegatedAuthRules.supportingAgentDelegatedAuthRule)

    def mockMultipleAgentsSwitch(bool: Boolean): CallHandler0[Boolean] =
      (mockAppConfig.emaSupportingAgentsEnabled _: () => Boolean)
        .expects()
        .returning(bool)
        .anyNumberOfTimes()

    val primaryAgentEnrolment: Enrolments = Enrolments(Set(
      Enrolment(EnrolmentKeys.Individual, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.individualId, mtditid)), "Activated"),
      Enrolment(EnrolmentKeys.Agent, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.agentReference, arn)), "Activated")
    ))

    val supportingAgentEnrolment: Enrolments = Enrolments(Set(
      Enrolment(EnrolmentKeys.SupportingAgent, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.individualId, mtditid)), "Activated"),
      Enrolment(EnrolmentKeys.Agent, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.agentReference, arn)), "Activated")
    ))

    def mockAuthReturnException(exception: Exception,
                                predicate: Predicate): CallHandler4[Predicate, Retrieval[_], HeaderCarrier, ExecutionContext, Future[Any]] =
      (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
        .expects(predicate, *, *, *)
        .returning(Future.failed(exception))

    def mockAuthReturn(enrolments: Enrolments, predicate: Predicate): CallHandler4[Predicate, Retrieval[_], HeaderCarrier, ExecutionContext, Future[Any]] =
      (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
        .expects(predicate, *, *, *)
        .returning(Future.successful(enrolments))

    def mockSignInUrl(): CallHandler0[String] =
      (mockAppConfig.signInUrl _: () => String)
        .expects()
        .returning(signInUrl)
        .anyNumberOfTimes()

    def mockViewAndChangeUrl(): CallHandler0[String] =
      (mockAppConfig.viewAndChangeEnterUtrUrl _: () => String)
        .expects()
        .returning(viewAndChangeUrl)
        .anyNumberOfTimes()

    def testAuth: AuthorisedAction = {
      mockViewAndChangeUrl()
      mockSignInUrl()

      new AuthorisedAction(
        appConfig = mockAppConfig,
        agentAuthErrorPage = agentAuthErrorPageView
      )(
        authService = mockAuthService,
        errorHandler = mockErrorHandler,
        mcc = stubMessagesControllerComponents()
      )
    }

    lazy val fakeRequestWithMtditidAndNino: FakeRequest[AnyContentAsEmpty.type] = fakeRequest.withSession(
      SessionValues.TAX_YEAR -> "2022",
      SessionValues.CLIENT_MTDITID -> mtditid,
      SessionValues.CLIENT_NINO -> nino
    )
  }

  ".enrolmentGetIdentifierValue" should {
    "return the value for the given identifier" in {
      val returnValue = "anIdentifierValue"
      val returnValueAgent = "anAgentIdentifierValue"

      val enrolments = Enrolments(Set(
        Enrolment(EnrolmentKeys.Individual, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.individualId, returnValue)), "Activated"),
        Enrolment(EnrolmentKeys.Agent, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.agentReference, returnValueAgent)), "Activated")
      ))

      auth.enrolmentGetIdentifierValue(EnrolmentKeys.Individual, EnrolmentIdentifiers.individualId, enrolments) shouldBe Some(returnValue)
      auth.enrolmentGetIdentifierValue(EnrolmentKeys.Agent, EnrolmentIdentifiers.agentReference, enrolments) shouldBe Some(returnValueAgent)
    }

    "return a None" when {
      val key = "someKey"
      val identifierKey = "anIdentifier"
      val returnValue = "anIdentifierValue"

      val enrolments = Enrolments(Set(Enrolment(key, Seq(EnrolmentIdentifier(identifierKey, returnValue)), "someState")))


      "the given identifier cannot be found" in {
        auth.enrolmentGetIdentifierValue(key, "someOtherIdentifier", enrolments) shouldBe None
      }

      "the given key cannot be found" in {
        auth.enrolmentGetIdentifierValue("someOtherKey", identifierKey, enrolments) shouldBe None
      }

    }

  }

  ".individualAuthentication" should {
    "perform the block action" when {
      "the correct enrolment exist" which {

        val enrolments = Enrolments(Set(
          Enrolment(EnrolmentKeys.Individual, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.individualId, mtditid)), "Activated"),
          Enrolment(EnrolmentKeys.nino, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.ninoId, nino)), "Activated")
        ))

        lazy val result: Future[Result] = {
          (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
            .expects(*, allEnrolments and confidenceLevel, *, *)
            .returning(Future.successful(enrolments and ConfidenceLevel.L250))
          auth.individualAuthentication[AnyContent](block)(fakeRequest, emptyHeaderCarrier)
        }

        "returns an OK status" in {
          status(result) shouldBe OK
        }

        "returns a body of the mtditid" in {
          bodyOf(result) shouldBe mtditid
        }
      }

    }

    "return a redirect" when {

      "the nino enrolment is missing" which {

        val enrolments = Enrolments(Set())

        lazy val result: Future[Result] = {
          (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
            .expects(*, allEnrolments and confidenceLevel, *, *)
            .returning(Future.successful(enrolments and ConfidenceLevel.L250))
          auth.individualAuthentication[AnyContent](block)(fakeRequest, emptyHeaderCarrier)
        }

        "returns a forbidden" in {
          status(result) shouldBe SEE_OTHER
        }
      }

      "the individual enrolment is missing but there is a nino" which {

        val enrolments = Enrolments(Set(Enrolment("HMRC-NI", Seq(EnrolmentIdentifier(EnrolmentIdentifiers.ninoId, nino)), "Activated")))

        lazy val result: Future[Result] = {
          (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
            .expects(*, allEnrolments and confidenceLevel, *, *)
            .returning(Future.successful(enrolments and ConfidenceLevel.L250))
          auth.individualAuthentication[AnyContent](block)(fakeRequest, emptyHeaderCarrier)
        }

        "returns an Unauthorised" in {
          status(result) shouldBe SEE_OTHER
        }
        "returns an redirect to the correct page" in {
          redirectUrl(result) shouldBe "/update-and-submit-income-tax-return/error/you-need-to-sign-up"
        }
      }

      "there is no session id" which {

        val enrolments = Enrolments(Set(
          Enrolment(EnrolmentKeys.Individual, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.individualId, mtditid)), "Activated"),
          Enrolment(EnrolmentKeys.nino, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.ninoId, nino)), "Activated")
        ))

        lazy val result: Future[Result] = {
          (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
            .expects(*, allEnrolments and confidenceLevel, *, *)
            .returning(Future.successful(enrolments and ConfidenceLevel.L250))
          auth.individualAuthentication[AnyContent](block)(FakeRequest(), emptyHeaderCarrier)
        }

        "returns an OK status" in {
          status(result) shouldBe SEE_OTHER
        }

        "returns a body of the mtditid" in {
          redirectUrl(result) shouldBe "/signIn"
        }
      }
    }

    "return the user to IV Uplift" when {

      "the confidence level is below minimum" which {

        val enrolments = Enrolments(Set(
          Enrolment(EnrolmentKeys.Individual, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.individualId, mtditid)), "Activated"),
          Enrolment(EnrolmentKeys.nino, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.ninoId, "AA123456A")), "Activated")
        ))

        lazy val result: Future[Result] = {
          (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
            .expects(*, allEnrolments and confidenceLevel, *, *)
            .returning(Future.successful(enrolments and ConfidenceLevel.L50))
          auth.individualAuthentication[AnyContent](block)(fakeRequest, emptyHeaderCarrier)
        }

        "has a status of 303" in {
          status(result) shouldBe SEE_OTHER
        }

        "redirects to the iv url" in {
          await(result).header.headers("Location") shouldBe "/update-and-submit-income-tax-return/iv-uplift"
        }
      }
    }
  }

  ".agentAuthenticated" when {
    "MTD ID and/or NINO are not found in the session" should {
      "return a redirect to View and Change service" in new AgentTest {
        val result: Future[Result] = testAuth.agentAuthentication(testBlock)(
          request = FakeRequest().withSession(fakeRequest.session.data.toSeq :_*),
          hc = emptyHeaderCarrier
        )

        status(result) shouldBe SEE_OTHER
        redirectUrl(result) shouldBe viewAndChangeUrl
      }
    }

    "NINO and MTD IT ID are present in the session" which {
      "results in a NoActiveSession error to be returned from Auth" should {
        "return a redirect to the login page" in new AgentTest {
          object AuthException extends NoActiveSession("Some reason")
          mockAuthReturnException(AuthException, primaryAgentPredicate(mtditid))

          val result: Future[Result] = testAuth.agentAuthentication(testBlock)(
            request = FakeRequest().withSession(fakeRequestWithMtditidAndNino.session.data.toSeq :_*),
            hc = emptyHeaderCarrier
          )

          status(result) shouldBe SEE_OTHER
          redirectUrl(result) shouldBe s"$baseUrl/signIn"
        }
      }

      "[EMA disabled] results in an AuthorisationException error being returned from Auth" should {
        "return a redirect to the agent error page" in new AgentTest {
          mockMultipleAgentsSwitch(false)

          object AuthException extends AuthorisationException("Some reason")
          mockAuthReturnException(AuthException, primaryAgentPredicate(mtditid))

          val result: Future[Result] = testAuth.agentAuthentication(testBlock)(
            request = FakeRequest().withSession(fakeRequestWithMtditidAndNino.session.data.toSeq :_*),
            hc = emptyHeaderCarrier
          )

          status(result) shouldBe SEE_OTHER
          redirectUrl(result) shouldBe s"$baseUrl/error/you-need-client-authorisation"
        }
      }

      "results in an unexpected error being thrown during primary agent auth call" should {
        "return an InternalServerError page" in new AgentTest {
          mockMultipleAgentsSwitch(false)

          object OtherException extends IndexOutOfBoundsException("Some reason")
          mockAuthReturnException(OtherException, primaryAgentPredicate(mtditid))
          mockInternalServerError(InternalServerError("An unexpected error occurred"))

          val result: Future[Result] = testAuth.agentAuthentication(testBlock)(
            request = FakeRequest().withSession(fakeRequestWithMtditidAndNino.session.data.toSeq :_*),
            hc = emptyHeaderCarrier
          )

          status(result) shouldBe INTERNAL_SERVER_ERROR
          bodyOf(result) shouldBe "An unexpected error occurred"
        }
      }

      "results in an unexpected error being thrown during secondary agent auth call" should {
        "return an InternalServerError page" in new AgentTest {
          mockMultipleAgentsSwitch(true)

          object AuthException extends AuthorisationException("Some reason")
          object OtherException extends IndexOutOfBoundsException("Some reason")

          mockAuthReturnException(AuthException, primaryAgentPredicate(mtditid))
          mockAuthReturnException(OtherException, secondaryAgentPredicate(mtditid))
          mockInternalServerError(InternalServerError("An unexpected error occurred"))

          val result: Future[Result] = testAuth.agentAuthentication(testBlock)(
            request = FakeRequest().withSession(fakeRequestWithMtditidAndNino.session.data.toSeq :_*),
            hc = emptyHeaderCarrier
          )

          status(result) shouldBe INTERNAL_SERVER_ERROR
          bodyOf(result) shouldBe "An unexpected error occurred"
        }
      }

      "[EMA enabled] results in an AuthorisationException error being returned from Auth" should {
        "return a redirect to the agent error page when secondary agent auth call also fails" in new AgentTest {
          mockMultipleAgentsSwitch(true)

          object AuthException extends AuthorisationException("Some reason")
          mockAuthReturnException(AuthException, primaryAgentPredicate(mtditid))
          mockAuthReturnException(AuthException, secondaryAgentPredicate(mtditid))

          lazy val result: Future[Result] = testAuth.agentAuthentication(testBlock)(
            request = FakeRequest().withSession(fakeRequestWithMtditidAndNino.session.data.toSeq :_*),
            hc = emptyHeaderCarrier
          )

          status(result) shouldBe SEE_OTHER
          redirectUrl(result) shouldBe s"$baseUrl/error/you-need-client-authorisation"
        }

        "handle appropriately when a supporting agent is properly authorised" in new AgentTest {
          mockMultipleAgentsSwitch(true)

          object AuthException extends AuthorisationException("Some reason")
          mockAuthReturnException(AuthException, primaryAgentPredicate(mtditid))
          mockAuthReturn(supportingAgentEnrolment, secondaryAgentPredicate(mtditid))

          lazy val result: Future[Result] = testAuth.agentAuthentication(testBlock)(
            request = FakeRequest().withSession(fakeRequestWithMtditidAndNino.session.data.toSeq :_*),
            hc = validHeaderCarrier
          )

          status(result) shouldBe OK
          bodyOf(result) shouldBe s"$mtditid $arn"
        }
      }

      "results in successful authorisation for a primary agent" should {
        "return a redirect to You Need Agent Services page when an ARN cannot be found" in new AgentTest {
          val primaryAgentEnrolmentNoArn: Enrolments = Enrolments(Set(
            Enrolment(EnrolmentKeys.Individual, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.individualId, mtditid)), "Activated"),
            Enrolment(EnrolmentKeys.Agent, Seq.empty, "Activated")
          ))

          mockAuthReturn(primaryAgentEnrolmentNoArn, primaryAgentPredicate(mtditid))

          lazy val result: Future[Result] = testAuth.agentAuthentication(testBlock)(
            request = FakeRequest().withSession(fakeRequestWithMtditidAndNino.session.data.toSeq :_*),
            hc = validHeaderCarrier
          )

          status(result) shouldBe SEE_OTHER
          redirectUrl(result) shouldBe s"$baseUrl/error/you-need-agent-services-account"
        }

        "return a redirect to Sign In page when a session ID cannot be found" in new AgentTest {
          mockAuthReturn(primaryAgentEnrolment, primaryAgentPredicate(mtditid))

          lazy val result: Future[Result] = testAuth.agentAuthentication(testBlock)(
            request = FakeRequest().withSession(fakeRequestWithMtditidAndNino.session.data.toSeq :_*),
            hc = emptyHeaderCarrier
          )

          status(result) shouldBe SEE_OTHER
          redirectUrl(result) shouldBe s"$baseUrl/signIn"
        }

        "invoke block when the user is properly authenticated" in new AgentTest {
          mockAuthReturn(primaryAgentEnrolment, primaryAgentPredicate(mtditid))

          lazy val result: Future[Result] = testAuth.agentAuthentication(testBlock)(
            request = FakeRequest().withSession(fakeRequestWithMtditidAndNino.session.data.toSeq :_*),
            hc = validHeaderCarrier
          )

          status(result) shouldBe OK
          bodyOf(result) shouldBe s"$mtditid $arn"
        }
      }
    }
  }

  ".invokeBlock" should {

    lazy val block: User[AnyContent] => Future[Result] = user =>
      Future.successful(Ok(s"mtditid: ${user.mtditid}${user.arn.fold("")(arn => " arn: " + arn)}"))

    "perform the block action" when {

      "the user is successfully verified as an agent" which {

        lazy val result = {
          mockAuthAsAgent()
          auth.invokeBlock(fakeRequestWithMtditidAndNino, block)
        }

        "should return an OK(200) status" in {
          status(result) shouldBe OK
          bodyOf(result) shouldBe "mtditid: 1234567890 arn: 0987654321"
        }
      }

      "the user is successfully verified as an individual" in {

        lazy val result = {
          mockAuth(Some("AA123456A"))
          auth.invokeBlock(fakeRequest, block)
        }

        status(result) shouldBe OK

        bodyOf(result) shouldBe "mtditid: 1234567890"
      }
    }

    "return a redirect" when {

      "the authorisation service returns an AuthorisationException exception" in {
        object AuthException extends AuthorisationException("Some reason")

        lazy val result = {
          mockAuthReturnException(AuthException)
          auth.invokeBlock(fakeRequest, block)
        }
        status(result) shouldBe SEE_OTHER
      }

      "there is no MTDITID value in session for an agent" in {
        lazy val result = {

          (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
            .expects(*, Retrievals.affinityGroup, *, *)
            .returning(Future.successful(Some(AffinityGroup.Agent)))

          auth.invokeBlock(fakeRequestWithNino, block)
        }
        status(result) shouldBe SEE_OTHER
        redirectUrl(result) shouldBe "/utr-entry"
      }

    }

    "redirect to the sign in page" when {
      "the authorisation service returns a NoActiveSession exception" in {
        object NoActiveSession extends NoActiveSession("Some reason")

        lazy val result = {
          mockAuthReturnException(NoActiveSession)
          auth.invokeBlock(fakeRequest, block)
        }

        status(result) shouldBe SEE_OTHER
      }
    }

    "return an internal server error page" when {
      "an unexpected error occurs during auth call" in {
        object UnexpectedException extends IndexOutOfBoundsException("Some reason")

        lazy val result = {
          mockAuthReturnException(UnexpectedException)
          mockInternalServerError(InternalServerError("An unexpected error occurred"))
          auth.invokeBlock(fakeRequest, block)
        }
        status(result) shouldBe INTERNAL_SERVER_ERROR
        bodyOf(result) shouldBe "An unexpected error occurred"
      }
    }
  }

}
