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

import models.{APIErrorModel, GetExcludedJourneysResponseModel}
import play.api.http.Status._
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object GetExcludedJourneysHttpParser extends APIParser {
  type GetExcludedJourneysResponse = Either[APIErrorModel, GetExcludedJourneysResponseModel]

  override val parserName: String = "GetExcludedJourneysHttpParser"
  override val service: String = "income-tax-submission"

  implicit object GetExcludedJourneysHttpReads extends HttpReads[GetExcludedJourneysResponse] {
    override def read(method: String, url: String, response: HttpResponse): GetExcludedJourneysResponse = {
      response.status match {
        case OK => response.json.validate[GetExcludedJourneysResponseModel].fold[GetExcludedJourneysResponse](
          _ => badSuccessJsonFromAPI,
          parsedModel => Right(parsedModel)
        )
        case INTERNAL_SERVER_ERROR => handleAPIError(response)
        case _ => handleAPIError(response, Some(INTERNAL_SERVER_ERROR))
      }
    }
  }
}
