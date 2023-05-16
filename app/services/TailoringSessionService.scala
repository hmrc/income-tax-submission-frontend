/*
 * Copyright 2023 HM Revenue & Customs
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

import com.google.inject.Inject
import models.User
import models.mongo.{DatabaseError, TailoringUserDataModel}
import org.joda.time.{DateTime, DateTimeZone}
import play.api.Logger
import repositories.TailoringUserDataRepository

import scala.concurrent.{ExecutionContext, Future}

class TailoringSessionService @Inject() (tailoringUserDataRepository: TailoringUserDataRepository) {

  lazy val logger: Logger = Logger(this.getClass)

  def getSessionData(taxYear: Int)(implicit user: User[_], ec: ExecutionContext): Future[Either[DatabaseError, Option[TailoringUserDataModel]]] = {

    tailoringUserDataRepository.find(taxYear).map {
      case Left(error) =>
        logger.error("[TailoringSessionService][getSessionData] Could not find user session.")
        Left(error)
      case Right(userData) => Right(userData)
    }
  }

  def createSessionData[A](dataModel: Seq[String], taxYear: Int)(onFail: A)(onSuccess: A)
                          (implicit user: User[_], ec: ExecutionContext): Future[A] = {

    val userData: TailoringUserDataModel = TailoringUserDataModel(
      nino = user.nino,
      taxYear = taxYear,
      tailoring = dataModel,
      lastUpdated = DateTime.now(DateTimeZone.UTC)
    )

    tailoringUserDataRepository.create(userData)().map {
      case Left(_) =>
        logger.error("[TailoringSessionService][createSessionData] Could not create user session.")
        onFail
      case Right(_) => onSuccess
    }
  }

  def updateSessionData[A](dataModel: Seq[String], taxYear: Int)(onFail: A)(onSuccess: A)
                          (implicit user: User[_], ec: ExecutionContext): Future[A] = {

    val userData: TailoringUserDataModel = TailoringUserDataModel(
      nino = user.nino,
      taxYear = taxYear,
      tailoring = dataModel,
      lastUpdated = DateTime.now(DateTimeZone.UTC)
    )

    tailoringUserDataRepository.update(userData).map {
      case Left(_) =>
        logger.error("[TailoringSessionService][updateSessionData] Could not update user session.")
        onFail
      case Right(_) => onSuccess
    }
  }

}
