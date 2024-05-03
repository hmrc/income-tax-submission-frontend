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

  val connector = mock[TaskListConnector]
  val service: TaskListService = new TaskListService(connector)
  val taxYear: Int = 1999

  ".getTaskList" should {

    "return the connector response" in {
      val expectedResult: TaskListResponse = Left(error500)

      (connector.getTaskList(_: Int)(_: HeaderCarrier))
        .expects(taxYear, headerCarrierWithSession)
        .returning(Future.successful(expectedResult))

      val result = await(service.getTaskList(taxYear))

      result shouldBe expectedResult
    }
  }

}
