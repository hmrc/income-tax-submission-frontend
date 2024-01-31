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

import connectors.httpParsers.CalculationDetailsHttpParser.CalculationDetailResponse
import connectors.{IncomeTaxCalculationConnector, LiabilityCalculationConnector}
import connectors.httpParsers.LiabilityCalculationHttpParser.LiabilityCalculationResponse
import models.LiabilityCalculationIdModel
import models.calculation.{CalculationResponseModel, Inputs, Metadata, PersonalInformation}
import uk.gov.hmrc.http.HeaderCarrier
import utils.UnitTest

import scala.concurrent.{ExecutionContext, Future}
import java.time.LocalDate

class LiabilityCalculationServiceSpec extends UnitTest {

  val connector: LiabilityCalculationConnector = mock[LiabilityCalculationConnector]
  val incomeTaxCalculationConnector: IncomeTaxCalculationConnector = mock[IncomeTaxCalculationConnector]
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

  val service: LiabilityCalculationService = new LiabilityCalculationService(connector, incomeTaxCalculationConnector)
  val calculationResponseModel: CalculationResponseModel = CalculationResponseModel(
    inputs = Inputs(PersonalInformation(taxRegime = "UK", class2VoluntaryContributions = None)),
    messages = None,
    metadata = Metadata(Some("2019-02-15T09:35:15.094Z"), Some(false), Some("customerRequest"), None, None),
    calculation = None)


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
  val mtditid = "1234567890"
  val nino = "AA123456A"
  val calculationId = "041f7e4d-87b9-4d4a-a296-3cfbdf92f7e2"
  private val dateNow: LocalDate = LocalDate.now()
  private val taxYearCutoffDate: LocalDate = LocalDate.parse(s"${dateNow.getYear}-04-05")

  val taxYear: Int = if (dateNow.isAfter(taxYearCutoffDate)) LocalDate.now().getYear + 1 else LocalDate.now().getYear
  val taxYearEOY: Int = taxYear - 1

  ".getCalculationDetailsByCalcId" should {

    "return the connector response" in {

      val expectedResult: CalculationDetailResponse = Right(calculationResponseModel)

      (incomeTaxCalculationConnector.getCalculationResponseByCalcId(_:String,_:String,_:String,_:Int)(_: HeaderCarrier,_:ExecutionContext))
        .expects(mtditid,nino,calculationId, taxYearEOY,headerCarrierWithSession,ec)
        .returning(Future.successful(expectedResult))

      val result = await(service.getCalculationDetailsByCalcId(mtditid,nino,calculationId,taxYearEOY))

      result shouldBe expectedResult
    }

  }

}
