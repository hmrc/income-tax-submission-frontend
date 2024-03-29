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

import models.{APIErrorModel, IncomeSourcesModel}
import play.api.Logging
import play.api.http.Status._
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import utils.PagerDutyHelper.PagerDutyKeys._
import utils.PagerDutyHelper.pagerDutyLog

object IncomeSourcesHttpParser extends APIParser with Logging {
  type IncomeSourcesResponse = Either[APIErrorModel, IncomeSourcesModel]

  override val parserName: String = "IncomeSourcesHttpParser"
  override val service: String = "income-tax-submission"

  implicit object IncomeSourcesHttpReads extends HttpReads[IncomeSourcesResponse] {
    override def read(method: String, url: String, response: HttpResponse): IncomeSourcesResponse = {
      response.status match {
        case OK => response.json.validate[IncomeSourcesModel].fold[IncomeSourcesResponse](
            jsonErrors => badSuccessJsonFromAPIWithErrors(jsonErrors),
            parsedModel => Right(parsedModel.excludeNotRelevantEmploymentData)
          )
        case NO_CONTENT => Right(IncomeSourcesModel())
        case NOT_FOUND =>
          pagerDutyLog(NOT_FOUND_FROM_API, logMessage(response))
          Right(IncomeSourcesModel())
        case INTERNAL_SERVER_ERROR =>
          pagerDutyLog(INTERNAL_SERVER_ERROR_FROM_API, logMessage(response))
          handleAPIError(response)
        case SERVICE_UNAVAILABLE =>
          pagerDutyLog(SERVICE_UNAVAILABLE_FROM_API, logMessage(response))
          handleAPIError(response)
        case _ =>
          pagerDutyLog(UNEXPECTED_RESPONSE_FROM_API, logMessage(response))
          handleAPIError(response, Some(INTERNAL_SERVER_ERROR))
      }
    }
  }
}
