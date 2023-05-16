/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers.predicates

import common.SessionValues
import config.{AppConfig, ErrorHandler}
import models.{User, ValidTaxYearListModel}
import play.api.http.Status.{INTERNAL_SERVER_ERROR, SEE_OTHER}
import play.api.i18n.MessagesApi
import play.api.mvc.Request
import play.api.mvc.Results.InternalServerError
import services.ValidTaxYearListService
import uk.gov.hmrc.http.HeaderCarrier
import utils.UnitTest

import scala.concurrent.Future

class TaxYearActionSpec extends UnitTest {
  val validTaxYearList: Seq[Int] = Seq(2021, 2022, 2023)
  val validTaxYear: Int = 2022
  val invalidTaxYear: Int = 3000

  implicit lazy val mockedConfig: AppConfig = mock[AppConfig]
  implicit lazy val cc: MessagesApi = mockControllerComponents.messagesApi
  implicit lazy val mockedService: ValidTaxYearListService = mock[ValidTaxYearListService]
  implicit lazy val mockedErrorHandler: ErrorHandler = mock[ErrorHandler]

  def taxYearAction(taxYear: Int, reset: Boolean = true): TaxYearAction = new TaxYearAction(taxYear, reset)

  "TaxYearAction.refine" should {

    "return a Right(request)" when {

      "the tax year is within range of allowed years, and matches that in session if the feature switch is on" in {
        lazy val userRequest = User("1234567890", None, "AA123456A", sessionId)(
          fakeRequest.withSession(SessionValues.TAX_YEAR -> validTaxYear.toString, SessionValues.VALID_TAX_YEARS -> validTaxYearList.mkString(","))
        )

        lazy val result = {
          (() => mockedConfig.taxYearErrorFeature).expects() returning true

          await(taxYearAction(validTaxYear).refine(userRequest))
        }

        result.isRight shouldBe true
      }

      "the tax year is equal to the session value if the feature switch is off" in {
        lazy val userRequest = User("1234567890", None, "AA123456A", sessionId)(
          fakeRequest.withSession(SessionValues.TAX_YEAR -> (validTaxYear + 1).toString, SessionValues.VALID_TAX_YEARS -> validTaxYearList.mkString(","))
        )

        lazy val result = {
          (() => mockedConfig.taxYearErrorFeature).expects() returning false
          await(taxYearAction(validTaxYear + 1).refine(userRequest))
        }

        result.isRight shouldBe true
      }

      "the tax year is different to the session value if the reset variable input is false" in {
        lazy val userRequest = User("1234567890", None, "AA123456A", sessionId)(
          fakeRequest.withSession(SessionValues.TAX_YEAR -> validTaxYear.toString, SessionValues.VALID_TAX_YEARS -> validTaxYearList.mkString(","))
        )

        lazy val result = {
          (() => mockedConfig.taxYearErrorFeature).expects() returning false

          await(taxYearAction(validTaxYear + -1, reset = false).refine(userRequest))
        }

        result.isRight shouldBe true
      }

    }

    "return a Right(result) with the Valid Tax Year List In Session" when {

      "the tax year is different from that in session and the feature switch is off" which {
        lazy val userRequest = User("1234567890", None, "AA123456A", sessionId)(
          fakeRequest.withSession(SessionValues.TAX_YEAR -> validTaxYear.toString, SessionValues.VALID_TAX_YEARS -> validTaxYearList.mkString(","))
        )

        lazy val result = {
          (() => mockedConfig.taxYearErrorFeature).expects() returning false
          taxYearAction(validTaxYear + 1).refine(userRequest)
        }

        "has a status of SEE_OTHER (303)" in {
          status(result.map(_.left.toOption.get)) shouldBe SEE_OTHER
        }

        "has the start page redirect url" in {
          redirectUrl(result.map(_.left.toOption.get)) shouldBe controllers.routes.StartPageController.show(validTaxYear + 1).url
        }

        "has an updated tax year session value" in {
          await(result.map(_.left.toOption.get)).session.get(SessionValues.TAX_YEAR).get shouldBe (validTaxYear + 1).toString
        }
      }

      "the tax year is outside of the allowed limit while the feature switch is on" which {
        lazy val userRequest = User("1234567890", None, "AA123456A", sessionId)(
          fakeRequest.withSession(SessionValues.TAX_YEAR -> (validTaxYear + 4).toString, SessionValues.VALID_TAX_YEARS -> validTaxYearList.mkString(","))
        )

        lazy val result = {
          (() => mockedConfig.taxYearErrorFeature).expects() returning true

          taxYearAction(validTaxYear + 4).refine(userRequest)
        }

        "has a status of SEE_OTHER (303)" in {
          status(result.map(_.left.toOption.get)) shouldBe SEE_OTHER
        }

        "has the tax year error redirect url" in {
          redirectUrl(result.map(_.left.toOption.get)) shouldBe controllers.routes.TaxYearErrorController.show.url
        }
      }
    }

    "return a Right(result) without a Valid Tax Year List In Session" when {

      "the tax year is different from that in session and the feature switch is off" which {
        lazy val userRequest = User("1234567890", None, "AA123456A", sessionId)(
          fakeRequest.withSession(SessionValues.TAX_YEAR -> validTaxYear.toString)
        )

        lazy val result = {
          (() => mockedConfig.taxYearErrorFeature).expects() returning false

          (mockedService.getValidTaxYearList(_:String, _:String)(_:HeaderCarrier))
            .expects("AA123456A", "1234567890", *)
            .returning(Future.successful(Right(ValidTaxYearListModel(validTaxYearList))))

          taxYearAction(validTaxYear + 1).refine(userRequest)
        }



        "has a status of SEE_OTHER (303)" in {
          status(result.map(_.left.toOption.get)) shouldBe SEE_OTHER
        }

        "has the start page redirect url" in {
          redirectUrl(result.map(_.left.toOption.get)) shouldBe controllers.routes.StartPageController.show(validTaxYear + 1).url
        }

        "has an updated tax year session value" in {
          await(result.map(_.left.toOption.get)).session.get(SessionValues.TAX_YEAR).get shouldBe (validTaxYear + 1).toString
        }
      }

      "the tax year is outside of the allowed limit while the feature switch is on" which {
        lazy val userRequest = User("1234567890", None, "AA123456A", sessionId)(
          fakeRequest.withSession(SessionValues.TAX_YEAR -> (validTaxYear + 4).toString)
        )

        lazy val result = {
          (() => mockedConfig.taxYearErrorFeature).expects() returning true

          (mockedService.getValidTaxYearList(_:String, _:String)(_:HeaderCarrier))
            .expects("AA123456A", "1234567890", *)
            .returning(Future.successful(Right(ValidTaxYearListModel(validTaxYearList))))
          taxYearAction(validTaxYear + 4).refine(userRequest)
        }

        "has a status of SEE_OTHER (303)" in {
          status(result.map(_.left.toOption.get)) shouldBe SEE_OTHER
        }

        "has the tax year error page redirect url" in {
          redirectUrl(result.map(_.left.toOption.get)) shouldBe controllers.routes.TaxYearErrorController.show.url
        }
      }

      "there is a downstream internal server error whilst retrieving the list of valid tax years" which {
        lazy val userRequest = User("1234567890", None, "AA123456A", sessionId)(
          fakeRequest.withSession(SessionValues.TAX_YEAR -> validTaxYear.toString)
        )

        lazy val result = {
          (mockedService.getValidTaxYearList(_:String, _:String)(_:HeaderCarrier))
            .expects("AA123456A", "1234567890", *)
            .returning(Future.successful(Left(error500)))
          (mockedErrorHandler.handleError(_:Int)(_:Request[_])).expects(INTERNAL_SERVER_ERROR ,* ).returning(InternalServerError)
          taxYearAction(validTaxYear).refine(userRequest)
        }

        "has a status of INTERNAL_SERVER_ERROR (500)" in {
          status(result.map(_.left.toOption.get)) shouldBe INTERNAL_SERVER_ERROR
        }

      }
    }

  }

}
