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

package audit

import play.api.libs.json.{Json, Writes}
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Individual, Organisation}

case class EnterUpdateAndSubmissionServiceAuditDetail(affinityGroup: AffinityGroup, nino: String) {
  private val name = "EnteredUpdateAndSubmitIncomeTaxReturn"
  def toAuditModel: AuditModel[EnterUpdateAndSubmissionServiceAuditDetail] = AuditModel[EnterUpdateAndSubmissionServiceAuditDetail](name, name, this)
}

object EnterUpdateAndSubmissionServiceAuditDetail {
  implicit val writes: Writes[EnterUpdateAndSubmissionServiceAuditDetail] = Writes[EnterUpdateAndSubmissionServiceAuditDetail] {
    model =>
      Json.obj(
        "userType" -> (model.affinityGroup match {
          case Individual => "individual"
          case Organisation => "organisation"
          case Agent => "agent"
          case _ => "None"
        }),
        "nino" -> model.nino
      )
  }
}
