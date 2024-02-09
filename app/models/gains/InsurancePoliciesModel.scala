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

package models.gains

import play.api.libs.json.{Format, Json, OFormat}
import uk.gov.hmrc.crypto.EncryptedValue

case class InsurancePoliciesModel(submittedOn: Option[String],
                                  lifeInsurance: Option[Seq[LifeInsuranceModel]],
                                  capitalRedemption: Option[Seq[CapitalRedemptionModel]],
                                  lifeAnnuity: Option[Seq[LifeAnnuityModel]],
                                  voidedIsa: Option[Seq[VoidedIsaModel]],
                                  foreign: Option[Seq[ForeignModel]])

object InsurancePoliciesModel {
  implicit val formats: OFormat[InsurancePoliciesModel] = Json.format[InsurancePoliciesModel]
}

case class EncryptedInsurancePoliciesModel(submittedOn: Option[EncryptedValue],
                                           lifeInsurance: Option[Seq[EncryptedLifeInsuranceModel]],
                                           capitalRedemption: Option[Seq[EncryptedCapitalRedemptionModel]],
                                           lifeAnnuity: Option[Seq[EncryptedLifeAnnuityModel]],
                                           voidedIsa: Option[Seq[EncryptedVoidedIsaModel]],
                                           foreign: Option[Seq[EncryptedForeignModel]])

object EncryptedInsurancePoliciesModel {
  implicit lazy val encryptedValueOFormat: OFormat[EncryptedValue] = Json.format[EncryptedValue]

  implicit val formats: Format[EncryptedInsurancePoliciesModel] = Json.format[EncryptedInsurancePoliciesModel]
}