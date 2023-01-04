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

package models.userResearch

import models.userResearch.ResearchUser.{DelegatedEnrolments, Enrolments}
import utils.UnitTest

class ResearchUserSpec extends UnitTest {

  val taxYear = 2021
  val nino = "AA000000A"
  val credentialStrength: CredentialStrength = CS_Strong
  val confidenceLevel: ConfidenceLevel = L250
  val affinityGroup: AffinityGroup = AG_Individual
  val enrolments: Enrolments = Seq(
    Enrolment("enrolment1", Seq(Identifier("id1K", "id1V")), Activated)
  )
  val delegatedEnrolments: DelegatedEnrolments = Seq(
    DelegatedEnrolment("enrolment1", Seq(Identifier("id1K", "id1V")), "mtd-auth")
  )

  val validModel: ResearchUser = ResearchUser(
    taxYear = taxYear,
    nino = nino,
    credentialStrength = credentialStrength,
    confidenceLevel = confidenceLevel,
    affinityGroup = affinityGroup,
    enrolments = enrolments,
    delegatedEnrolments = delegatedEnrolments
  )

  val validAuthRequestModel: AuthLoginRequest = AuthLoginRequest(
    "", affinityGroup, confidenceLevel, credentialStrength, enrolments, Seq.empty, Some(nino)
  )

  "ResearchUser .toLoginRequest" should {

    "correctly convert the model to an AuthLoginRequest" in {
      validModel.toLoginRequest shouldBe validAuthRequestModel
    }

  }

}
