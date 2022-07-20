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

import audit.AuditService
import controllers.predicates.AuthorisedAction
import helpers.WireMockHelper
import itUtils.IntegrationTest
import play.api.http.Status.SEE_OTHER
import play.api.test.Helpers.{status, stubMessagesControllerComponents}
import services.AuthService
import uk.gov.hmrc.http.SessionKeys

class IVUpliftControllerISpec extends IntegrationTest with WireMockHelper {

  private def controller = new IVUpliftController()(
    appConfig,
    stubMessagesControllerComponents,
    app.injector.instanceOf[AuditService],
    app.injector.instanceOf[AuthService],
    app.injector.instanceOf[AuthorisedAction],
    scala.concurrent.ExecutionContext.Implicits.global)

  private val callBackTaxYear: Int = taxYear + 2

  "IVUpliftController" should {
    "redirect user to initialiseJourney" when {
      "initialiseJourney() is called it" should {
        "as an individual return status code 303" in {
          lazy val response = {
            authoriseIndividual()
            controller.initialiseJourney()(fakeRequest.withSession(SessionKeys.authToken -> "mock-bearer-token"))
          }

          status(response) shouldBe SEE_OTHER
          await(response).header.headers shouldBe Map("Location" ->
            "http://localhost:9538/mdtp/uplift?origin=update-and-submit-income-tax-return&confidenceLevel=200&completionURL=/update-and-submit-income-tax-return/iv-uplift-callback&failureURL=/update-and-submit-income-tax-return/error/we-could-not-confirm-your-details")
        }

      }
    }

    "redirect user to start page" when {

      "callback() is called it" should {
        lazy val response = {
          authoriseIndividual()
          controller.callback()(fakeRequest.withSession(SessionKeys.authToken -> "mock-bearer-token"))
        }

        lazy val response2 = {
          authoriseIndividual()
          controller.callback()(fakeRequest.withSession(SessionKeys.authToken -> "mock-bearer-token", "TAX_YEAR" -> s"$callBackTaxYear"))
        }

        "return status code 303" in {
          status(response) shouldBe SEE_OTHER
          await(response).header.headers shouldBe Map("Location" -> s"/update-and-submit-income-tax-return/$taxYear/start")
        }
        "return status code 303 when there is a tax year in session" in {
          status(response2) shouldBe SEE_OTHER
          await(response2).header.headers shouldBe Map("Location" -> s"/update-and-submit-income-tax-return/$callBackTaxYear/start")
        }
      }
    }
  }
}
