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

object IncomeSourcesHttpParser {
  type IncomeSourcesResponse = Either[IncomeSourcesException, IncomeSourcesModel]

  implicit object IncomeSourcesHttpReads extends HttpReads[IncomeSourcesResponse] {
    override def read(method: String, url: String, response: HttpResponse): IncomeSourcesResponse = {
      response.status match {
        case OK => response.json.validate[IncomeSourcesModel].fold[IncomeSourcesResponse](
          jsonErrors =>
            Left(IncomeSourcesInvalidJsonException),
          parsedModel =>
            Right(parsedModel)
        )
        case NOT_FOUND => Left(IncomeSourcesNotFoundException)
        case SERVICE_UNAVAILABLE => Left(IncomeSourcesServiceUnavailableException)
        case _ => Left(IncomeSourcesUnhandledException)
      }
    }
  }



  sealed trait IncomeSourcesException

  object IncomeSourcesInvalidJsonException extends IncomeSourcesException
  object IncomeSourcesServiceUnavailableException extends IncomeSourcesException
  object IncomeSourcesNotFoundException extends IncomeSourcesException
  object IncomeSourcesUnhandledException extends IncomeSourcesException


}
