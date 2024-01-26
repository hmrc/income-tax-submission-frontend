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
import connectors.httpParsers.CalculationDetailsHttpParser.{CalculationDetailResponse,CalculationDetailsHttpReads}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IncomeTaxCalculationConnector @Inject()(http: HttpClient,
                                              config: AppConfig) extends RawResponseReads {

  def getCalculationResponseUrl(nino: String): String = s"${config.calculationBaseUrl}/income-tax/nino/$nino/calculation-details"


  def getCalculationResponseByCalcIdUrl(nino: String, calcId: String): String =
    s"${config.calculationBaseUrl}/income-tax/nino/$nino/calc-id/$calcId/calculation-details"

  def getCalculationResponse(mtditid: String, nino: String, taxYear: Int)
                            (implicit headerCarrier: HeaderCarrier,
                             ec: ExecutionContext): Future[CalculationDetailResponse] = {
    val Url: String = getCalculationResponseUrl(nino)
    http.GET[CalculationDetailResponse](Url,
      Seq(("taxYear", taxYear.toString)))(
      CalculationDetailsHttpReads,
      headerCarrier.withExtraHeaders("mtditid" -> mtditid), ec
    )
  }

  def getCalculationResponseByCalcId(mtditid: String, nino: String, calcId: String, taxYear: Int)
                                    (implicit headerCarrier: HeaderCarrier,
                                     ec: ExecutionContext): Future[CalculationDetailResponse] = {
    val Url: String = getCalculationResponseByCalcIdUrl(nino, calcId)
    http.GET[CalculationDetailResponse](Url,
      Seq(("taxYear", taxYear.toString)))(
          CalculationDetailsHttpReads,
          headerCarrier.withExtraHeaders("mtditid" -> mtditid), ec
        )
  }
}
