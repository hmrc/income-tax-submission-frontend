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

package models.mongo

import org.joda.time.{DateTime, DateTimeZone}
import play.api.libs.json.{Format, Json, OFormat}
import uk.gov.hmrc.mongo.play.json.formats.MongoJodaFormats
import utils.EncryptedValue


case class TailoringUserDataModel(
                                   nino: String,
                                   taxYear: Int,
                                   tailoring: Seq[String],
                                   lastUpdated: DateTime = DateTime.now(DateTimeZone.UTC)
                                 ) extends UserDataTemplate

object TailoringUserDataModel extends MongoJodaFormats {
  implicit val mongoJodaDateTimeFormats: Format[DateTime] = dateTimeFormat

  implicit lazy val formats: OFormat[TailoringUserDataModel] = Json.format[TailoringUserDataModel]

}

case class EncryptedTailoringUserDataModel(
                                            nino: String,
                                            taxYear: Int,
                                            tailoring: Seq[EncryptedValue],
                                            lastUpdated: DateTime = DateTime.now(DateTimeZone.UTC)
                                 ) extends UserDataTemplate

object EncryptedTailoringUserDataModel extends MongoJodaFormats {
  implicit val mongoJodaDateTimeFormats: Format[DateTime] = dateTimeFormat

  implicit lazy val formats: OFormat[EncryptedTailoringUserDataModel] = Json.format[EncryptedTailoringUserDataModel]
}
