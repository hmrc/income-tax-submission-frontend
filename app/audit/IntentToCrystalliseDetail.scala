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

import play.api.libs.json.{Json, OFormat}

case class IntentToCrystalliseDetail(taxYear: Int, userType: String, nino: String, mtditid: String) {
  private def name = "CreateIntentToCrystallise"
  def toAuditModel: AuditModel[IntentToCrystalliseDetail] =  AuditModel[IntentToCrystalliseDetail](name, name, this)
}

object IntentToCrystalliseDetail {
  implicit val format: OFormat[IntentToCrystalliseDetail] = Json.format[IntentToCrystalliseDetail]
}