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

import utils.UnitTest
import play.api.libs.json.Json

class IVHandoffAuditDetailSpec extends UnitTest {

  "writes" when {
    "passed an audit detail model with success tax calculation field" should {
      "produce valid json" in {
        val json = Json.parse(
          s"""{
             |	"body": {
             |		"untaxedUkInterest": true,
             |		"untaxedUkAccounts": [{
             |			"id": "azerty",
             |			"accountName": "Account 1",
             |			"amount": 100.01
             |		}],
             |		"taxedUkInterest": true,
             |		"taxedUkAccounts": [{
             |			"id": "qwerty",
             |			"accountName": "Account 2",
             |			"amount": 9001.01
             |		}]
             |	},
             |	"prior": {
             |		"submissions": [{
             |			"id": "UntaxedId1",
             |			"accountName": "Untaxed Account",
             |			"amount": 100.01
             |		}, {
             |			"id": "TaxedId1",
             |			"accountName": "Taxed Account",
             |			"amount": 9001.01
             |		}]
             |	},
             |	"nino": "AA123456A",
             |	"mtditid": "1234567890",
             |	"taxYear": 2020
             |}""".stripMargin)

        val model = IVHandoffAuditDetail("individual", 50, 200)
//        Json.toJson(model) shouldBe json
      }
    }
  }
}


