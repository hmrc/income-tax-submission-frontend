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

package audit

import play.api.libs.json.{Json, OFormat}

case class TailorAddIncomeSourcesDetail(nino: String, mtditid: String, userType: String, taxYear: Int, body: SourcesDetail) {
  private def name = "TailorAddIncomeSources"
  def toAuditModel: AuditModel[TailorAddIncomeSourcesDetail] =  AuditModel[TailorAddIncomeSourcesDetail](name, name, this)
}

object TailorAddIncomeSourcesDetail {
  implicit val formats: OFormat[TailorAddIncomeSourcesDetail] = Json.format[TailorAddIncomeSourcesDetail]
}

case class SourcesDetail(SourcesAdded: Seq[String])
object SourcesDetail {
  implicit val formats: OFormat[SourcesDetail] = Json.format[SourcesDetail]
}

