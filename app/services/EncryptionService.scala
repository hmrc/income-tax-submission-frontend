/*
 * Copyright 2023 HM Revenue & Customs
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

import models.mongo._
import utils.AesGcmAdCrypto
import utils.CypherSyntax.{DecryptableOps, EncryptableOps}

import javax.inject.Inject

class EncryptionService @Inject()(implicit val encryptionService: AesGcmAdCrypto) {

  def encryptTailoringUserData(tailoringUserDataModel: TailoringUserDataModel): EncryptedTailoringUserDataModel = {
    implicit val associatedText: String = tailoringUserDataModel.nino

    EncryptedTailoringUserDataModel(
      nino = tailoringUserDataModel.nino,
      taxYear = tailoringUserDataModel.taxYear,
      tailoring = tailoringUserDataModel.tailoring.map(_.encrypted),
      lastUpdated = tailoringUserDataModel.lastUpdated
    )
  }

  def decryptTailoringUserData(encryptedTailoringUserDataModel: EncryptedTailoringUserDataModel): TailoringUserDataModel = {
    implicit val associatedText: String = encryptedTailoringUserDataModel.nino

    TailoringUserDataModel(
      nino = encryptedTailoringUserDataModel.nino,
      taxYear = encryptedTailoringUserDataModel.taxYear,
      tailoring = encryptedTailoringUserDataModel.tailoring.map(_.decrypted[String]),
      lastUpdated = encryptedTailoringUserDataModel.lastUpdated
    )
  }
}
