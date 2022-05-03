/*
 * Copyright 2022 HM Revenue & Customs
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

import audit.{AuditModel, AuditService, IVFailureAuditDetail}
import itUtils.{IntegrationTest, ViewHelpers}
import play.api.http.Status.OK
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.errors.IVFailurePage

import scala.concurrent.Future

class IVFailureControllerSpec extends IntegrationTest with ViewHelpers {

  private val page: IVFailurePage = app.injector.instanceOf[IVFailurePage]
  private val auditService = app.injector.instanceOf[AuditService]
  
  val controller = new IVFailureController()(
    appConfig,stubMessagesControllerComponents, page, auditService, scala.concurrent.ExecutionContext.Implicits.global)

  def event(id: String): AuditModel[IVFailureAuditDetail] =
    AuditModel("LowConfidenceLevelIvOutcomeFail", "LowConfidenceLevelIvOutcomeFail", IVFailureAuditDetail(id))

  "IVFailureController" should {

    "redirect user to failure page" when {

      "the url is called" should {

        def request(id: Option[String]): FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", controllers.routes.IVFailureController.show(id).url)
        def result(id: Option[String] = Some("68948af0-5d8b-4de9-b070-0650d12fda74")): Future[Result] = {
          route(customApp(
            dividendsEnabled = false,
            interestEnabled = false,
            giftAidEnabled = false,
            employmentEnabled = false,
            studentLoansEnabled = false,
            employmentEOYEnabled = false,
            cisEnabled = false,
            crystallisationEnabled = false
          ), request(id)).get
        }
        
        "return status code OK" in {
          status(result()) shouldBe OK
        }
        "return status code OK if no id is supplied" in {
          status(result(None)) shouldBe OK
        }
      }
    }
  }
}
