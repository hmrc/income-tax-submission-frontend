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

package models

import audit.EnterUpdateAndSubmissionServiceDetail
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.auth.core.AffinityGroup
import utils.UnitTest

class EnterUpdateAndSubmissionServiceDetailSpec extends UnitTest {

  def validModel(input: AffinityGroup): EnterUpdateAndSubmissionServiceDetail = EnterUpdateAndSubmissionServiceDetail(input)

  def validJson(affinityGroup: String): JsObject = Json.obj( "userType" -> affinityGroup )

  "EnterUpdateAndSubmissionServiceDetail" should {

    "write to json when type is individual" in {
      Json.toJson(validModel(AffinityGroup.Individual)) shouldBe validJson("individual")
    }

    "write to json when type is agent" in {
      Json.toJson(validModel(AffinityGroup.Agent)) shouldBe validJson("agent")
    }

    "write to json when type is organisation" in {
      Json.toJson(validModel(AffinityGroup.Organisation)) shouldBe validJson("organisation")
    }

  }

}
