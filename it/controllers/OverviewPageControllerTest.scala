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

package controllers

import common.{EnrolmentIdentifiers, EnrolmentKeys}
import config.{AppConfig, ErrorHandler}
import itUtils.IntegrationTest
import models.{DividendsModel, InterestModel}
import play.api.libs.ws.WSClient
import play.api.test.FakeRequest
import play.api.test.Helpers.{OK, SEE_OTHER, UNAUTHORIZED}
import services.IncomeSourcesService
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.auth.core.syntax.retrieved.authSyntaxForRetrieved
import uk.gov.hmrc.auth.core.{Enrolment, _}
import views.html.OverviewPageView

import scala.concurrent.Future

class OverviewPageControllerTest extends IntegrationTest {

  private val taxYear = 2021

  lazy val frontendAppConfig: AppConfig = app.injector.instanceOf[AppConfig]

  lazy val wsClient: WSClient = app.injector.instanceOf[WSClient]

  def controller(stubbedRetrieval: Future[_]): OverviewPageController = new OverviewPageController(
    frontendAppConfig,
    mcc,
    scala.concurrent.ExecutionContext.Implicits.global,
    app.injector.instanceOf[IncomeSourcesService],
    app.injector.instanceOf[OverviewPageView],
    authAction(stubbedRetrieval),
    app.injector.instanceOf[ErrorHandler]
  )

  "Hitting the show endpoint" should {

    s"return an OK ($OK) and no prior data" when {

      "all auth requirements are met" in {
        val retrieval: Future[Enrolments ~ Some[AffinityGroup] ~ ConfidenceLevel] = Future.successful(
          Enrolments(Set(
            Enrolment("HMRC-MTD-IT", Seq(EnrolmentIdentifier("MTDITID", "1234567890")), "Activated", None),
            Enrolment(EnrolmentKeys.nino, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.ninoId, "AA123456A")), "Activated")
          )) and Some(AffinityGroup.Individual) and ConfidenceLevel.L200
        )

        stubGet("/income-tax-submission-service/income-tax/nino/AA123456A/sources\\?taxYear=2021&mtditid=1234567890", OK, "{}")

        val result = await(controller(retrieval).show(taxYear)(FakeRequest()))

        result.header.status shouldBe OK
      }

    }

    s"return an OK ($OK) with prior data" when {

      "all auth requirements are met" in {
        val retrieval: Future[Enrolments ~ Some[AffinityGroup] ~ ConfidenceLevel] = Future.successful(
          Enrolments(Set(
            Enrolment("HMRC-MTD-IT", Seq(EnrolmentIdentifier("MTDITID", "1234567890")), "Activated", None),
            Enrolment(EnrolmentKeys.nino, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.ninoId, "AA123456A")), "Activated")
          )) and Some(AffinityGroup.Individual) and ConfidenceLevel.L200
        )

        stubGet("/income-tax-submission-service/income-tax/nino/AA123456A/sources\\?taxYear=2021&mtditid=1234567890", OK,
          """{
            |	"dividends": {
            |		"ukDividends": 69.99,
            |		"otherUkDividends": 63.99
            |	},
            |	"interest": [{
            |		"accountName": "BANK",
            |		"incomeSourceId": "12345678908765432",
            |		"taxedUkInterest": 44.66,
            |		"untaxedUkInterest": 66.44
            |	}]
            |}""".stripMargin)

        val result = await(controller(retrieval).show(taxYear)(FakeRequest()))

        result.header.status shouldBe OK
      }

    }

    s"return an UNAUTHORISED ($UNAUTHORIZED)" when {

      "the confidence level is too low" in {
        val retrieval: Future[Enrolments ~ Some[AffinityGroup] ~ ConfidenceLevel] = Future.successful(
          Enrolments(Set(Enrolment("HMRC-MTD-IT", Seq(EnrolmentIdentifier("MTDITID", "1234567890")), "Activated", None))) and
          Some(AffinityGroup.Individual) and ConfidenceLevel.L50
        )

        val result = await(controller(retrieval).show(taxYear)(FakeRequest()))

        result.header.status shouldBe SEE_OTHER
        result.header.headers shouldBe Map("Location" -> "/income-through-software/return/iv-uplift")
      }

      "it contains the wrong credentials" in {
        val retrieval: Future[Enrolments ~ Some[AffinityGroup] ~ ConfidenceLevel] = Future.successful(
          Enrolments(Set(
            Enrolment("HMRC-MTD-IT", Seq(EnrolmentIdentifier("UTR", "1234567890")), "Activated", None),
            Enrolment(EnrolmentKeys.nino, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.ninoId, "AA123456A")), "Activated")
          )) and
          Some(AffinityGroup.Individual) and ConfidenceLevel.L200
        )

        val result = await(controller(retrieval).show(taxYear)(FakeRequest()))

        result.header.status shouldBe UNAUTHORIZED
      }

    }

  }

}
