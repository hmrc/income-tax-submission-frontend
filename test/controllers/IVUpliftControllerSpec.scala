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

import config.MockAuditService
import controllers.Assets.SEE_OTHER
import play.api.test.DefaultAwaitTimeout
import play.api.test.Helpers.stubMessagesControllerComponents
import utils.UnitTest

class IVUpliftControllerSpec extends UnitTest with DefaultAwaitTimeout with MockAuditService{

  val controller = new IVUpliftController()(mockAppConfig,stubMessagesControllerComponents,mockAuditService, scala.concurrent.ExecutionContext.Implicits.global)

  "IVUpliftController" should {

    "redirect user to initialiseJourney" when {

      "initialiseJourney() is called it" should {

        verifyAuditEvent
        val response = controller.initialiseJourney()(fakeRequest)

        "return status code 303" in {

          status(response) shouldBe SEE_OTHER
          await(response).header.headers shouldBe Map("Location" ->
            "/mdtp/registration?origin=update-and-submit-income-tax-return&confidenceLevel=200&completionURL=/income-through-software/return/iv-uplift-callback&failureURL=/income-through-software/return/error/we-could-not-confirm-your-details")

        }
      }
    }
    "redirect user to start page" when {

      "callback() is called it" should {

        val response = controller.callback()(fakeRequest)
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
