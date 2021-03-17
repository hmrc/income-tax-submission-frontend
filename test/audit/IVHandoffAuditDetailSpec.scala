
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
        Json.toJson(model) shouldBe json
      }
    }
  }
}


