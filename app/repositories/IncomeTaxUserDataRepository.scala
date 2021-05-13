/*
 * Copyright 2020 HM Revenue & Customs
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

import config.AppConfig
import javax.inject.Inject
import models.mongo.UserData
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.Filters.{and, equal}
import org.mongodb.scala.model.Indexes.{ascending, compoundIndex}
import org.mongodb.scala.model.{FindOneAndReplaceOptions, IndexModel, IndexOptions}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.Codecs.toBson
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IncomeTaxUserDataRepository @Inject()(mongo: MongoComponent, appConfig: AppConfig)(implicit ec: ExecutionContext
) extends PlayMongoRepository[UserData](
  mongoComponent = mongo,
  collectionName = "userData",
  domainFormat   = UserData.formats,
  indexes        = IncomeTaxUserDataIndexes.indexes(appConfig)
){

  def update(userData: UserData): Future[Boolean] = {

    collection.findOneAndReplace(
      filter = and(
        equal("sessionId", toBson(userData.sessionId)),
        equal("mtdItId", toBson(userData.mtdItId)),
        equal("nino", toBson(userData.nino)),
        equal("taxYear", toBson(userData.taxYear))
      ),
      replacement = userData,
      options = FindOneAndReplaceOptions().upsert(true))
      .toFutureOption().map(_.isDefined)
  }
}

object IncomeTaxUserDataIndexes {

  private val lookUpIndex: Bson = compoundIndex(
    ascending("sessionId"),
    ascending("mtdItId"),
    ascending("nino"),
    ascending("taxYear")
  )

  def indexes(appConfig: AppConfig): Seq[IndexModel] = {
    Seq(
      IndexModel(lookUpIndex, IndexOptions().unique(true)),
      IndexModel(ascending("lastUpdated"), IndexOptions().expireAfter(appConfig.mongoTTL, TimeUnit.MINUTES))
    )
  }

}
