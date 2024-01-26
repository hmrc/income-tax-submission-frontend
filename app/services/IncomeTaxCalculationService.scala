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

import connectors.IncomeTaxCalculationConnector
import models.calculation.{LiabilityCalculationDetailsModel, LiabilityCalculationDetailsError}
import play.api.Logger
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IncomeTaxCalculationService @Inject()(incomeTaxCalculationConnector: IncomeTaxCalculationConnector)
                                           (implicit ec: ExecutionContext) {

  def getCalculationDetailsByCalcId(mtditid: String, nino: String, calcId: String, taxYear: Int)
                                   (implicit headerCarrier: HeaderCarrier): Future[Either[LiabilityCalculationDetailsError, LiabilityCalculationDetailsModel]] = {
    Logger("application").debug("[IncomeTaxCalculationService][getCalculationDetailsByCalcId] - " +
      s"Requesting calc data from the backend by calc id and taxYear: $calcId - $taxYear")
    incomeTaxCalculationConnector.getCalculationResponseByCalcId(mtditid, nino, calcId, taxYear)
  }

  def getCalculationDetails(mtditid: String, nino: String, taxYear: Int)
                           (implicit headerCarrier: HeaderCarrier): Future[Either[LiabilityCalculationDetailsError, LiabilityCalculationDetailsModel]] = {
    Logger("application").debug("[IncomeTaxCalculationService][getCalculationDetails] - " +
      s"Requesting calc data from the backend by nino and taxYear")
    incomeTaxCalculationConnector.getCalculationResponse(mtditid, nino, taxYear)
  }
}
