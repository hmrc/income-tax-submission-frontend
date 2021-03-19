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

package controllers.predicates

import common.{EnrolmentIdentifiers, EnrolmentKeys}
import models.User
import play.api.http.Status._
import play.api.mvc.Results._
import play.api.mvc.{AnyContent, Result}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.Retrieval
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.syntax.retrieved.authSyntaxForRetrieved
import uk.gov.hmrc.http.HeaderCarrier
import utils.UnitTest

import scala.concurrent.{ExecutionContext, Future}

class AuthorisedActionSpec extends UnitTest {

  val auth: AuthorisedAction = authorisedAction
  val nino = "AA123456A"

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
        val block: User[AnyContent] => Future[Result] = user => Future.successful(Ok(user.mtditid))
        val mtditid = "AAAAAA"
        val enrolments = Enrolments(Set(Enrolment(EnrolmentKeys.Individual, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.individualId, mtditid)), "Activated")))

        lazy val result: Future[Result] = auth.individualAuthentication[AnyContent](block, enrolments, mtditid, nino)(fakeRequest)

        "returns an OK status" in {
          status(result) shouldBe OK
        }

        "returns a body of the mtditid" in {
          bodyOf(result) shouldBe mtditid
        }
      }

    }

    "return a forbidden" when {

      "the correct enrolment is missing" which {
        val block: User[AnyContent] => Future[Result] = user => Future.successful(Ok(user.mtditid))
        val mtditid = "AAAAAA"
        val enrolments = Enrolments(Set(Enrolment("notAnIndividualOops", Seq(EnrolmentIdentifier(EnrolmentIdentifiers.individualId, mtditid)), "Activated")))

        lazy val result: Future[Result] = auth.individualAuthentication[AnyContent](block, enrolments, mtditid, nino)(fakeRequest)

        "returns a forbidden" in {
          status(result) shouldBe FORBIDDEN
        }
      }

    }

  }

  ".agentAuthenticated" should {

    val block: User[AnyContent] => Future[Result] = user => Future.successful(Ok(s"${user.mtditid} ${user.arn.get}"))

    "perform the block action" when {

      "the agent is authorised for the given user" which {

        val enrolments = Enrolments(Set(
          Enrolment(EnrolmentKeys.Individual, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.individualId, "1234567890")), "Activated"),
          Enrolment(EnrolmentKeys.Agent, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.agentReference, "0987654321")), "Activated")
        ))

        lazy val result = {
          (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
            .expects(*, *, *, *)
            .returning(Future.successful(enrolments))

          auth.agentAuthentication(block, nino)(fakeRequestWithMtditid, emptyHeaderCarrier)
        }

        "has a status of OK" in {
          status(result) shouldBe OK
        }

        "has the correct body" in {
          bodyOf(result) shouldBe "1234567890 0987654321"
        }
      }
    }

    "return an Unauthorised" when {

      "the authorisation service returns an AuthorisationException exception" in {
        object AuthException extends AuthorisationException("Some reason")

        lazy val result = {
          mockAuthReturnException(AuthException)

          auth.agentAuthentication(block, nino)(fakeRequestWithMtditid, emptyHeaderCarrier)
        }
        status(result) shouldBe UNAUTHORIZED
      }

    }

    "redirect to the sign in page" when {

      "the authorisation service returns a NoActiveSession exception" in {
        object NoActiveSession extends NoActiveSession("Some reason")

        lazy val result = {
          mockAuthReturnException(NoActiveSession)
          auth.agentAuthentication(block, nino)(fakeRequestWithMtditid, emptyHeaderCarrier)
        }

        status(result) shouldBe SEE_OTHER
      }
    }

    "return a Forbidden" when {

      "the user does not have an enrolment for the agent" in {
        val enrolments = Enrolments(Set(
          Enrolment(EnrolmentKeys.Individual, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.individualId, "1234567890")), "Activated")
        ))

        lazy val result = {
          (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
            .expects(*, *, *, *)
            .returning(Future.successful(enrolments))
          auth.agentAuthentication(block, nino)(fakeRequestWithMtditid, emptyHeaderCarrier)
        }
        status(result) shouldBe FORBIDDEN
      }
    }

  }

  ".checkAuthorisation" should {

    lazy val block: User[AnyContent] => Future[Result] = user =>
      Future.successful(Ok(s"mtditid: ${user.mtditid}${user.arn.fold("")(arn => " arn: " + arn)}"))

    lazy val enrolments = Enrolments(Set(
      Enrolment(EnrolmentKeys.Individual, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.individualId, "1234567890")), "Activated"),
      Enrolment(EnrolmentKeys.Agent, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.agentReference, "1234567890")), "Activated"),
      Enrolment(EnrolmentKeys.nino, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.ninoId, "AA123456A")), "Activated")
    ))

    "perform the block action" when {

      "the user is authenticated as an individual" which {
        lazy val result = auth.checkAuthorisation(block, enrolments)

        "returns an OK (200) status" in {
          status(result) shouldBe OK
        }

        "returns the correct body" in {
          bodyOf(result) shouldBe "mtditid: 1234567890"
        }
      }

      "the user is authenticated as an agent" which {
        lazy val result = {

          (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
            .expects(*, *, *, *)
            .returning(Future.successful(enrolments))

          auth.checkAuthorisation(block, enrolments, isAgent = true)(fakeRequestWithMtditid, emptyHeaderCarrier)
        }

        "returns an OK (200) status" in {
          status(result) shouldBe OK
        }

        "returns the correct body" in {
          bodyOf(result) shouldBe "mtditid: 1234567890 arn: 1234567890"
        }
      }

    }

    "return an Unauthorised" when {

      "the enrolments do not contain an MTDITID for a user" in {
        lazy val result = auth.checkAuthorisation(block, Enrolments(Set(
          Enrolment(EnrolmentKeys.nino, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.ninoId, "AA123456A")), "Activated")
        )))(fakeRequest.withSession("ClientNino" -> "AA123456A"), emptyHeaderCarrier)

        status(result) shouldBe UNAUTHORIZED
      }

      "the enrolments do not contain an AgentReferenceNumber for an agent" in {
        lazy val result = auth.checkAuthorisation(block, Enrolments(Set.empty[Enrolment]), isAgent = true)(
          fakeRequest.withSession("ClientNino" -> "AA123456A"),
          emptyHeaderCarrier
        )

        status(result) shouldBe UNAUTHORIZED
      }

    }

  }

  ".invokeBlock" should {

    lazy val block: User[AnyContent] => Future[Result] = user =>
      Future.successful(Ok(s"mtditid: ${user.mtditid}${user.arn.fold("")(arn => " arn: " + arn)}"))

    "return the user to IV Uplift" when {
      "the confidence level is invalid" which {

        object ConfidenceLevelError extends NoSuchElementException("Illegal confidence level of 0")
        object AuthError extends NoSuchElementException("Error auth")

        def result(a:NoSuchElementException): Future[Result] = {
          mockAuthReturnException(a)
          auth.invokeBlock(fakeRequest, block)
        }

        "should return a 303" in {
          status(result(ConfidenceLevelError)) shouldBe SEE_OTHER
          await(result(ConfidenceLevelError)).header.headers shouldBe Map("Location" -> "/income-through-software/return/iv-uplift")
        }
        "should return a 401 if different message" in {
          status(result(AuthError)) shouldBe UNAUTHORIZED
        }
      }
    }

    "perform the block action" when {

      "the user is successfully verified as an agent" which {

        lazy val result = {
          mockAuthAsAgent()
          auth.invokeBlock(fakeRequestWithMtditid, block)
        }

        "should return an OK(200) status" in {
          status(result) shouldBe OK
          bodyOf(result) shouldBe "mtditid: 1234567890 arn: 0987654321"
        }
      }

      "the user is successfully verified as an individual" in {

        lazy val result = {
          mockAuth(Some(nino))
          auth.invokeBlock(fakeRequest, block)
        }

        status(result) shouldBe OK

        bodyOf(result) shouldBe "mtditid: 1234567890"
      }
    }

    "return an Unauthorised" when {

      "the authorisation service returns an AuthorisationException exception" in {
        object AuthException extends AuthorisationException("Some reason")

        lazy val result = {
          mockAuthReturnException(AuthException)
          auth.invokeBlock(fakeRequest, block)
        }
        status(result) shouldBe UNAUTHORIZED
      }

      "there is no MTDITID value in session" in {
        lazy val result = {
          lazy val enrolments = Enrolments(Set(
            Enrolment(EnrolmentKeys.Agent, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.agentReference, "0987654321")), "Activated"),
            Enrolment(EnrolmentKeys.nino, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.ninoId, "AA123456A")), "Activated")
          ))
          (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
            .expects(*, Retrievals.allEnrolments and Retrievals.affinityGroup and Retrievals.confidenceLevel, *, *)
            .returning(Future.successful(enrolments and Some(AffinityGroup.Agent) and ConfidenceLevel.L200))

          auth.invokeBlock(fakeRequest.withSession("ClientNino" -> "AA123456A"), block)
        }
        status(result) shouldBe UNAUTHORIZED
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
  }

}
