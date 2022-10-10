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

package itUtils

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import models.mongo.{DatabaseError, TailoringUserDataModel}
import models.{LiabilityCalculationIdModel, User}
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.api.test.Helpers.OK
import repositories.TailoringUserDataRepository

trait OverviewPageHelpers extends IntegrationTest with ViewHelpers {

  object Links {
    def startPageBreadcrumbUrl(taxYear: Int = taxYear): String = s"/update-and-submit-income-tax-return/$taxYear/start"

    def dividendsLink(taxYear: Int = taxYear): String =
      s"http://localhost:9308/update-and-submit-income-tax-return/personal-income/$taxYear/dividends/dividends-from-uk-companies"

    def dividendsLinkWithPriorData(taxYear: Int = taxYear): String =
      s"http://localhost:9308/update-and-submit-income-tax-return/personal-income/$taxYear/dividends/check-income-from-dividends"

    def interestsLink(taxYear: Int = taxYear): String =
      s"http://localhost:9308/update-and-submit-income-tax-return/personal-income/$taxYear/interest/untaxed-uk-interest"

    def interestsLinkWithPriorData(taxYear: Int = taxYear): String =
      s"http://localhost:9308/update-and-submit-income-tax-return/personal-income/$taxYear/interest/check-interest"

    def interestsTailoringGatewayLink(taxYear: Int = taxYear): String =
      s"http://localhost:9308/update-and-submit-income-tax-return/personal-income/$taxYear/interest/interest-from-UK"

    def dividendsTailoringGatewayLink(taxYear: Int = taxYear): String =
      s"http://localhost:9308/update-and-submit-income-tax-return/personal-income/$taxYear/dividends/dividends-from-stocks-and-shares"

    def giftAidTailoringGatewayLink(taxYear: Int = taxYear): String =
      s"http://localhost:9308/update-and-submit-income-tax-return/personal-income/$taxYear/charity/charity-donations-to-charity"

    def employmentLink(taxYear: Int = taxYear): String = s"http://localhost:9317/update-and-submit-income-tax-return/employment-income/$taxYear/employment-summary"

    def cisLink(taxYear: Int = taxYear): String = s"http://localhost:9338/update-and-submit-income-tax-return/construction-industry-scheme-deductions/$taxYear/summary"

    def pensionsLink(taxYear: Int = taxYear): String = s"http://localhost:9321/update-and-submit-income-tax-return/pensions/$taxYear/pensions-summary"

    def stateBenefitsLink(taxYear: Int = taxYear): String = s"http://localhost:9376/update-and-submit-income-tax-return/state-benefits/$taxYear/summary"

    def viewEstimateLink(taxYear: Int = taxYear): String = s"/update-and-submit-income-tax-return/$taxYear/calculate"

    def viewAndChangeLinkInYear(isAgent: Boolean): String = if (isAgent) {
      "http://localhost:9081/report-quarterly/income-and-expenses/view/agents/tax-overview"
    }
    else {
      "http://localhost:9081/report-quarterly/income-and-expenses/view/tax-overview"
    }

    def viewAndChangeLink(isAgent: Boolean): String = if (isAgent) {
      "http://localhost:9081/report-quarterly/income-and-expenses/view/agents"
    }
    else {
      s"http://localhost:9081/report-quarterly/income-and-expenses/view$vcPtaNavBarOrigin"
    }

    val endOfYearContinueLink = s"/update-and-submit-income-tax-return/$taxYearEOY/final-calculation"

    def addSectionsLink(taxYear: Int): String = s"/update-and-submit-income-tax-return/$taxYear/add-sections"

  }

  def stubLiabilityCalculation(response: Option[LiabilityCalculationIdModel], returnStatus: Int = OK): StubMapping = {
    stubGet(s"/income-tax-calculation/income-tax/nino/AA123456A/taxYear/$taxYear/tax-calculation\\?crystallise=true", returnStatus, Json.toJson(response).toString())
  }

  def stubLiabilityCalculationEOY(response: Option[LiabilityCalculationIdModel], returnStatus: Int = OK): StubMapping = {
    stubGet(s"/income-tax-calculation/income-tax/nino/AA123456A/taxYear/$taxYearEOY/tax-calculation\\?crystallise=true", returnStatus, Json.toJson(response).toString())
  }

  lazy val repo: TailoringUserDataRepository = app.injector.instanceOf[TailoringUserDataRepository]

  implicit val mtdUser: User[AnyContent] = User("1234567890", None, "AA123456A", "1234567890")

  def cleanDatabase(taxYear: Int): Seq[String] = {
    await(repo.clear(taxYear))
    await(repo.ensureIndexes)
  }

  def insertJourneys(endOfYear: Boolean, journeys: String*): Either[DatabaseError, Boolean] = {
    await(repo.create(TailoringUserDataModel(
      "AA123456A",
      if (endOfYear) taxYearEOY else taxYear,
      journeys
    )))
  }

  def insertAllJourneys(endOfYear: Boolean = false): Either[DatabaseError, Boolean] = {
    insertJourneys(
      endOfYear,
      "dividends",
      "interest",
      "gift-aid",
      "employment",
      "cis"
    )
  }

  def stubIncomeSources: StubMapping =
    stubGet(s"/income-tax-submission-service/income-tax/nino/AA123456A/sources\\?taxYear=$taxYear", OK, Json.toJson(incomeSourcesModel).toString)

  def stubIncomeSourcesEndOfYear: StubMapping =
    stubGet(s"/income-tax-submission-service/income-tax/nino/AA123456A/sources\\?taxYear=$taxYearEOY", OK, Json.toJson(incomeSourcesModel).toString)
}
