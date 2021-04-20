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

import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.auth.core.AffinityGroup
import utils.UnitTest

class EnterUpdateAndSubmissionServiceAuditDetailSpec extends UnitTest {

  def validModel(affinityGroupInput: AffinityGroup, ninoInput : String): EnterUpdateAndSubmissionServiceAuditDetail = EnterUpdateAndSubmissionServiceAuditDetail(affinityGroupInput, ninoInput)

  def validJson(affinityGroup: String, nino: String): JsObject = Json.obj(
    "userType" -> affinityGroup,
    "nino" -> nino
  )

  val ninoEntry: String = "AA111111A"

  "EnterUpdateAndSubmissionServiceAuditDetail" should {

    "write to json when type is individual" in {
      Json.toJson(validModel(AffinityGroup.Individual, ninoEntry)) shouldBe validJson("individual", "AA111111A")
    }

    "write to json when type is agent" in {
      Json.toJson(validModel(AffinityGroup.Agent, ninoEntry)) shouldBe validJson("agent", "AA111111A")
    }

    "write to json when type is organisation" in {
      Json.toJson(validModel(AffinityGroup.Organisation, ninoEntry)) shouldBe validJson("organisation", "AA111111A")
    }

  }

}
