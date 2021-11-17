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
import itUtils.IntegrationTest
import models.User
import org.scalatest.events.TestFailed
import play.api.http.Status._
import play.api.mvc.Results._
import play.api.mvc.{AnyContent, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.status
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.syntax.retrieved.authSyntaxForRetrieved

import scala.concurrent.Future

class AuthorisedActionSpec extends IntegrationTest {
  
  val nino = "AA123456A"
  lazy val auth: AuthorisedAction = app.injector.instanceOf[AuthorisedAction]

  ".enrolmentGetIdentifierValue" should {

    "return the value for the given identifier" in {
      val returnValue = "anIdentifierValue"
      val returnValueAgent = "anAgentIdentifierValue"

      val enrolments = Enrolments(Set(
        Enrolment(EnrolmentKeys.Individual, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.individualId, returnValue)), "Activated"),
        Enrolment(EnrolmentKeys.Agent, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.agentReference, returnValueAgent)), "Activated")
      ))

      authAction(Future.successful(None)).enrolmentGetIdentifierValue(EnrolmentKeys.Individual, EnrolmentIdentifiers.individualId, enrolments) shouldBe Some(returnValue)
      authAction(Future.successful(None)).enrolmentGetIdentifierValue(EnrolmentKeys.Agent, EnrolmentIdentifiers.agentReference, enrolments) shouldBe Some(returnValueAgent)
    }

    "return a None" when {
      val key = "someKey"
      val identifierKey = "anIdentifier"
      val returnValue = "anIdentifierValue"

      val enrolments = Enrolments(Set(Enrolment(key, Seq(EnrolmentIdentifier(identifierKey, returnValue)), "someState")))


      "the given identifier cannot be found" in {
        authAction(Future.successful(None)).enrolmentGetIdentifierValue(key, "someOtherIdentifier", enrolments) shouldBe None
      }

      "the given key cannot be found" in {
        authAction(Future.successful(None)).enrolmentGetIdentifierValue("someOtherKey", identifierKey, enrolments) shouldBe None
      }

    }

  }

  ".individualAuthentication" should {

    "perform the block action" when {

      "the correct enrolment exist" which {
        val block: User[AnyContent] => Future[Result] = user => Future.successful(Ok(user.mtditid))
        val mtditid = "1234567890"
        val enrolments = Enrolments(Set(
          Enrolment(EnrolmentKeys.Individual, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.individualId, mtditid)), "Activated"),
          Enrolment(EnrolmentKeys.nino, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.ninoId, "AA123456A")), "Activated")
        ))

        lazy val result: Future[Result] = {
          val retrieval = Future.successful(enrolments and ConfidenceLevel.L200)
          authAction(retrieval).individualAuthentication[AnyContent](block)(fakeRequest, headerCarrierWithSession)
        }

        "returns an OK status" in {
          status(result) shouldBe OK
        }

        "returns a body of the mtditid" in {
          bodyOf(result) shouldBe mtditid
        }
      }

      "the fallback session id header is populated" which {
        val block: User[AnyContent] => Future[Result] = user => Future.successful(Ok(user.mtditid))
        val mtditid = "1234567890"
        val enrolments = Enrolments(Set(
          Enrolment(EnrolmentKeys.Individual, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.individualId, mtditid)), "Activated"),
          Enrolment(EnrolmentKeys.nino, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.ninoId, "AA123456A")), "Activated")
        ))

        lazy val result: Future[Result] = {
          val retrievals = Future.successful(enrolments and ConfidenceLevel.L200)
          authAction(retrievals).individualAuthentication[AnyContent](block)(fallBackSessionIdFakeRequest, emptyHeaderCarrier)
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

      "the session id is missing" which {
        val block: User[AnyContent] => Future[Result] = user => Future.successful(Ok(user.mtditid))
        val mtditid = "1234567890"
        val enrolments = Enrolments(Set(
          Enrolment(EnrolmentKeys.Individual, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.individualId, mtditid)), "Activated"),
          Enrolment(EnrolmentKeys.nino, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.ninoId, "AA123456A")), "Activated")
        ))

        lazy val result: Future[Result] = {
          val retrievals = Future.successful(enrolments and ConfidenceLevel.L200)
          authAction(retrievals).individualAuthentication[AnyContent](block)(FakeRequest(), emptyHeaderCarrier)
        }

        "returns an SEE_OTHER status" in {
          status(result) shouldBe SEE_OTHER
        }
      }

      "the individual enrolment is missing" which {
        val block: User[AnyContent] => Future[Result] = user => Future.successful(Ok(user.mtditid))
        val mtditid = "AAAAAA"
        val enrolments = Enrolments(Set(Enrolment("notAnIndividualOops", Seq(EnrolmentIdentifier(EnrolmentIdentifiers.individualId, mtditid)), "Activated")))

        lazy val result: Future[Result] = {
          val retrievals = Future.successful(enrolments and ConfidenceLevel.L200)
          authAction(retrievals).individualAuthentication[AnyContent](block)(fakeRequest, headerCarrierWithSession)
        }

        "returns an Unauthorised" in {
          status(result) shouldBe SEE_OTHER
        }

        "has the correct redirect url" in {
          redirectUrl(result) shouldBe appConfig.signInUrl
        }
      }

      "the individual enrolment is missing but there is a nino" which {
        val block: User[AnyContent] => Future[Result] = user => Future.successful(Ok(user.mtditid))
        val nino = "AA123456A"
        val enrolments = Enrolments(Set(Enrolment("HMRC-NI", Seq(EnrolmentIdentifier(EnrolmentIdentifiers.ninoId, nino)), "Activated")))

        lazy val result: Future[Result] = {
          val retrievals = Future.successful(enrolments and ConfidenceLevel.L200)
          authAction(retrievals).individualAuthentication[AnyContent](block)(fakeRequest, headerCarrierWithSession)
        }

        "returns an Unauthorised" in {
          status(result) shouldBe SEE_OTHER
        }
        "returns an redirect to the correct page" in {
          redirectUrl(result) shouldBe "/update-and-submit-income-tax-return/error/you-need-to-sign-up"
        }
      }

    }

    "return the user to IV Uplift" when {

      "the confidence level is below minimum" which {
        val block: User[AnyContent] => Future[Result] = user => Future.successful(Ok(user.mtditid))
        val mtditid = "1234567890"
        val enrolments = Enrolments(Set(
          Enrolment(EnrolmentKeys.Individual, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.individualId, mtditid)), "Activated"),
          Enrolment(EnrolmentKeys.nino, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.ninoId, "AA123456A")), "Activated")
        ))

        lazy val result: Future[Result] = {
          val retrievals = Future.successful(enrolments and ConfidenceLevel.L50)
          authAction(retrievals).individualAuthentication[AnyContent](block)(fakeRequest, headerCarrierWithSession)
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

  ".agentAuthenticated" should {

    val block: User[AnyContent] => Future[Result] = user => Future.successful(Ok(s"${user.mtditid} ${user.arn.get}"))

    "perform the block action" when {

      "the agent is authorised for the given user" which {

        val enrolments = Enrolments(Set(
          Enrolment(EnrolmentKeys.Individual, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.individualId, "1234567890")), "Activated"),
          Enrolment(EnrolmentKeys.Agent, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.agentReference, "0987654321")), "Activated")
        ))

        lazy val result = {
          val retrievals = Future.successful(enrolments)
          authAction(retrievals).agentAuthentication(block)(fakeRequestAgent, headerCarrierWithSession)
        }

        "has a status of OK" in {
          status(result) shouldBe OK
        }

        "has the correct body" in {
          bodyOf(result) shouldBe "1234567890 0987654321"
        }
      }
    }

    "redirect to the enter UTR page on VAT V&C" when {

      "there is no MTDITID in session" which {

        lazy val result = {
          authAction(Future.successful(None)).agentAuthentication(block)(fakeRequestAgentNoMtditid, headerCarrierWithSession)
        }

        "has a status of SEE_OTHER(303)" in {
          status(result) shouldBe SEE_OTHER
        }

        "has the correct redirect url" in {
          redirectUrl(result) shouldBe appConfig.viewAndChangeEnterUtrUrl
        }

      }

      "there is no NINO in session" which {

        lazy val result = {
          authAction(Future.successful(None)).agentAuthentication(block)(fakeRequestAgentNoNino, headerCarrierWithSession)
        }

        "has a status of SEE_OTHER(303)" in {
          status(result) shouldBe SEE_OTHER
        }

        "has the correct redirect url" in {
          redirectUrl(result) shouldBe appConfig.viewAndChangeEnterUtrUrl
        }

      }

    }

    "return an SEE_OTHER" when {

      "the session id is missing" which {

        val enrolments: Enrolments = Enrolments(Set(
          Enrolment(EnrolmentKeys.Individual, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.individualId, "1234567890")), "Activated"),
          Enrolment(EnrolmentKeys.Agent, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.agentReference, "0987654321")), "Activated")
        ))

        lazy val result = {
          authoriseAgent()
          auth.agentAuthentication(block)(fakeRequestAgent, emptyHeaderCarrier)
        }

        "has a status of SEE_OTHER" in {
          status(result) shouldBe SEE_OTHER
        }
      }

      "the authorisation service returns an AuthorisationException exception" in {
        lazy val result = {
          unauthorisedIndividualWrongCredentials()
          auth.agentAuthentication(block)(fakeRequestAgent, headerCarrierWithSession)
        }
        status(result) shouldBe SEE_OTHER
      }

    }

    "redirect to the sign in page" when {

      "the authorisation service returns a NoActiveSession exception" in {
        lazy val result = {
          unauthorisedInactiveSession()
          auth.agentAuthentication(block)(fakeRequestAgent, headerCarrierWithSession)
        }

        status(result) shouldBe SEE_OTHER
        redirectUrl(result) shouldBe appConfig.signInUrl
      }
    }


    "return a redirect to the you need agent services page" when {

      "the user does not have an enrolment for the agent" in {
        val enrolments = Enrolments(Set(
          Enrolment(EnrolmentKeys.Individual, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.individualId, "1234567890")), "Activated")
        ))

        lazy val result = {
          authAction(Future.successful(enrolments)).agentAuthentication(block)(fakeRequestAgent, headerCarrierWithSession)
        }
        status(result) shouldBe SEE_OTHER
      }
    }

  }

  ".invokeBlock" should {
    lazy val block: User[AnyContent] => Future[Result] = user =>
      Future.successful(Ok(s"mtditid: ${user.mtditid}${user.arn.fold("")(arn => " arn: " + arn)}"))

    "perform the block action" when {

      "the user is successfully verified as an agent" which {

        lazy val result = {
          authoriseAgent()
          auth.invokeBlock(fakeRequestAgent, block)
        }

        "should return an OK(200) status" in {
          status(result) shouldBe OK
          bodyOf(result) shouldBe "mtditid: 1234567890 arn: XARN1234567"
        }
      }

      "the user is successfully verified as an individual" in {

        lazy val result = {
          authoriseIndividual()
          auth.invokeBlock(fakeRequest, block)
        }

        status(result) shouldBe OK

        bodyOf(result) shouldBe "mtditid: 1234567890"
      }
    }

    "return a redirect" when {

      "the authorisation service returns an AuthorisationException exception" in {
        lazy val result = {
          unauthorisedAuthorisationException()
          auth.invokeBlock(fakeRequest, block)
        }
        status(result) shouldBe SEE_OTHER
        TestFailed
      }

      "there is no MTDITID value in session" which {
        lazy val result = {
          authoriseAgent()
          auth.invokeBlock(fakeRequest.withSession("ClientNino" -> "AA123456A"), block)
        }

        "has a status of SEE_OTHER (303)" in {
          status(result) shouldBe SEE_OTHER
        }

        "redirects to the view and change 'enter utr' page" in {
          redirectUrl(result) shouldBe "http://localhost:9081/report-quarterly/income-and-expenses/view/agents/client-utr"
        }
      }
    }
    
  }
}
