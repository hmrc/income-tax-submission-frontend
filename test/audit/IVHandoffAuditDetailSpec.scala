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

import utils.UnitTest
import play.api.libs.json.Json

class IVHandoffAuditDetailSpec extends UnitTest {

  private val handoffReason = "individual"
  private val currentConfidenceLevel = 50
  private val minimumConfidenceLevelRequired = 200


  "writes" when {
    "passed an audit detail model with Iv Uplift Handoff Details" should {
      "produce valid json" in {
        val json = Json.obj(
          "reasonForHandoff" -> handoffReason,
          "currentConfidenceLevel" -> currentConfidenceLevel,
          "minimumConfidenceLevelToProceed" -> minimumConfidenceLevelRequired
        )

        val model = IVHandoffAuditDetail(handoffReason, currentConfidenceLevel, minimumConfidenceLevelRequired)
        Json.toJson(model) shouldBe json
      }
    }
  }
}


