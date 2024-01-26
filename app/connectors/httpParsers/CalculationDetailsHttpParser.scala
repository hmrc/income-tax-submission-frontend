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

package connectors.httpParsers

import models.{APIErrorBodyModel, APIErrorModel}
import models.calculation.CalculationResponseModel
import play.api.Logging
import play.api.http.Status._
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import utils.PagerDutyHelper.PagerDutyKeys._
import utils.PagerDutyHelper._


object CalculationDetailsHttpParser extends APIParser with Logging {
  type CalculationDetailResponse = Either[APIErrorModel, CalculationResponseModel]

  override val parserName: String = "CalculationDetailsHttpParser"
  override val service: String = "income-tax-calculation"

  implicit object CalculationDetailsHttpReads extends HttpReads[CalculationDetailResponse] {
    override def read(method: String, url: String, response: HttpResponse): CalculationDetailResponse = {
      response.status match {
        case OK => response.json.validate[CalculationResponseModel].fold[CalculationDetailResponse](
          validationErrors => badSuccessJsonFromAPIWithErrors(validationErrors),
          parsedModel => Right(parsedModel)
        )
        case NOT_FOUND =>
          logger.info(s"[CalculationDetailsHttpReads]: $NOT_FOUND converted to $NO_CONTENT")
          Left(APIErrorModel(NO_CONTENT, APIErrorBodyModel(NOT_FOUND.toString, "NOT FOUND")))
        case INTERNAL_SERVER_ERROR =>
          pagerDutyLog(INTERNAL_SERVER_ERROR_FROM_API, logMessage(response))
          handleAPIError(response)
        case SERVICE_UNAVAILABLE =>
          pagerDutyLog(SERVICE_UNAVAILABLE_FROM_API, logMessage(response))
          handleAPIError(response)
        case BAD_REQUEST | CONFLICT | UNPROCESSABLE_ENTITY =>
          pagerDutyLog(FOURXX_RESPONSE_FROM_API, logMessage(response))
          handleAPIError(response)
        case _ =>
          pagerDutyLog(UNEXPECTED_RESPONSE_FROM_API, logMessage(response))
          handleAPIError(response)
      }
    }
  }
}
