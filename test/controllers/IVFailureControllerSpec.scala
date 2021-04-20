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

import audit.{AuditModel, IVFailureAuditDetail}
import config.MockAuditService
import controllers.Assets.OK
import org.scalamock.handlers.CallHandler
import play.api.mvc.Result
import play.api.test.Helpers.stubMessagesControllerComponents
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import utils.UnitTest
import views.html.errors.IVFailurePage

import scala.concurrent.Future

class IVFailureControllerSpec extends UnitTest with MockAuditService{

  private val page: IVFailurePage = app.injector.instanceOf[IVFailurePage]
  val controller = new IVFailureController()(
    mockAppConfig,stubMessagesControllerComponents, page, mockAuditService, scala.concurrent.ExecutionContext.Implicits.global)

  def event(id: String): AuditModel[IVFailureAuditDetail] =
    AuditModel("LowConfidenceLevelIvOutcomeFail", "LowConfidenceLevelIvOutcomeFail", IVFailureAuditDetail(id))

  def verifyAudit(id:String): CallHandler[Future[AuditResult]] = verifyAuditEvent(event(id))

  "IVFailureController" should {

    "redirect user to failure page" when {

      "show() is called it" should {

        def response(id: Option[String] = Some("68948af0-5d8b-4de9-b070-0650d12fda74")): Future[Result] = controller.show(id)(fakeRequest)

        "return status code OK" in {
          verifyAudit("68948af0-5d8b-4de9-b070-0650d12fda74")
          status(response()) shouldBe OK
        }
        "return status code OK if no id is supplied" in {
          verifyAuditEvent
          status(response(None)) shouldBe OK
        }
        "return status code OK if no id is supplied and default to session id" in {
          verifyAudit("sesh id")
          status(controller.show(None)(fakeRequest.withSession("sessionId" -> "sesh id"))) shouldBe OK
        }
      }
    }
  }
}
