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

import models.IncomeSourcesModel
import play.api.http.Status._
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import utils.PagerDutyHelper.PagerDutyKeys._
import utils.PagerDutyHelper.pagerDutyLog

object IncomeSourcesHttpParser {
  type IncomeSourcesResponse = Either[IncomeSourcesError, IncomeSourcesModel]

  implicit object IncomeSourcesHttpReads extends HttpReads[IncomeSourcesResponse] {
    override def read(method: String, url: String, response: HttpResponse): IncomeSourcesResponse = {
      response.status match {
        case OK => response.json.validate[IncomeSourcesModel].fold[IncomeSourcesResponse](
          jsonErrors => {
            pagerDutyLog(BAD_SUCCESS_JSON_FROM_API, Some(s"[IncomeSourcesHttpParser][read] Invalid Json from API."))
            Left(IncomeSourcesInvalidJsonError)
          },
          parsedModel => Right(parsedModel)
        )
        case NO_CONTENT => Right(IncomeSourcesModel())
        case NOT_FOUND =>
          pagerDutyLog(NOT_FOUND_FROM_API, logMessage(response))
          Right(IncomeSourcesModel())
        case INTERNAL_SERVER_ERROR =>
          pagerDutyLog(INTERNAL_SERVER_ERROR_FROM_API, logMessage(response))
          Left(IncomeSourcesInternalServerError)
        case SERVICE_UNAVAILABLE =>
          pagerDutyLog(SERVICE_UNAVAILABLE_FROM_API, logMessage(response))
          Left(IncomeSourcesServiceUnavailableError)
        case _ =>
          pagerDutyLog(UNEXPECTED_RESPONSE_FROM_API, logMessage(response))
          Left(IncomeSourcesUnhandledError)
      }
    }

    private def logMessage(response:HttpResponse): Option[String] ={
      Some(s"[IncomeSourcesHttpParser][read] Received ${response.status} from income-sources. Body:${response.body}")
    }
  }

  sealed trait IncomeSourcesError

  object IncomeSourcesInvalidJsonError extends IncomeSourcesError
  object IncomeSourcesServiceUnavailableError extends IncomeSourcesError
  object IncomeSourcesInternalServerError extends IncomeSourcesError
  object IncomeSourcesNotFoundError extends IncomeSourcesError
  object IncomeSourcesUnhandledError extends IncomeSourcesError

}
