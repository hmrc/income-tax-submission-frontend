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

import itUtils.IntegrationTest
import models.User
import models.mongo.UserData
import org.joda.time.DateTime
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.result.InsertOneResult
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.mvc.AnyContentAsEmpty
import play.api.test.{DefaultAwaitTimeout, FakeRequest, FutureAwaits}
import uk.gov.hmrc.mongo.play.json.Codecs.toBson

class IncomeTaxUserDataRepositoryISpec extends IntegrationTest with FutureAwaits with DefaultAwaitTimeout with BeforeAndAfterEach {

  val repo: IncomeTaxUserDataRepositoryImpl = app.injector.instanceOf[IncomeTaxUserDataRepositoryImpl]

  private def count = await(repo.collection.countDocuments().toFuture())

  class EmptyDatabase {
    await(repo.collection.drop().toFuture())
    await(repo.ensureIndexes)
  }

  val userData: UserData = UserData(
    "sessionId-1618a1e8-4979-41d8-a32e-5ffbe69fac81",
    "1234567890",
    "AA123456A",
    2022,
    dividendsModel,
    interestsModel,
    Some(giftAidModel),
    Some(employmentsModel)
  )

  implicit val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

  "update" should {
    "add a document to the collection" in new EmptyDatabase {
      count mustBe 0
      val res: Boolean = await(repo.update(userData))
      res mustBe true
      count mustBe 1
      val data: Option[UserData] = await(repo.find(User(userData.mtdItId,None,userData.nino,userData.sessionId),userData.taxYear))
      data.map(_.copy(lastUpdated = DateTime.parse("2021-05-17T14:01:52.634Z"))) mustBe Some(
        userData.copy(lastUpdated = DateTime.parse("2021-05-17T14:01:52.634Z"))
      )
    }
    "update a document in the collection" in {
      val newUserData = userData.copy(dividends = dividendsModel.map(_.copy(ukDividends = Some(344565.44))))
      count mustBe 1
      val res: Boolean = await(repo.update(newUserData))
      res mustBe true
      count mustBe 1
      val data: Option[UserData] = await(repo.find(User(userData.mtdItId,None,userData.nino,userData.sessionId),userData.taxYear))
      data.map(_.copy(lastUpdated = DateTime.parse("2021-05-17T14:01:52.634Z"))) mustBe Some(
        newUserData.copy(lastUpdated = DateTime.parse("2021-05-17T14:01:52.634Z"))
      )
    }
    "insert a new document to the collection if the sessionId is different" in {
      val newUserData = userData.copy(sessionId = "sessionId-000001")
      count mustBe 1
      val res: Boolean = await(repo.update(newUserData))
      res mustBe true
      count mustBe 2
    }
  }

  "find" should {

    def filter(sessionId: String, mtdItId: String, nino: String, taxYear: Int): Bson = org.mongodb.scala.model.Filters.and(
      org.mongodb.scala.model.Filters.equal("sessionId", toBson(sessionId)),
      org.mongodb.scala.model.Filters.equal("mtdItId", toBson(mtdItId)),
      org.mongodb.scala.model.Filters.equal("nino", toBson(nino)),
      org.mongodb.scala.model.Filters.equal("taxYear", toBson(taxYear))
    )

    "get a document and update the TTL" in {
      count mustBe 2
      val dataBefore: UserData = await(repo.collection.find(filter(userData.sessionId,userData.mtdItId,userData.nino,userData.taxYear)).toFuture()).head
      val dataAfter: Option[UserData] = await(repo.find(User(userData.mtdItId,None,userData.nino,userData.sessionId),userData.taxYear))

      dataAfter.map(_.copy(lastUpdated = dataBefore.lastUpdated)) mustBe Some(dataBefore)
      dataAfter.map(_.lastUpdated.isAfter(dataBefore.lastUpdated)) mustBe Some(true)
    }
  }

  "the set indexes" should {
    "enforce uniqueness" in {
      val result: Either[Exception,InsertOneResult] = try {
        Right(await(repo.collection.insertOne(userData).toFuture()))
      } catch {
        case e: Exception => Left(e)
      }
      result.isLeft mustBe true
      result.left.get.getMessage must include("E11000 duplicate key error collection: income-tax-submission-frontend.userData index: sessionId_1_mtdItId_1_nino_1_taxYear_1 dup key:")
    }
  }
}
