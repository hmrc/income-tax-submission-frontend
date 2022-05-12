/*
 * Copyright 2022 HM Revenue & Customs
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

import connectors.ValidTaxYearListConnector
import connectors.httpParsers.ValidTaxYearListHttpParser.ValidTaxYearListResponse
import models.ValidTaxYearListModel
import uk.gov.hmrc.http.HeaderCarrier
import utils.UnitTest

import scala.concurrent.Future

class ValidTaxYearListServiceSpec extends UnitTest {

  val connector: ValidTaxYearListConnector = mock[ValidTaxYearListConnector]
  val service: ValidTaxYearListService = new ValidTaxYearListService(connector)

  private val validTaxYearList: ValidTaxYearListModel = ValidTaxYearListModel(Seq(2022, 2023))

  ".getValidTaxYearList" should {

    "return the connector response" in {
      val responseBody = validTaxYearList
      val expectedResult: ValidTaxYearListResponse = Right(responseBody)

      (connector.getValidTaxYearList(_: String)(_: HeaderCarrier))
        .expects("123456789", headerCarrierWithSession.withExtraHeaders("mtditid"->"987654321"))
        .returning(Future.successful(expectedResult))

      val result = await(service.getValidTaxYearList("123456789", "987654321"))

      result shouldBe expectedResult
    }

  }
  
}
