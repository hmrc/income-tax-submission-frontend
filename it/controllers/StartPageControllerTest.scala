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

import config.AppConfig
import controllers.predicates.AuthorisedAction
import itUtils.IntegrationTest
import play.api.test.FakeRequest
import play.api.test.Helpers.{OK, SEE_OTHER}
import views.html.StartPage

class StartPageControllerTest extends IntegrationTest {

  private val taxYear = 2022

  lazy val frontendAppConfig: AppConfig = app.injector.instanceOf[AppConfig]

  def controller: StartPageController = new StartPageController(
    app.injector.instanceOf[AuthorisedAction],
    app.injector.instanceOf[StartPage],
    frontendAppConfig,
    mcc
  )

  "Hitting the show endpoint" should {

    s"return an OK (200)" when {

      "all auth requirements are met" in {
        val result = {
          authoriseIndividual()
          await(controller.show(taxYear)(FakeRequest()))
        }

        result.header.status shouldBe OK
      }

    }

    s"redirect to the iv journey" when {

      "the confidence level is too low" which {
        lazy val result = {
          unauthorisedIndividualInsufficientConfidenceLevel()
          await(controller.show(taxYear)(FakeRequest()))
        }

        "has a status of SEE_OTHER (303)" in {
          result.header.status shouldBe SEE_OTHER
        }

        "has the iv journey url as the redirect link" in {
          result.header.headers shouldBe Map("Location" -> "/income-through-software/return/iv-uplift")
        }
      }

    }

    "redirect to the sign in link" when {

      "it contains the wrong credentials" which {
        lazy val result = {
          unauthorisedIndividualWrongCredentials()
          await(controller.show(taxYear)(FakeRequest()))
        }

        "has a status of SEE_OTHER (303)" in {
          result.header.status shouldBe SEE_OTHER
        }

        "has the sign in url as the redirect link" in {
          result.header.headers("Location") shouldBe appConfig.signInUrl
        }
      }

    }

  }

}
