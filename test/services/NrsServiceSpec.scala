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

import connectors.NrsConnector
import connectors.httpParsers.NrsSubmissionHttpParser.NrsSubmissionResponse
import models.NrsSubmissionModel
import uk.gov.hmrc.http.HeaderCarrier
import utils.UnitTest

import scala.concurrent.Future

class NrsServiceSpec extends UnitTest {

  val connector: NrsConnector = mock[NrsConnector]
  val service: NrsService = new NrsService(connector)

  val nino: String = "AA123456A"
  val taxYear: Int = 2020
  val mtditid: String = "968501689"
  val calculationId: String = "041f7e4d-87b9-4d4a-a296-3cfbdf92f7e2"

  val nrsSubmissionModel: NrsSubmissionModel = NrsSubmissionModel(calculationId)

  ".postNrsConnector" when {
    
    "there is a true client ip and port" should {
      
      "return the connector response" in {

        val expectedResult: NrsSubmissionResponse = Right()

        val headerCarrierWithTrueClientDetails = headerCarrierWithSession.copy(trueClientIp = Some("127.0.0.1"), trueClientPort = Some("80"))

        (connector.postNrsConnector(_: String, _: NrsSubmissionModel)(_: HeaderCarrier))
          .expects(nino, nrsSubmissionModel, headerCarrierWithTrueClientDetails.withExtraHeaders("mtditid" -> mtditid, "clientIP" -> "127.0.0.1", "clientPort" -> "80"))
          .returning(Future.successful(expectedResult))

        val result = await(service.submit(nino, nrsSubmissionModel, mtditid)(headerCarrierWithTrueClientDetails))

        result shouldBe expectedResult
      }
    }
    
    "there isn't a true client ip and port" should {
      
      "return the connector response" in {

        val expectedResult: NrsSubmissionResponse = Right()

        (connector.postNrsConnector(_: String, _: NrsSubmissionModel)(_: HeaderCarrier))
          .expects(nino, nrsSubmissionModel, headerCarrierWithSession.withExtraHeaders("mtditid" -> mtditid))
          .returning(Future.successful(expectedResult))

        val result = await(service.submit(nino, nrsSubmissionModel, mtditid))

        result shouldBe expectedResult
      }
    }
    
  }

}
