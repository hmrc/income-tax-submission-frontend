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

import itUtils.IntegrationTest
import models.User
import models.mongo._
import org.apache.pekko.Done
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.mvc.AnyContent
import play.api.test.{DefaultAwaitTimeout, FakeRequest, FutureAwaits}
import uk.gov.hmrc.crypto.EncryptedValue

class UserDataRepositoryISpec extends IntegrationTest with FutureAwaits with DefaultAwaitTimeout {


  val tailoringRepo: TailoringUserDataRepository = app.injector.instanceOf[TailoringUserDataRepository]

  val tailoringInvalidRepo: TailoringUserDataRepository = customApp(
    invalidEncryptionKey = true
  ).injector.instanceOf[TailoringUserDataRepository]

  private def count: Long = await(tailoringRepo.collection.countDocuments().toFuture())

  class EmptyDatabase {
    await(tailoringRepo.collection.drop().toFuture())
    await(tailoringRepo.ensureIndexes())
    count mustBe 0
  }

  val tailoringUserData: TailoringUserDataModel = TailoringUserDataModel(
    nino,
    taxYear,
    Seq("journey1", "journey2")
  )

  implicit val request: FakeRequest[AnyContent] = FakeRequest()

  "create" should {
    "add a document to the collection" in new EmptyDatabase {
      val result: Either[DatabaseError, Done] = await(tailoringRepo.create(tailoringUserData))
      result mustBe Right(Done)
      count mustBe 1
    }

    "fail to add a document to the collection when it already exists" in new EmptyDatabase {
      await(tailoringRepo.create(tailoringUserData))
      val result: Either[DatabaseError, Done] = await(tailoringRepo.create(tailoringUserData))
      result mustBe Left(DataNotUpdated)
      count mustBe 1
    }
  }

  "update" should {

    "update a document in the collection" in new EmptyDatabase {

      val testUser: User[AnyContent] = User(mtditid, None, nino, "individual", sessionId)(fakeRequest)

      val initialData: TailoringUserDataModel = TailoringUserDataModel(
        testUser.nino, taxYear,
        List("itr","div")
      )

      val newUserData: TailoringUserDataModel = initialData.copy(tailoring = List("gif","emp"))

      await(tailoringRepo.create(initialData))
      count mustBe 1

      val res: Either[DatabaseError, Done] = await(tailoringRepo.update(newUserData))
      res mustBe Right(Done)
      count mustBe 1

      val data: Either[DatabaseError, Option[TailoringUserDataModel]] = await(tailoringRepo.find(taxYear)(testUser))
      data.map(_.map(_.tailoring)) shouldBe Right(Some(List("gif","emp")))
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

    val testUser = User(mtditid, None, nino, "individual", sessionId)(fakeRequest)

    "get a document" in {
      count mustBe 1
      val dataAfter: Either[DatabaseError, Option[TailoringUserDataModel]] = await(tailoringRepo.find(taxYear)(testUser))
      dataAfter.map(_.map(_.tailoring)) mustBe Right(Some(List("gif","emp")))
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
      val result =
        intercept[Throwable](await(tailoringRepo.collection.insertOne(EncryptedTailoringUserDataModel(
          nino, taxYear, List(EncryptedValue("test", "test"))
        )).toFuture()))

      result.getMessage must include(
        "E11000 duplicate key error collection: income-tax-submission-frontend.tailoringUserData index: UserDataLookupIndex dup key:")
    }
  }

  "clear" should {

    "clear the document for the current user" in new EmptyDatabase{
      await(tailoringRepo.create(TailoringUserDataModel(nino, taxYear, List("gif"))))
      count shouldBe 1
      await(tailoringRepo.clear(taxYear))
      count shouldBe 0
    }
  }
}
