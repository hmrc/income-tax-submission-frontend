/*
 * Copyright 2024 HM Revenue & Customs
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

import itUtils.IntegrationTest
import repositories.TailoringUserDataRepository

class TailoringSessionServiceISpec extends IntegrationTest {

  val tailoringUserDataRepository: TailoringUserDataRepository = app.injector.instanceOf[TailoringUserDataRepository]
  val tailoringSessionService: TailoringSessionService = new TailoringSessionService(tailoringUserDataRepository)
  val tailoringSessionServiceInvalidEncryption: TailoringSessionService = customApp(invalidEncryptionKey = true).injector.instanceOf[TailoringSessionService]

  val tailoringDataModel: Seq[String] = Seq("interest", "dividends", "employment")


  "create" should {
    "return false when it fails to decrypt the model" in {

      val result = await(tailoringSessionServiceInvalidEncryption.createSessionData(tailoringDataModel, taxYear)(false)(true))

      result shouldBe false
    }

    "return true when successful and false when adding a duplicate" in {

      await(tailoringUserDataRepository.collection.drop().toFuture())
      await(tailoringUserDataRepository.ensureIndexes)

      val initialResult = await(tailoringSessionService.createSessionData(tailoringDataModel, taxYear)(false)(true))
      val duplicateResult = await(tailoringSessionService.createSessionData(tailoringDataModel, taxYear)(false)(true))

      initialResult shouldBe true
      duplicateResult shouldBe false
    }
  }

  "update" should {
    "return false when failing to decrypt the model" in {

      val result = await(tailoringSessionServiceInvalidEncryption.updateSessionData(tailoringDataModel, taxYear)(false)(true))

      result shouldBe false
    }
  }

}
