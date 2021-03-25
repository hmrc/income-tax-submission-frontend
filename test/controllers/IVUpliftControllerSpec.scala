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

import audit.{AuditModel, IVHandoffAuditDetail, IVSuccessAuditDetail}
import config.MockAuditService
import controllers.Assets.SEE_OTHER
import org.scalamock.handlers.CallHandler
import play.api.test.DefaultAwaitTimeout
import play.api.test.Helpers.stubMessagesControllerComponents
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import utils.UnitTest

import scala.concurrent.Future

class IVUpliftControllerSpec extends UnitTest with DefaultAwaitTimeout with MockAuditService {

  private val controller = new IVUpliftController()(
    mockAppConfig,
    stubMessagesControllerComponents,
    mockAuditService,
    mockAuthService,
    authorisedAction,
    scala.concurrent.ExecutionContext.Implicits.global)

  val individualHandoffReason = "individual"
  val individualConfidenceLevel = 50
  val organisationHandoffReason = "organisation"
  val organisationConfidenceLevel = 100
  val minimumConfidenceLevel = 200

  "IVUpliftController" should {

    "redirect user to initialiseJourney" when {

      "initialiseJourney() is called it" should {

        lazy val individualDetail: IVHandoffAuditDetail =
          IVHandoffAuditDetail(individualHandoffReason, individualConfidenceLevel, minimumConfidenceLevel)
        lazy val organisationDetail: IVHandoffAuditDetail =
          IVHandoffAuditDetail(organisationHandoffReason, organisationConfidenceLevel, minimumConfidenceLevel)

        lazy val individualEvent: AuditModel[IVHandoffAuditDetail] =
          AuditModel("LowConfidenceLevelIvHandoff","LowConfidenceLevelIvHandoff", individualDetail)
        lazy val organisationEvent: AuditModel[IVHandoffAuditDetail] =
          AuditModel("LowConfidenceLevelIvHandoff","LowConfidenceLevelIvHandoff", organisationDetail)

        def verifyIndividualHandoffAudit: CallHandler[Future[AuditResult]] = verifyAuditEvent(individualEvent)
        def verifyOrganisationHandoffAudit: CallHandler[Future[AuditResult]] = verifyAuditEvent(organisationEvent)

        "as an individual return status code 303" in {
          lazy val response = controller.initialiseJourney()(fakeRequest)

          mockIVCredentials(AffinityGroup.Individual, individualConfidenceLevel)
          verifyIndividualHandoffAudit
          status(response) shouldBe SEE_OTHER
          await(response).header.headers shouldBe Map("Location" ->
            "/mdtp/uplift?origin=update-and-submit-income-tax-return&confidenceLevel=200&completionURL=/income-through-software/return/iv-uplift-callback&failureURL=/income-through-software/return/error/we-could-not-confirm-your-details")
        }

        "as an organisation return status code 303" in {
          lazy val response = controller.initialiseJourney()(fakeRequest)

          mockIVCredentials(AffinityGroup.Organisation, organisationConfidenceLevel)
          verifyOrganisationHandoffAudit
          status(response) shouldBe SEE_OTHER
          await(response).header.headers shouldBe Map("Location" ->
            "/mdtp/uplift?origin=update-and-submit-income-tax-return&confidenceLevel=200&completionURL=/income-through-software/return/iv-uplift-callback&failureURL=/income-through-software/return/error/we-could-not-confirm-your-details")
        }
      }
    }
    "redirect user to start page" when {

      "callback() is called it" should {

        def event(nino: String): AuditModel[IVSuccessAuditDetail] =
          AuditModel("LowConfidenceLevelIvOutcomeSuccess", "LowConfidenceLevelIvOutcomeSuccess", IVSuccessAuditDetail(nino))

        def verifyAudit(nino:String): CallHandler[Future[AuditResult]] = verifyAuditEvent(event(nino))

        mockAuth(Some("AA12324AA"))
        verifyAudit("AA12324AA")

        val response = controller.callback()(fakeRequest)

        mockAuth(Some("AA12324AA"))
        verifyAudit("AA12324AA")
        val response2 = controller.callback()(fakeRequest.withSession("TAX_YEAR" -> "2022"))

        "return status code 303" in {
          status(response) shouldBe SEE_OTHER
          await(response).header.headers shouldBe Map("Location" -> "/income-through-software/return/2021/start")
        }
        "return status code 303 when there is a tax year in session" in {
          status(response2) shouldBe SEE_OTHER
          await(response2).header.headers shouldBe Map("Location" -> "/income-through-software/return/2022/start")
        }
      }
    }
  }
}
