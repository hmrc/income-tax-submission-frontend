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

import config.AppConfig
import connectors.httpParsers.IncomeSourcesHttpParser.{IncomeSourcesHttpReads, IncomeSourcesResponse}
import play.api.Logging
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IncomeSourcesConnector @Inject()(val http: HttpClient,
                                       val config: AppConfig
                                      )(implicit ec: ExecutionContext) extends RawResponseReads with Logging{

  def getIncomeSources(nino: String, taxYear: Int)(implicit hc: HeaderCarrier): Future[IncomeSourcesResponse] = {
    val incomeSourcesUrl: String = config.incomeTaxSubmissionUrl + s"/nino/$nino/sources?taxYear=$taxYear"
    http.GET[IncomeSourcesResponse](incomeSourcesUrl)(IncomeSourcesHttpReads, hcWithExcludedIncomeSources(taxYear), ec)
  }

  private[connectors] def hcWithExcludedIncomeSources(taxYear: Int)(implicit hc: HeaderCarrier): HeaderCarrier = {

    if (config.excludedIncomeSources(taxYear).nonEmpty) {
      hc.withExtraHeaders("excluded-income-sources" -> config.excludedIncomeSources(taxYear).mkString(","))
    } else {
      hc
    }
  }

}
