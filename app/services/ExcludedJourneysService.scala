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

package services

import connectors.ExcludedJourneysConnector
import models.{ClearExcludedJourneysRequestModel, GetExcludedJourneysResponseModel}
import org.apache.pekko.Done
import play.api.Logging
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ExcludedJourneysService @Inject()(excludedJourneysConnector: ExcludedJourneysConnector) extends Logging {

  def getExcludedJourneys(taxYear: Int, nino: String, mtditid: String)
                         (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[GetExcludedJourneysResponseModel] =
    excludedJourneysConnector.getExcludedJourneys(taxYear, nino)(hc.withExtraHeaders("mtditid" -> mtditid)).map {
      case Right(data) => data
      case Left(error) =>
        logger.warn(s"[getExcludedJourneys] Failed to retrieve excluded journeys for tax year: $taxYear, mtditid: $mtditid, returned error: $error")
        throw new RuntimeException(s"Failed to retrieve excluded journeys for tax year: $taxYear, mtditid: $mtditid")
    }

  def clearExcludedJourneys(taxYear: Int, nino: String, mtditid: String, data: ClearExcludedJourneysRequestModel)
                           (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Done] =
    excludedJourneysConnector.clearExcludedJourneys(taxYear, nino, data)(hc.withExtraHeaders("mtditid" -> mtditid)).map {
      case Right(_) => Done
      case Left(error) =>
        logger.warn(s"[clearExcludedJourneys] Failed to clear excluded journeys for tax year: $taxYear, mtditid: $mtditid. Returned error: $error")
        throw new RuntimeException(s"Failed to clear excluded journeys for tax year: $taxYear, mtditid: $mtditid")
    }
}
