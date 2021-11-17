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

package connectors

import com.github.tomakehurst.wiremock.http.HttpHeader
import itUtils.IntegrationTest
import models.{APIErrorBodyModel, APIErrorModel, NrsSubmissionModel}
import play.mvc.Http.Status._
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}

class NrsConnectorSpec extends IntegrationTest {

  lazy val connector: NrsConnector = app.injector.instanceOf[NrsConnector]

  val nino: String = "AA123456A"
  val taxYear: Int = 2019
  val mtditid: String = "1234567890"

  val nrsSubmissionModel: NrsSubmissionModel = NrsSubmissionModel("1sfg34gh-87b9-4d4a-a296-72w2gdg8357a")
  val expectedHeaders = Seq(new HttpHeader("mtditid", mtditid))
  val response: String = "1sfg34gh-87b9-4d4a-a296-72w2gdg8357a"

  val url: String = s"/income-tax-nrs-proxy/$nino/itsa-crystallisation"

  ".NrsConnector" should {

    "return an Accepted response when successful" in {

      stubPost(url, ACCEPTED, "{}")
      val result = await(connector.postNrsConnector(nino, nrsSubmissionModel))

      result shouldBe Right()
    }

    "return an InternalServerError" in {

      val expectedResult = APIErrorModel(INTERNAL_SERVER_ERROR, APIErrorBodyModel("INTERNAL_SERVER_ERROR", "Internal Server Error"))

      stubPost(url, INTERNAL_SERVER_ERROR, expectedResult.toJson.toString())
      val result = await(connector.postNrsConnector(nino, nrsSubmissionModel))

      result shouldBe Left(expectedResult)
    }

    "return a NotFound error" in {

      val expectedResult = APIErrorModel(NOT_FOUND, APIErrorBodyModel("NOT_FOUND", "NRS returning not found error"))

      stubPost(url, NOT_FOUND, expectedResult.toJson.toString())
      val result = await(connector.postNrsConnector(nino, nrsSubmissionModel))

      result shouldBe Left(expectedResult)
    }

    "return a ParsingError when an unexpected error has occurred" in {

      val expectedResult = APIErrorModel(CONFLICT, APIErrorBodyModel("PARSING_ERROR", "Error parsing response from API"))

      stubPost(url, CONFLICT, expectedResult.toJson.toString())
      val result = await(connector.postNrsConnector(nino, nrsSubmissionModel))

      result shouldBe Left(expectedResult)
    }

  }
}
