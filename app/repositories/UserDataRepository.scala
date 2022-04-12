/*
 * Copyright 2022 HM Revenue & Customs
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

package repositories

import com.mongodb.client.model.ReturnDocument
import models.User
import models.mongo._
import org.joda.time.{DateTime, DateTimeZone}
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.Filters.{and, equal}
import org.mongodb.scala.model.Updates.set
import org.mongodb.scala.model.{FindOneAndReplaceOptions, FindOneAndUpdateOptions}
import uk.gov.hmrc.mongo.play.json.Codecs.{logger, toBson}
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.mongo.play.json.formats.MongoJodaFormats
import utils.EncryptionDecryptionException
import utils.PagerDutyHelper.PagerDutyKeys._
import utils.PagerDutyHelper.pagerDutyLog

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

trait UserDataRepository[C <: UserDataTemplate] {
  self: PlayMongoRepository[C] =>
  implicit val ec: ExecutionContext

  val repoName: String
  type UserData

  def encryptionMethod: UserData => C

  def decryptionMethod: C => UserData

  def create[T](userData: UserData)(implicit user: User[T]): Future[Either[DatabaseError, Boolean]] = {
    lazy val start = s"[$repoName][create]"
    Try {
      encryptionMethod(userData)
    }.toEither match {
      case Left(exception: Exception) => Future.successful(handleEncryptionDecryptionException(exception, start))
      case Right(encryptedData) =>
        collection.insertOne(encryptedData).toFutureOption().map {
          case Some(_) => Right(true)
        }.recover {
          case exception: Exception =>
            pagerDutyLog(FAILED_TO_CREATE_DATA, Some(s"$start Failed to create user data. Exception: ${exception.getMessage}"))
            Left(DataNotUpdated)
        }
    }
  }

  def find[T](taxYear: Int)(implicit user: User[T]): Future[Either[DatabaseError, Option[UserData]]] = {
    lazy val start = s"[$repoName][find]"

    val userData = collection.findOneAndUpdate(
      filter = filter(user.nino, taxYear),
      update = set("lastUpdated", toBson(DateTime.now(DateTimeZone.UTC))(MongoJodaFormats.dateTimeWrites)),
      options = FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
    ).toFutureOption().map {
      case Some(data) => Right(Some(data))
      case None =>
        logger.info(s"$start No CYA data found for user. SessionId: ${user.sessionId}")
        Right(None)
    }.recover {
      case exception: Exception =>
        pagerDutyLog(FAILED_TO_FIND_DATA, Some(s"$start Failed to find user data. Exception: ${exception.getMessage}"))
        Left(DataNotFound)
    }

    userData.map {
      case Left(_) => Left(DataNotFound)
      case Right(data) =>
        Try {
          data.map(decryptionMethod)
        }.toEither match {
          case Left(value) => handleEncryptionDecryptionException(value, start)
          case Right(value) => Right(value)
        }
    }

  }

  def update(userData: UserData): Future[Either[DatabaseError, Boolean]] = {
    lazy val start = s"[$repoName][update]"

    Try {
      encryptionMethod.apply(userData)
    }.toEither match {
      case Left(exception: Exception) => Future.successful(handleEncryptionDecryptionException(exception, start))
      case Right(encryptedData) =>
        collection.findOneAndReplace(
          filter = filter(encryptedData.nino, encryptedData.taxYear),
          replacement = encryptedData,
          options = FindOneAndReplaceOptions().returnDocument(ReturnDocument.AFTER)
        ).toFutureOption().map {
          case Some(_) => Right(true)
        }.recover {
          case exception: Exception =>
            pagerDutyLog(FAILED_TO_UPDATE_DATA, Some(s"$start Failed to update user data. Exception: ${exception.getMessage}"))
            Left(DataNotUpdated)
        }
    }
  }

  def clear(taxYear: Int)(implicit user: User[_]): Future[Boolean] = collection.deleteOne(
    filter = filter(user.nino, taxYear)
  ).toFutureOption().map(_.isDefined)

  def filter(nino: String, taxYear: Int): Bson = and(
    equal("nino", toBson(nino)),
    equal("taxYear", toBson(taxYear))
  )

  def handleEncryptionDecryptionException[T](exception: Throwable, startOfMessage: String): Left[DatabaseError, T] = {
    val message: String = exception match {
      case exception: EncryptionDecryptionException => s"${exception.failureReason} ${exception.failureMessage}"
      case _ => exception.getMessage
    }

    pagerDutyLog(ENCRYPTION_DECRYPTION_ERROR, Some(s"$startOfMessage $message"))
    Left(EncryptionDecryptionError(message))
  }


}
