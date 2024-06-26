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
import connectors.httpParsers.LiabilityCalculationHttpParser.{LiabilityCalculationHttpReads, LiabilityCalculationResponse}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

class LiabilityCalculationConnector @Inject()(http: HttpClient,
                                              config: AppConfig
                                     )(implicit ec: ExecutionContext) extends RawResponseReads {

  def getCalculationId(nino: String, taxYear: Int)(implicit hc: HeaderCarrier): Future[LiabilityCalculationResponse] = {
    val Url: String = config.calculationBaseUrl + s"/income-tax-calculation/income-tax/nino/$nino/taxYear/$taxYear/tax-calculation"
    http.GET[LiabilityCalculationResponse](Url)
  }

  def getIntentToCrystallise(nino: String, taxYear: Int, crystallise: Boolean)(implicit hc: HeaderCarrier): Future[LiabilityCalculationResponse] = {
    val Url: String = config.calculationBaseUrl + s"/income-tax-calculation/income-tax/nino/$nino/taxYear/$taxYear/tax-calculation?crystallise=$crystallise"
    http.GET[LiabilityCalculationResponse](Url)
  }
}
