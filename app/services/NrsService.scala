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

package services

import connectors.NrsConnector
import connectors.httpParsers.NrsSubmissionHttpParser.NrsSubmissionResponse
import play.api.http.HeaderNames
import play.api.libs.json.Writes
import play.api.mvc.Request
import uk.gov.hmrc.http.HeaderCarrier
import utils.HMRCHeaderNames

import javax.inject.Inject
import scala.concurrent.Future

class NrsService @Inject() (nrsConnector: NrsConnector) {

  def submit[A](nino: String, nrsSubmissionModel: A, mtditid: String, notableEvent: String)
               (implicit request: Request[_], hc: HeaderCarrier, writes: Writes[A]): Future[NrsSubmissionResponse] = {
    
    val extraHeaders = Seq(
      Some("mtditid" -> mtditid),
      Some(HeaderNames.USER_AGENT -> "income-tax-submission-frontend"),
      Some(HMRCHeaderNames.TrueUserAgent -> request.headers.get(HeaderNames.USER_AGENT).getOrElse("No user agent provided")),
      hc.trueClientIp.map(ip => "clientIP" -> ip),
      hc.trueClientPort.map(port => "clientPort" -> port)
    ).flatten
    nrsConnector.postNrsConnector(nino, nrsSubmissionModel, notableEvent)(hc.withExtraHeaders(extraHeaders: _*), writes)
  }

}
