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

package services

import javax.inject.{Inject, Singleton}
import models.IncomeSourcesModel
import models.mongo.UserData
import repositories.IncomeTaxUserDataRepository

import scala.concurrent.Future


@Singleton
class IncomeTaxUserDataService @Inject()(incomeTaxUserDataRepository: IncomeTaxUserDataRepository) {

  def updateUserData(sessionId: String,
                     mtdItId: String,
                     nino: String,
                     taxYear: Int,
                     incomeSourcesModel: Option[IncomeSourcesModel]): Future[Boolean] = {

    val userData = UserData(
      sessionId,mtdItId,nino,taxYear,
      dividends = incomeSourcesModel.flatMap(_.dividends),
      interest = incomeSourcesModel.flatMap(_.interest),
      giftAid = incomeSourcesModel.flatMap(_.giftAid),
      employment = incomeSourcesModel.flatMap(_.employment)
    )

    incomeTaxUserDataRepository.update(userData)
  }
}
