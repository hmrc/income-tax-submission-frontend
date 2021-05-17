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

package services

import config.MockIncomeTaxUserDataRepository
import models.{IncomeSourcesModel, User}
import models.mongo.UserData
import play.api.i18n.MessagesApi
import play.api.mvc.Results.InternalServerError
import utils.UnitTest
import views.html.errors.InternalServerErrorPage

class IncomeTaxUserDataServiceSpec extends UnitTest with MockIncomeTaxUserDataRepository{

  val page: InternalServerErrorPage = app.injector.instanceOf[InternalServerErrorPage]
  val messages: MessagesApi = app.injector.instanceOf[MessagesApi]

  val service: IncomeTaxUserDataService = new IncomeTaxUserDataService(mockRepository,page,mockAppConfig,messages)

  ".saveUserData" should {

    "return the repo response" in {

      mockUpdate()

      val result = await(service.saveUserData(
        User(mtditid = "1234567890", arn = None, nino = "AA123456A", sessionId = "sessionId-1618a1e8-4979-41d8-a32e-5ffbe69fac81"),
        2022
      ))

      result shouldBe Right(true)
    }
    "return the repo response when saving all income sources" in {

      val incomeData = IncomeSourcesModel(
        dividendsModel, interestsModel, Some(giftAidModel), Some(employmentsModel)
      )

      mockUpdate()

      val result = await(service.saveUserData(
        User(mtditid = "1234567890", arn = None, nino = "AA123456A", sessionId = "sessionId-1618a1e8-4979-41d8-a32e-5ffbe69fac81"),
        2022,
        Some(incomeData)
      ))

      result shouldBe Right(true)
    }
    "return the repo response when it fails to save" in {

      mockUpdate(false)

      val result = await(service.saveUserData(
        User(mtditid = "1234567890", arn = None, nino = "AA123456A", sessionId = "sessionId-1618a1e8-4979-41d8-a32e-5ffbe69fac81"),
        2022
      ))

      result shouldBe Left(InternalServerError(page()(fakeRequest, messages.preferred(fakeRequest), mockAppConfig)))
    }

  }

}
