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
import models.calculation.{CalculationResponseModel, LiabilityCalculationError, LiabilityCalculationResponseModel}
import play.api.Logger
import play.api.http.Status._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IncomeTaxCalculationConnector @Inject()(http: HttpClient,
                                              config: AppConfig) extends RawResponseReads {
  val baseUrl: String = config.incomeTaxCalculationService

  def getCalculationResponseUrl(nino: String): String = s"$baseUrl/income-tax-calculation/income-tax/nino/$nino/calculation-details"

  def getCalculationResponseByCalcIdUrl(nino: String, calcId: String): String =
    s"$baseUrl/income-tax-calculation/income-tax/nino/$nino/calc-id/$calcId/calculation-details"

  def getCalculationResponse(mtditid: String, nino: String, taxYear: String)
                            (implicit headerCarrier: HeaderCarrier,
                             ec: ExecutionContext): Future[Either[LiabilityCalculationError, LiabilityCalculationResponseModel]] = {
    http.GET[HttpResponse](getCalculationResponseUrl(nino), Seq(("taxYear", taxYear)))(httpReads,
      headerCarrier.withExtraHeaders("mtditid" -> mtditid), ec) map { response =>
      response.status match {
        case OK =>
          response.json.validate[CalculationResponseModel].fold(
            invalid => {
              Logger("application").error(
                s"[IncomeTaxCalculationConnector][getCalculationResponse] - Json validation error parsing calculation response, error $invalid")
              Left(LiabilityCalculationError(INTERNAL_SERVER_ERROR, "Json validation error parsing calculation response"))
            },
            valid => Right(valid)
          )
        case status =>
          if (status >= INTERNAL_SERVER_ERROR) {
            Logger("application").error(s"[IncomeTaxCalculationConnector][getCalculationResponse] - Response status: ${response.status},body: ${response.body}")
          } else {
            Logger("application").warn(s"[IncomeTaxCalculationConnector][getCalculationResponse] - Response status: ${response.status}, body: ${response.body}")
          }
          Left(LiabilityCalculationError(response.status, response.body))
      }
    }
  }

  def getCalculationResponseByCalcId(mtditid: String, nino: String, calcId: String, taxYear: Int)
                                    (implicit headerCarrier: HeaderCarrier,
                                     ec: ExecutionContext): Future[Either[LiabilityCalculationError, LiabilityCalculationResponseModel]] = {
    http.GET[HttpResponse](getCalculationResponseByCalcIdUrl(nino, calcId), Seq(("taxYear", taxYear.toString)))(httpReads,
      headerCarrier.withExtraHeaders("mtditid" -> mtditid), ec) map { response =>
      response.status match {
        case OK =>
          response.json.validate[CalculationResponseModel].fold(
            invalid => {
              Logger("application").error(
                s"[IncomeTaxCalculationConnector][getCalculationResponseByCalcId] - Json validation error parsing calculation response, error $invalid")
              Left(LiabilityCalculationError(INTERNAL_SERVER_ERROR, "Json validation error parsing calculation response"))
            },
            valid => Right(valid)
          )
        case status =>
          if (status >= INTERNAL_SERVER_ERROR) {
            Logger("application").error(
              s"[IncomeTaxCalculationConnector][getCalculationResponseByCalcId] - Response status: ${response.status},body: ${response.body}")
          } else {
            Logger("application").warn(
              s"[IncomeTaxCalculationConnector][getCalculationResponseByCalcId] - Response status: ${response.status}, body: ${response.body}")
          }
          Left(LiabilityCalculationError(response.status, response.body))
      }
    }
  }
}
