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

package services

import connectors.IncomeSourcesConnector
import connectors.httpparsers.IncomeSourcesHttpParser.{IncomeSourcesInvalidJsonException, IncomeSourcesResponse}
import uk.gov.hmrc.http.HeaderCarrier
import utils.UnitTest

import scala.concurrent.Future

class IncomeSourcesServiceSpec extends UnitTest {

  val connector = mock[IncomeSourcesConnector]
  val service: IncomeSourcesService = new IncomeSourcesService(connector)

  ".getIncomeSources" should {

    "return the connector response" in {
      val expectedResult: IncomeSourcesResponse = Left(IncomeSourcesInvalidJsonException)

      (connector.getIncomeSources(_: String, _: Int, _: String)(_: HeaderCarrier))
        .expects("123456789",1999, "987654321", *)
        .returning(Future.successful(expectedResult))

      val result = await(service.getIncomeSources("123456789", 1999, "987654321"))

      result shouldBe expectedResult
    }
  }

}
