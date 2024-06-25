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

package services

import connectors.TaskListConnector
import connectors.httpParsers.TaskListHttpParser.TaskListResponse
import uk.gov.hmrc.http.HeaderCarrier
import utils.UnitTest

import scala.concurrent.Future

class TaskListServiceSpec extends UnitTest {

  val connector: TaskListConnector = mock[TaskListConnector]
  val service: TaskListService = new TaskListService(connector)
  val invalidTaxYear: Int = 1999
  val nino: String = "AA123456A"

  ".getTaskList" should {

    "return 500 error for invalid tax year from connector response" in {
      val expectedResult: TaskListResponse = Left(error500)

      (connector.getTaskList(_: String, _: Int)(_: HeaderCarrier))
        .expects(nino, invalidTaxYear, headerCarrierWithSession)
        .returning(Future.successful(expectedResult))

      val result = await(service.getTaskList(nino, invalidTaxYear))

      result shouldBe expectedResult
    }
  }

}
