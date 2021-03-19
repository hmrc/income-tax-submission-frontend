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

package services

import connectors.CalculationIdConnector
import connectors.httpParsers.CalculationIdHttpParser.CalculationIdResponse
import models.LiabilityCalculationIdModel
import uk.gov.hmrc.http.HeaderCarrier
import utils.UnitTest

import scala.concurrent.Future

class CalculationIdServiceSpec extends UnitTest {

  val connector: CalculationIdConnector = mock[CalculationIdConnector]
  val service: CalculationIdService = new CalculationIdService(connector)

  ".getIncomeSources" should {

    "return the connector response" in {
      val responseBody = LiabilityCalculationIdModel("calculationId")
      val expectedResult: CalculationIdResponse = Right(responseBody)

      (connector.getCalculationId(_: String, _: Int, _: String)(_: HeaderCarrier))
        .expects("123456789",1999, "987654321", *)
        .returning(Future.successful(expectedResult))

      val result = await(service.getCalculationId("123456789", 1999, "987654321"))

      result shouldBe expectedResult
    }
  }

}
