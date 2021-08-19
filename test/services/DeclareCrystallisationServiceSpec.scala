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

import connectors.DeclareCrystallisationConnector
import connectors.httpParsers.DeclareCrystallisationHttpParser.DeclareCrystallisationResponse
import uk.gov.hmrc.http.HeaderCarrier
import utils.UnitTest

import scala.concurrent.Future

class DeclareCrystallisationServiceSpec extends UnitTest {

  val connector: DeclareCrystallisationConnector = mock[DeclareCrystallisationConnector]
  val service: DeclareCrystallisationService = new DeclareCrystallisationService(connector)


  val nino: String = "123456789"
  val taxYear: Int = 1999
  val mtditid: String = "968501689"
  val calculationId: String = "041f7e4d-87b9-4d4a-a296-3cfbdf92f7e2"


  ".postDeclareCrystallisation" should {

    "return the connector response" in {
      val expectedResult: DeclareCrystallisationResponse = Right(())

      (connector.postDeclareCrystallisation(_: String, _: Int, _: String)(_: HeaderCarrier))
        .expects(nino, taxYear, calculationId, headerCarrierWithSession.withExtraHeaders("mtditid"-> mtditid))
        .returning(Future.successful(expectedResult))

      val result = await(service.postDeclareCrystallisation(nino, taxYear,
        calculationId, mtditid))

      result shouldBe expectedResult
    }

  }

}
