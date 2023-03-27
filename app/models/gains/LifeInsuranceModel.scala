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

package models.gains

import play.api.libs.json.{Format, Json, OFormat}
import uk.gov.hmrc.crypto.EncryptedValue

case class LifeInsuranceModel(
                               customerReference: Option[String],
                               event: Option[String],
                               gainAmount: BigDecimal,
                               taxPaid: Option[Boolean],
                               yearsHeld: Option[Int],
                               yearsHeldSinceLastGain: Option[Int],
                               deficiencyRelief: Option[BigDecimal]
                             )

object LifeInsuranceModel {
  implicit val formats: OFormat[LifeInsuranceModel] = Json.format[LifeInsuranceModel]
}

case class EncryptedLifeInsuranceModel(
                                        customerReference: Option[EncryptedValue],
                                        event: Option[EncryptedValue],
                                        gainAmount: EncryptedValue,
                                        taxPaid: Option[EncryptedValue],
                                        yearsHeld: Option[EncryptedValue],
                                        yearsHeldSinceLastGain: Option[EncryptedValue],
                                        deficiencyRelief: Option[EncryptedValue]
                             )

object EncryptedLifeInsuranceModel {
  implicit lazy val encryptedValueOFormat: OFormat[EncryptedValue] = Json.format[EncryptedValue]

  implicit val formats: Format[EncryptedLifeInsuranceModel] = Json.format[EncryptedLifeInsuranceModel]
}