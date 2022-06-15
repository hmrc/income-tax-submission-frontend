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

package controllers

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import common.IncomeSources._
import itUtils.{IntegrationTest, ViewHelpers}
import models.mongo.{DatabaseError, ExclusionUserDataModel}
import models.{ExcludeJourneyModel, IncomeSourcesModel}
import play.api.http.HeaderNames
import play.api.http.Status.OK
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.writeableOf_AnyContentAsEmpty
import repositories.ExclusionUserDataRepository
import utils.ShaHashHelper

class OverviewExclusionISpec extends IntegrationTest with ViewHelpers with ShaHashHelper {

  private lazy val repo: ExclusionUserDataRepository = app.injector.instanceOf[ExclusionUserDataRepository]

  val completeExclusion: Seq[ExcludeJourneyModel] =
    Seq(ExcludeJourneyModel(DIVIDENDS,None),ExcludeJourneyModel(CIS,None),ExcludeJourneyModel(EMPLOYMENT,None),
      ExcludeJourneyModel(INTEREST, Some(sha256Hash(incomeSourcesModel.interest.get.toString()))),
      ExcludeJourneyModel(GIFT_AID, Some(sha256Hash(incomeSourcesModel.giftAid.get.toString())))
  )

  private def insertJourneys(journeys: Seq[ExcludeJourneyModel]): Either[DatabaseError, Boolean] = {
    await(repo.create(ExclusionUserDataModel(
      "AA123456A",
      taxYear,
      journeys
    )))
  }

  private def cleanDatabase(taxYear: Int) = {
    await(repo.clear(taxYear))
    await(repo.ensureIndexes)
  }

  def stubIncomeSources(incomeSources: IncomeSourcesModel): StubMapping = {
    stubGet(s"/income-tax-submission-service/income-tax/nino/AA123456A/sources\\?taxYear=$taxYear", OK, Json.toJson(incomeSources).toString())
  }

  val urlPathInYear = s"/update-and-submit-income-tax-return/$taxYear/view"
  val headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear, validTaxYearList), "Csrf-Token" -> "nocheck")

  ".show" should {
    "clear exclusion data" when {
      "all criteria to remove journeys from exclusion are met" in {
        val request = FakeRequest("GET", urlPathInYear).withHeaders(headers: _*)

        cleanDatabase(taxYear)
        insertJourneys(completeExclusion)
        await({
          authoriseAgentOrIndividual(user.isAgent)
          stubIncomeSources(incomeSourcesModel)
          route(app, request, false).get
        })

        await(repo.find(taxYear)).right.get.get.exclusionModel shouldBe Seq.empty[ExcludeJourneyModel]
      }
    }
    "ignore exclusion data" when {
      "no criteria to remove journeys from exclusion is met" in {
        val request = FakeRequest("GET", urlPathInYear).withHeaders(headers: _*)

        cleanDatabase(taxYear)
        insertJourneys(completeExclusion)

        await({
          authoriseAgentOrIndividual(user.isAgent)
          stubIncomeSources(incomeSourcesModel.copy(None, None, None, None, None))
          route(app, request, false).get
        })



        await(repo.find(taxYear)).right.get.get.exclusionModel shouldBe (completeExclusion.toVector)
      }
    }


  }
}
