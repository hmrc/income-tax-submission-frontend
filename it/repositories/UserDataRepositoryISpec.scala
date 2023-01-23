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

package repositories

import itUtils.IntegrationTest
import models.User
import models.mongo._
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.result.InsertOneResult
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.mvc.AnyContent
import play.api.test.{DefaultAwaitTimeout, FakeRequest, FutureAwaits}
import uk.gov.hmrc.mongo.play.json.Codecs.toBson
import uk.gov.hmrc.crypto.EncryptedValue

class UserDataRepositoryISpec extends IntegrationTest with FutureAwaits with DefaultAwaitTimeout {


  val tailoringRepo: TailoringUserDataRepository = app.injector.instanceOf[TailoringUserDataRepository]

  val tailoringInvalidRepo: TailoringUserDataRepository = customApp(
    invalidEncryptionKey = true
  ).injector.instanceOf[TailoringUserDataRepository]

  private def count: Long = await(tailoringRepo.collection.countDocuments().toFuture())

  class EmptyDatabase {
    await(tailoringRepo.collection.drop().toFuture())
    await(tailoringRepo.ensureIndexes)
  }

  val tailoringUserData: TailoringUserDataModel = TailoringUserDataModel(
    nino,
    taxYear,
    Seq("journey1", "journey2")
  )

  implicit val request: FakeRequest[AnyContent] = FakeRequest()

  "create" should {
    "add a document to the collection" in new EmptyDatabase {
      count mustBe 0
      val result: Either[DatabaseError, Boolean] = await(tailoringRepo.create(tailoringUserData))
      result mustBe Right(true)
      count mustBe 1
    }
    "fail to add a document to the collection when it already exists" in new EmptyDatabase {
      count mustBe 0
      await(tailoringRepo.create(tailoringUserData))
      val result: Either[DatabaseError, Boolean] = await(tailoringRepo.create(tailoringUserData))
      result mustBe Left(DataNotUpdated)
      count mustBe 1
    }
  }

  "update" should {

    "update a document in the collection" in new EmptyDatabase {
      val testUser: User[AnyContent] = User(
        mtditid, None, nino, sessionId
      )(fakeRequest)

      val initialData: TailoringUserDataModel = TailoringUserDataModel(
        testUser.nino, taxYear,
        List("itr","div")
      )

      val newUserData: TailoringUserDataModel = initialData.copy(
        tailoring = List("gif","emp")
      )

      await(tailoringRepo.create(initialData))
      count mustBe 1

      val res: Boolean = await(tailoringRepo.update(newUserData).map {
        case Right(value) => value
        case Left(value) => false
      })
      res mustBe true
      count mustBe 1

      val data: Option[TailoringUserDataModel] = await(tailoringRepo.find(taxYear)(testUser).map {
        case Right(value) => value
        case Left(value) => None
      })

      data.get.tailoring shouldBe List("gif","emp")
    }

    "return a leftDataNotUpdated if the document cannot be found" in {
      val newUserData = tailoringUserData.copy(nino = "AA987654A")
      count mustBe 1
      val res = await(tailoringRepo.update(newUserData))
      res mustBe Left(DataNotUpdated)
      count mustBe 1
    }
  }

  "find" should {
    def filter(sessionId: String, mtdItId: String, nino: String, taxYear: Int): Bson = org.mongodb.scala.model.Filters.and(
      org.mongodb.scala.model.Filters.equal("nino", toBson(nino)),
      org.mongodb.scala.model.Filters.equal("taxYear", toBson(taxYear))
    )

    val testUser = User(
      mtditid, None, nino, sessionId
    )(fakeRequest)

    "get a document" in {
      count mustBe 1
      val dataAfter: Option[TailoringUserDataModel] = await(tailoringRepo.find(taxYear)(testUser).map {
        case Right(value) => value
        case Left(value) => None
      })

      dataAfter.get.tailoring mustBe List("gif","emp")
    }

    "return an encryptionDecryptionError" in {
      await(tailoringInvalidRepo.find(taxYear)(testUser)) mustBe
        Left(EncryptionDecryptionError(
        "Failed encrypting data"
        ))
    }
  }

  "the set indexes" should {

    "enforce uniqueness" in {
      val result: Either[Exception, InsertOneResult] = try {
        Right(await(tailoringRepo.collection.insertOne(EncryptedTailoringUserDataModel(
          nino, taxYear, List(EncryptedValue("test", "test"))
        )).toFuture()))
      } catch {
        case e: Exception => Left(e)
      }
      result.isLeft mustBe true
      result.left.e.swap.getOrElse(new Exception("wrong message")).getMessage must include(
        "E11000 duplicate key error collection: income-tax-submission-frontend.tailoringUserData index: UserDataLookupIndex dup key:")
    }

  }

  "clear" should {

    "clear the document for the current user" in new EmptyDatabase{
      count shouldBe 0
      await(tailoringRepo.create(TailoringUserDataModel(nino, taxYear, List("gif"))))
      count shouldBe 1
      await(tailoringRepo.clear(taxYear))
      count shouldBe 0
    }
  }

}
