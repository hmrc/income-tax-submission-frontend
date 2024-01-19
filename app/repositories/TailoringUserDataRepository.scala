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

package repositories

import config.AppConfig
import models.mongo.{EncryptedTailoringUserDataModel, TailoringUserDataModel}
import services.EncryptionService
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
@Singleton
class TailoringUserDataRepository @Inject()(
                                             mongo: MongoComponent,
                                             appConfig: AppConfig,
                                             encryptionService: EncryptionService
                                           )(implicit val ec: ExecutionContext) extends PlayMongoRepository[EncryptedTailoringUserDataModel](
  mongoComponent = mongo,
  collectionName = "tailoringUserData",
  domainFormat = EncryptedTailoringUserDataModel.formats,
  indexes = RepositoryIndexes.indexes()(appConfig)
) with UserDataRepository[EncryptedTailoringUserDataModel]{
  override val repoName = "tailoringUserData"
  override type UserData = TailoringUserDataModel
  override def encryptionMethod: TailoringUserDataModel => EncryptedTailoringUserDataModel = encryptionService.encryptTailoringUserData
  override def decryptionMethod: EncryptedTailoringUserDataModel => TailoringUserDataModel = encryptionService.decryptTailoringUserData

}
