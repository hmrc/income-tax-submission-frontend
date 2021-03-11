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

import java.util.UUID

import controllers.Assets.OK
import play.api.mvc.Result
import play.api.test.DefaultAwaitTimeout
import play.api.test.Helpers.stubMessagesControllerComponents
import utils.UnitTest
import views.html.errors.IVFailurePage

import scala.concurrent.Future

class IVFailureControllerSpec extends UnitTest with DefaultAwaitTimeout {

  private val page: IVFailurePage = app.injector.instanceOf[IVFailurePage]
  val controller = new IVFailureController()(mockAppConfig,stubMessagesControllerComponents, page, scala.concurrent.ExecutionContext.Implicits.global)

  "IVFailureController" should {

    "redirect user to failure page" when {

      "show() is called it" should {

        def response(id: Option[String] = Some(UUID.randomUUID().toString)): Future[Result] = controller.show(id)(fakeRequest)

        "return status code OK" in {
          status(response()) shouldBe OK
        }
        "return status code OK if no id is supplied" in {
          status(response(None)) shouldBe OK
        }
        "return status code OK if no id is supplied and default to session id" in {
          status(controller.show(None)(fakeRequest.withSession("sessionId" -> "sesh id"))) shouldBe OK
        }
      }
    }
  }
}