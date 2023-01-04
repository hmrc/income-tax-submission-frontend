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

object ResearchUsers {

  def generateUserCredentials(nino: String, suppliedYear: Int): ResearchUser = ResearchUser(
    taxYear = suppliedYear,
    nino = nino,
    credentialStrength = CS_Strong,
    confidenceLevel = L250,
    affinityGroup = AG_Individual,
    enrolments = Seq(Enrolment("HMRC-MTD-IT", Seq(Identifier("MTDITID", "1234567890")))),
    delegatedEnrolments = Seq(DelegatedEnrolment("HMRC-MTD-IT", Seq(Identifier("MTDITID", "1234567890")), "mtd-auth"))
  )

}
