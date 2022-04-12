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

package services

import itUtils.IntegrationTest
import models.mongo.{EncryptedTailoringUserDataModel, TailoringUserDataModel}
import utils.SecureGCMCipher

class EncryptionServiceISpec extends IntegrationTest{

  val service: EncryptionService = app.injector.instanceOf[EncryptionService]
  val encryption: SecureGCMCipher = app.injector.instanceOf[SecureGCMCipher]

  "encryptTailoringUserData" should {
    val nino: String = "AA123456A"
    val tailoring = List("item")
    val data = TailoringUserDataModel(nino, taxYear, tailoring)

    "encrypt all the user data apart from the look up ids and timestamp" in {
      val result = service.encryptTailoringUserData(data)
      result shouldBe EncryptedTailoringUserDataModel(
        nino = data.nino,
        taxYear = data.taxYear,
        tailoring = result.tailoring,
        lastUpdated = data.lastUpdated
      )
    }

    "encrypt the data and decrypt it back to the initial model" in {
      val encryptResult = service.encryptTailoringUserData(data)
      val decryptResult = service.decryptTailoringUserData(encryptResult)

      decryptResult shouldBe data
    }
  }

}