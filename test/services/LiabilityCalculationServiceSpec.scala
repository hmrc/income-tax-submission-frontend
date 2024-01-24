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

import connectors.LiabilityCalculationConnector
import connectors.httpParsers.LiabilityCalculationHttpParser.LiabilityCalculationResponse
import models.LiabilityCalculationIdModel
import uk.gov.hmrc.http.HeaderCarrier
import utils.UnitTest

import scala.concurrent.Future

class LiabilityCalculationServiceSpec extends UnitTest {

  val connector: LiabilityCalculationConnector = mock[LiabilityCalculationConnector]
  val service: LiabilityCalculationService = new LiabilityCalculationService(connector)

  ".getCalculationId" should {

    "return the connector response" in {
      val responseBody = LiabilityCalculationIdModel("calculationId")
      val expectedResult: LiabilityCalculationResponse = Right(responseBody)

      (connector.getCalculationId(_: String, _: Int)(_: HeaderCarrier))
        .expects("123456789",1999, headerCarrierWithSession.withExtraHeaders("mtditid"->"987654321"))
        .returning(Future.successful(expectedResult))

      val result = await(service.getCalculationId("123456789", 1999, "987654321"))

      result shouldBe expectedResult
    }

  }

  ".getIntentToCrystallise" should {

    "return the connector response" in {
      val responseBody = LiabilityCalculationIdModel("calculationId")
      val expectedResult: LiabilityCalculationResponse = Right(responseBody)

      (connector.getIntentToCrystallise(_: String, _: Int)(_: HeaderCarrier))
        .expects("123456789",1999, headerCarrierWithSession.withExtraHeaders("mtditid"->"987654321"))
        .returning(Future.successful(expectedResult))

      val result = await(service.getIntentToCrystallise("123456789", 1999, "987654321"))

      result shouldBe expectedResult
    }

  }

}
