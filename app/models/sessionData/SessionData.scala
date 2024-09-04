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

package models.sessionData

import play.api.libs.json.{Json, OFormat}

final case class SessionData(sessionID: String,
                             mtditid: Option[String],
                             nino: Option[String],
                             saUtr: Option[String],
                             clientFirstName: Option[String],
                             clientLastName: Option[String],
                             userType: Option[String])

object SessionData {
  implicit val formats: OFormat[SessionData] = Json.format[SessionData]

  def mkSessionData(sessionId: String, queryParams: Map[String, Seq[String]]): SessionData =
    SessionData(
      sessionID = sessionId,
      mtditid = queryParams.get("ClientMTDID").flatMap(_.headOption),
      nino = queryParams.get("ClientNino").flatMap(_.headOption),
      saUtr = queryParams.get("saUlt").flatMap(_.headOption).orElse(Some("Placeholder")),
      clientFirstName = queryParams.get("clientFirstName").flatMap(_.headOption).orElse(Some("Placeholder")),
      clientLastName = queryParams.get("clientLastName").flatMap(_.headOption).orElse(Some("Placeholder")),
      userType = queryParams.get("userType").flatMap(_.headOption).orElse(Some("Placeholder"))
    )
}
