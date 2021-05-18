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

package repositories

import java.util.concurrent.TimeUnit

import com.mongodb.client.model.ReturnDocument
import com.mongodb.client.model.Updates.set
import config.AppConfig
import javax.inject.{Inject, Singleton}
import models.User
import models.mongo.UserData
import org.joda.time.{DateTime, DateTimeZone}
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.Filters.{and, equal}
import org.mongodb.scala.model.Indexes.{ascending, compoundIndex}
import org.mongodb.scala.model.{FindOneAndReplaceOptions, FindOneAndUpdateOptions, IndexModel, IndexOptions}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.Codecs.toBson
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.mongo.play.json.formats.MongoJodaFormats

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IncomeTaxUserDataRepositoryImpl @Inject()(mongo: MongoComponent, appConfig: AppConfig)(implicit ec: ExecutionContext
) extends PlayMongoRepository[UserData](
  mongoComponent = mongo,
  collectionName = "userData",
  domainFormat   = UserData.formats,
  indexes        = IncomeTaxUserDataIndexes.indexes(appConfig)
) with IncomeTaxUserDataRepository {

  private def filter(sessionId: String, mtdItId: String, nino: String, taxYear: Int): Bson = and(
    equal("sessionId", toBson(sessionId)),
    equal("mtdItId", toBson(mtdItId)),
    equal("nino", toBson(nino)),
    equal("taxYear", toBson(taxYear))
  )

  def update(userData: UserData): Future[Boolean] = {
    collection.findOneAndReplace(
      filter = filter(userData.sessionId,userData.mtdItId,userData.nino,userData.taxYear),
      replacement = userData,
      options = FindOneAndReplaceOptions().upsert(true).returnDocument(ReturnDocument.AFTER)
    ).toFutureOption().map(_.isDefined)
  }

  def find[T](user: User[T], taxYear: Int): Future[Option[UserData]] = {
    collection.findOneAndUpdate(
      filter = filter(user.sessionId,user.mtditid,user.nino,taxYear),
      update = set("lastUpdated", toBson(DateTime.now(DateTimeZone.UTC))(MongoJodaFormats.dateTimeWrites)),
      options = FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER)
    ).toFutureOption()
  }
}

trait IncomeTaxUserDataRepository {

  def find[T](user: User[T], taxYear: Int): Future[Option[UserData]]
  def update(userData: UserData): Future[Boolean]
}

private object IncomeTaxUserDataIndexes {

  private val lookUpIndex: Bson = compoundIndex(
    ascending("sessionId"),
    ascending("mtdItId"),
    ascending("nino"),
    ascending("taxYear")
  )

  def indexes(appConfig: AppConfig): Seq[IndexModel] = {
    Seq(
      IndexModel(lookUpIndex, IndexOptions().unique(true).name("UserDataLookupIndex")),
      IndexModel(ascending("lastUpdated"), IndexOptions().expireAfter(appConfig.mongoTTL, TimeUnit.MINUTES).name("UserDataTTL"))
    )
  }

}
