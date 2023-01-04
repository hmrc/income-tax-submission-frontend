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

import play.api.libs.json.Json
import utils.UnitTest

class IVFailureAuditDetailSpec extends UnitTest {

  "writes" when {
    "passed an audit detail model" should {
      "produce valid json" in {
        val json = Json.parse(
          s"""{"ivJourneyId":"68948af0-5d8b-4de9-b070-0650d12fda74"}""".stripMargin)

        val model = IVFailureAuditDetail("68948af0-5d8b-4de9-b070-0650d12fda74")
        Json.toJson(model) shouldBe json
      }
    }
  }
}


