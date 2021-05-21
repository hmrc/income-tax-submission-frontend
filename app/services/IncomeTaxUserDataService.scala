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

import config.AppConfig
import javax.inject.{Inject, Singleton}
import models.mongo.UserData
import models.{IncomeSourcesModel, User}
import play.api.Logging
import play.api.i18n.MessagesApi
import play.api.mvc.Results.InternalServerError
import play.api.mvc.{Request, Result}
import repositories.IncomeTaxUserDataRepository
import views.html.errors.InternalServerErrorPage

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class IncomeTaxUserDataService @Inject()(incomeTaxUserDataRepository: IncomeTaxUserDataRepository,
                                         internalServerErrorPage: InternalServerErrorPage,
                                         implicit private val appConfig: AppConfig,
                                         val messagesApi: MessagesApi) extends Logging {

  def saveUserData[T](user: User[T],
                      taxYear: Int,
                      incomeSourcesModel: Option[IncomeSourcesModel] = None)
                     (implicit request: Request[_], ec: ExecutionContext): Future[Either[Result, Boolean]] = {

    updateUserData(user.sessionId, user.mtditid, user.nino, taxYear, incomeSourcesModel).map {
      response =>
        if (response) {
          Right(true)
        } else {
          logger.error("[IncomeTaxUserDataService][saveUserData] Failed to save user data")
          Left(InternalServerError(internalServerErrorPage()(request, messagesApi.preferred(request), appConfig)))
        }
    }
  }

  private def updateUserData(sessionId: String,
                             mtdItId: String,
                             nino: String,
                             taxYear: Int,
                             incomeSourcesModel: Option[IncomeSourcesModel] = None): Future[Boolean] = {

    val userData = UserData(
      sessionId, mtdItId, nino, taxYear,
      dividends = incomeSourcesModel.flatMap(_.dividends),
      interest = incomeSourcesModel.flatMap(_.interest),
      giftAid = incomeSourcesModel.flatMap(_.giftAid),
      employment = incomeSourcesModel.flatMap(_.employment)
    )

    incomeTaxUserDataRepository.update(userData)
  }
}
