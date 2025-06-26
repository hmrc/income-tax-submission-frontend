/*
 * Copyright 2024 HM Revenue & Customs
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

import models.userResearch.ResearchUser.Enrolments
import org.apache.commons.lang3.RandomStringUtils
import play.api.libs.json.{Json, Writes}

case class AuthLoginRequest(
                             credId: String = "",
                             affinityGroup: AffinityGroup,
                             confidenceLevel: ConfidenceLevel,
                             credentialStrength: CredentialStrength,
                             enrolments: Enrolments,
                             delegatedEnrolments: Enrolments,
                             nino: Option[String]
                           )

object AuthLoginRequest {
  implicit val writes: Writes[AuthLoginRequest] = Writes[AuthLoginRequest] { model =>
    val credId: String = if(model.credId.nonEmpty) model.credId else RandomStringUtils.secure().nextNumeric(16)
    
    Json.obj(
      "credId" -> credId,
      "affinityGroup" -> model.affinityGroup.value,
      "confidenceLevel" -> model.confidenceLevel.value,
      "credentialStrength" -> model.credentialStrength.value,
      "enrolments" -> model.enrolments.map(_.toJson)
    ) ++ (model.nino.fold(Json.obj())(ninoValue => Json.obj("nino" -> ninoValue)))
  }
}
