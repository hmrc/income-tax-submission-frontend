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

package connectors

import connectors.httpParsers.SessionDataHttpParser._
import models.sessionData.SessionData
import play.api.Logging
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}


trait IncomeTaxSessionDataConnector {
  def upsert(sessionId: String, sessionData: SessionData)(implicit hc: HeaderCarrier): Future[SessionDataResponse]
}

@Singleton
class IncomeTaxSessionDataConnectorImpl @Inject() (http: HttpClient)(implicit ec: ExecutionContext)
    extends IncomeTaxSessionDataConnector with Logging {

  def upsert(sessionId: String, sessionData: SessionData)(implicit hc: HeaderCarrier): Future[SessionDataResponse] = {
    val url = s"http://localhost:30027/income-tax-session-data"

    println(s"TrackMe:: ${Json.toJson(sessionData)}")

    http.POST[SessionData, SessionDataResponse](url, sessionData).map { res =>
      logger.info(s"Upsert response: $res")
      res
    }
  }
}
