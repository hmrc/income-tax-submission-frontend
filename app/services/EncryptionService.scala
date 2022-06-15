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

import config.AppConfig
import models.mongo._
import models.{EncryptedExcludeJourneyModel, ExcludeJourneyModel}
import utils.SecureGCMCipher

import javax.inject.Inject

class EncryptionService @Inject()(encryptionService: SecureGCMCipher, appConfig: AppConfig) {

  def encryptTailoringUserData(tailoringUserDataModel: TailoringUserDataModel): EncryptedTailoringUserDataModel = {
    implicit val textAndKey: TextAndKey = TextAndKey(tailoringUserDataModel.nino, appConfig.encryptionKey)

    EncryptedTailoringUserDataModel(
      nino = tailoringUserDataModel.nino,
      taxYear = tailoringUserDataModel.taxYear,
      tailoring = tailoringUserDataModel.tailoring.map(encryptionService.encrypt),
      lastUpdated = tailoringUserDataModel.lastUpdated
    )
  }

  def decryptTailoringUserData(encryptedTailoringUserDataModel: EncryptedTailoringUserDataModel): TailoringUserDataModel = {
    implicit val textAndKey: TextAndKey = TextAndKey(encryptedTailoringUserDataModel.nino, appConfig.encryptionKey)

    TailoringUserDataModel(
      nino = encryptedTailoringUserDataModel.nino,
      taxYear = encryptedTailoringUserDataModel.taxYear,
      tailoring = encryptedTailoringUserDataModel.tailoring.map(x => encryptionService.decrypt[String](x.value, x.nonce)),
      lastUpdated = encryptedTailoringUserDataModel.lastUpdated
    )
  }

  def encryptExclusionUserData(exclusionUserDataModel: ExclusionUserDataModel): EncryptedExclusionUserDataModel = {
    implicit val textAndKey: TextAndKey = TextAndKey(exclusionUserDataModel.nino, appConfig.encryptionKey)

    EncryptedExclusionUserDataModel(
      nino = exclusionUserDataModel.nino,
      taxYear = exclusionUserDataModel.taxYear,
      exclusionModel = exclusionUserDataModel.exclusionModel.map(encryptExcludeJourneyModel),
      lastUpdated = exclusionUserDataModel.lastUpdated
    )
  }

  def decryptExclusionUserData(encryptedExclusionUserDataModel: EncryptedExclusionUserDataModel): ExclusionUserDataModel = {
    implicit val textAndKey: TextAndKey = TextAndKey(encryptedExclusionUserDataModel.nino, appConfig.encryptionKey)

    ExclusionUserDataModel(
      nino = encryptedExclusionUserDataModel.nino,
      taxYear = encryptedExclusionUserDataModel.taxYear,
      exclusionModel = encryptedExclusionUserDataModel.exclusionModel.map(decryptExcludeJourneyModel),
      lastUpdated = encryptedExclusionUserDataModel.lastUpdated
    )
  }

  private def encryptExcludeJourneyModel(model: ExcludeJourneyModel)
                                     (implicit textAndKey: TextAndKey): EncryptedExcludeJourneyModel ={
    EncryptedExcludeJourneyModel(
      encryptionService.encrypt[String](model.journey),
      model.hash.map(x => encryptionService.encrypt[String](x))
    )
  }
  private def decryptExcludeJourneyModel(model: EncryptedExcludeJourneyModel)
                                     (implicit textAndKey: TextAndKey): ExcludeJourneyModel ={
    ExcludeJourneyModel(
      encryptionService.decrypt[String](model.journey.value, model.journey.nonce),
      model.hash.map(x => encryptionService.decrypt[String](x.value, x.nonce))
    )
  }
}
