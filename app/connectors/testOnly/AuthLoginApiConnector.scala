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

package connectors.testOnly

import config.AppConfig
import models.userResearch.{AuthLoginAPIResponse, ResearchUser}
import play.api.http.HeaderNames
import play.api.http.Status.CREATED
import play.api.libs.json.Json
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReadsInstances, HttpResponse, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AuthLoginApiConnector @Inject()(appConfig: AppConfig, http: HttpClientV2)(implicit ec: ExecutionContext) extends HttpReadsInstances {
  
  def submitLoginRequest(user: ResearchUser)(implicit hc: HeaderCarrier): Future[Option[AuthLoginAPIResponse]] = {
    val url = s"${appConfig.testOnly_authLoginUrl}/government-gateway/session/login"

    http.post(url"$url")
      .withBody(Json.toJson(user.toLoginRequest))
      .execute[HttpResponse]
      .map { response =>
      response.status match {
        case `CREATED` =>
          (response.header(HeaderNames.AUTHORIZATION), response.header(HeaderNames.LOCATION), (response.json \ "gatewayToken").asOpt[String]) match {
            case (Some(token), Some(sessionUri), Some(governmentGatewayToken)) => Some(AuthLoginAPIResponse(token, sessionUri, governmentGatewayToken))
            case _ => None
          }
        case _ => None
      }
    }
  }
  
}
