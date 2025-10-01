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

package repositories

import com.mongodb.client.model.ReturnDocument
import models.User
import models.mongo._
import org.apache.pekko.Done
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.Filters.{and, equal}
import org.mongodb.scala.model.Updates.set
import org.mongodb.scala.model.{FindOneAndReplaceOptions, FindOneAndUpdateOptions}
import uk.gov.hmrc.mdc.Mdc
import uk.gov.hmrc.mongo.play.json.Codecs.toBson
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import utils.PagerDutyHelper.PagerDutyKeys._
import utils.PagerDutyHelper.pagerDutyLog

import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

trait UserDataRepository[C <: UserDataTemplate] {
  self: PlayMongoRepository[C] =>
  implicit val ec: ExecutionContext

  val repoName: String
  type UserData

  def encryptionMethod: UserData => C
  def decryptionMethod: C => UserData

  def create(userData: UserData): Future[Either[DatabaseError, Done]] =
    withEncryptedData(userData, s"[$repoName][create]") { encryptedData =>
      Mdc.preservingMdc {
        collection.insertOne(encryptedData).toFuture().map(_ => Right(Done)).recover { exception =>
          pagerDutyLog(FAILED_TO_CREATE_DATA, Some(s"[$repoName][create] Failed to create user data. Exception: ${exception.getMessage}"))
          Left(DataNotUpdated)
        }
      }
    }

  def find[T](taxYear: Int)(implicit user: User[T]): Future[Either[DatabaseError, Option[UserData]]] =
    Mdc.preservingMdc {
      collection.findOneAndUpdate(
        filter = filter(user.nino, taxYear),
        update = set("lastUpdated", toBson(Instant.now())),
        options = FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
      ).toFutureOption()
    }.map { data =>
      Try(data.map(decryptionMethod)).fold(
        handleEncryptionDecryptionException(_, s"[$repoName][find]"),
        Right(_)
      )
    }.recover { exception =>
      pagerDutyLog(FAILED_TO_FIND_DATA, Some(s"[$repoName][find] Failed when trying to find user data. Exception: ${exception.getMessage}"))
      Left(DataNotFound)
    }

  def update(userData: UserData): Future[Either[DatabaseError, Done]] =
    withEncryptedData(userData, s"[$repoName][update]") { encryptedData =>
      Mdc.preservingMdc {
        collection.findOneAndReplace(
          filter = filter(encryptedData.nino, encryptedData.taxYear),
          replacement = encryptedData,
          options = FindOneAndReplaceOptions().returnDocument(ReturnDocument.AFTER)
        ).toFutureOption()
      }.map {
        case Some(_) => Right(Done)
        case _ => Left(DataNotUpdated)
      }.recover { exception =>
        pagerDutyLog(FAILED_TO_UPDATE_DATA, Some(s"[$repoName][update] Failed to update user data. Exception: ${exception.getMessage}"))
        Left(DataNotUpdated)
      }
    }

  def clear(taxYear: Int)(implicit user: User[_]): Future[Done] =
    Mdc.preservingMdc {
      collection.deleteOne(filter(user.nino, taxYear)).toFuture().map(_ => Done)
    }

  def filter(nino: String, taxYear: Int): Bson = and(
    equal("nino", toBson(nino)),
    equal("taxYear", toBson(taxYear))
  )

  private def withEncryptedData[T](userData: UserData, logPrefix: String)
                                  (block: C => Future[Either[DatabaseError, T]]): Future[Either[DatabaseError, T]] =
    Try(encryptionMethod(userData)).fold(
      e => Future.successful(handleEncryptionDecryptionException(e, logPrefix)),
      block
    )

  private def handleEncryptionDecryptionException[T](exception: Throwable, startOfMessage: String): Left[DatabaseError, T] = {
    pagerDutyLog(ENCRYPTION_DECRYPTION_ERROR, Some(s"$startOfMessage ${exception.getMessage}"))
    Left(EncryptionDecryptionError(exception.getMessage))
  }

}
