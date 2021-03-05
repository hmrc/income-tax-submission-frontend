/*
 * Copyright 2021 HM Revenue & Customs
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

package connectors.httpparsers

import models.LiabilityCalculationIdModel
import play.api.http.Status._
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import utils.PagerDutyHelper.PagerDutyKeys._
import utils.PagerDutyHelper.pagerDutyLog

object CalculationIdHttpParser {
  type CalculationIdResponse = Either[CalculationIdError, LiabilityCalculationIdModel]

  implicit object CalculationIdHttpReads extends HttpReads[CalculationIdResponse] {
    override def read(method: String, url: String, response: HttpResponse): CalculationIdResponse = {
      response.status match {
        case OK => response.json.validate[LiabilityCalculationIdModel].fold[CalculationIdResponse](
          jsonErrors => {
            pagerDutyLog(BAD_SUCCESS_JSON_FROM_API, Some(s"[CalculationIdHttpParser][read] Invalid Json from API."))
            Left(CalculationIdErrorInvalidJsonError)
          },
          parsedModel => Right(parsedModel)
        )
        case INTERNAL_SERVER_ERROR =>
          pagerDutyLog(INTERNAL_SERVER_ERROR_FROM_API, logMessage(response))
          Left(CalculationIdErrorInternalServerError)
        case SERVICE_UNAVAILABLE =>
          pagerDutyLog(SERVICE_UNAVAILABLE_FROM_API, logMessage(response))
          Left(CalculationIdErrorServiceUnavailableError)
        case _ =>
          pagerDutyLog(UNEXPECTED_RESPONSE_FROM_API, logMessage(response))
          Left(CalculationIdErrorUnhandledError)
      }
    }

    private def logMessage(response:HttpResponse): Option[String] ={
      Some(s"[CalculationIdHttpParser][read] Received ${response.status} from Calculation connector. Body:${response.body}")
    }
  }

  sealed trait CalculationIdError

  object CalculationIdErrorInvalidJsonError extends CalculationIdError
  object CalculationIdErrorServiceUnavailableError extends CalculationIdError
  object CalculationIdErrorInternalServerError extends CalculationIdError
  object CalculationIdErrorUnhandledError extends CalculationIdError

}
