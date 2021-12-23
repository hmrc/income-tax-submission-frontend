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

package models.userResearch

import models.userResearch.ResearchUser.{DelegatedEnrolments, Enrolments}

sealed trait CredentialStrength {
  val value: String
}
case object CS_Strong extends CredentialStrength { val value = "strong" }
case object CS_Weak extends CredentialStrength { val value = "weak" }
case object CS_None extends CredentialStrength { val value = "none" }

sealed trait ConfidenceLevel {
  val value: Int
}
case object L50 extends ConfidenceLevel { val value = 50 }
case object L200 extends ConfidenceLevel { val value = 200 }
case object L250 extends ConfidenceLevel { val value = 250 }

sealed trait AffinityGroup {
  val value: String
}
case object AG_Individual extends AffinityGroup { val value = "Individual" }
case object AG_Agent extends AffinityGroup { val value = "Agent" }

case class ResearchUser(taxYear: Int, nino: String, credentialStrength: CredentialStrength, confidenceLevel: ConfidenceLevel, affinityGroup: AffinityGroup, enrolments: Enrolments, delegatedEnrolments: DelegatedEnrolments) {
  def toLoginRequest: AuthLoginRequest = AuthLoginRequest(
    affinityGroup = this.affinityGroup,
    confidenceLevel = this.confidenceLevel,
    credentialStrength = this.credentialStrength,
    enrolments = this.enrolments,
    delegatedEnrolments = Seq.empty,
    nino = Some(nino)
  )
}

object ResearchUser {
  type Enrolments = Seq[Enrolment]
  type DelegatedEnrolments = Seq[DelegatedEnrolment]
}
