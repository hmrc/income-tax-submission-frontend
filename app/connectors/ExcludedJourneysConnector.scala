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

package connectors

import config.AppConfig
import connectors.httpParsers.ClearExcludedJourneysHttpParser._
import connectors.httpParsers.GetExcludedJourneysHttpParser._
import models.ClearExcludedJourneysRequestModel
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ExcludedJourneysConnector @Inject()(http: HttpClient, appConfig: AppConfig)(implicit ec: ExecutionContext) {

  def getExcludedJourneys(taxYear: Int, nino: String)(implicit hc: HeaderCarrier): Future[GetExcludedJourneysResponse] = {
    http.GET[GetExcludedJourneysResponse](appConfig.getExcludedJourneysUrl(taxYear, nino))
  }

  def clearExcludedJourneys(taxYear: Int, nino: String, data: ClearExcludedJourneysRequestModel)
                           (implicit hc: HeaderCarrier): Future[ClearExcludedJourneysResponse] = {

    http.POST[ClearExcludedJourneysRequestModel, ClearExcludedJourneysResponse](appConfig.clearExcludedJourneysUrl(taxYear, nino), data)
  }

}
