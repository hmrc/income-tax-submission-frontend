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

package audit

import play.api.libs.json.{Json, OWrites}

case class IVHandoffAuditDetail(reasonForHandoff: String,
                                currentConfidenceLevel: Int,
                                minimumConfidenceLevelToProceed: Int) {

  private def name = "LowConfidenceLevelIvHandoff"
  def toAuditModel: AuditModel[IVHandoffAuditDetail] = AuditModel(name, name, this)

  val IVModel: Map[String, String] = Map(
    "reasonForHandoff" -> reasonForHandoff,
    "currentConfidenceLevel" -> s"CL$currentConfidenceLevel",
    "minimumConfidenceLevelToProceed" -> s"CL$minimumConfidenceLevelToProceed"
  )
}

object IVHandoffAuditDetail {
  implicit def writes: OWrites[IVHandoffAuditDetail] = Json.writes[IVHandoffAuditDetail]
}
