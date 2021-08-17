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

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import itUtils.{IntegrationTest, ViewHelpers}
import models.IncomeSourcesModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.http.HeaderNames
import play.api.http.Status.OK
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.Helpers.{status, writeableOf_AnyContentAsEmpty}
import play.api.test.{FakeRequest, Helpers}

import scala.concurrent.Future

class ViewEstimationSectionISpec extends IntegrationTest with ViewHelpers  {
  val isInYear = true
  val taxYear = 2022

  object Selectors {
    val viewEstimationHeadingSelector = "#main-content > div > div > ol > li:nth-child(2) > h2"
    val viewEstimationLinkSelector = "#calculation_link"
    val viewEstimationParagraphSelector = "#main-content > div > div > ol > li.app-task-list__items > p"
  }

  trait CommonExpectedResults{
    val viewEstimationHeading: String
    val viewEstimationLink: String
  }

  trait SpecificExpectedResults{
    val viewEstimationParagraph: String
  }

  object CommonExpectedEN extends CommonExpectedResults {
    val viewEstimationHeading = "2. View tax calculation to date"
    val viewEstimationLink = "View tax calculation to date"
  }

  object CommonExpectedCY extends CommonExpectedResults {
    val viewEstimationHeading = "2. View tax calculation to date"
    val viewEstimationLink = "View tax calculation to date"
  }

  object ExpectedIndividualEN extends SpecificExpectedResults {
    val viewEstimationParagraph = "Provide at least one update before you can view your tax calculation to date."
  }

  object ExpectedAgentEN extends SpecificExpectedResults {
    val viewEstimationParagraph = "Provide at least one update before you can view your client’s tax calculation to date."
  }

  object ExpectedIndividualCY extends SpecificExpectedResults {
    val viewEstimationParagraph = "Provide at least one update before you can view your tax calculation to date."
  }

  object ExpectedAgentCY extends SpecificExpectedResults {
    val viewEstimationParagraph = "Provide at least one update before you can view your client’s tax calculation to date."
  }

  private val urlPath = s"/income-through-software/return/$taxYear/view"
  private val linkHref = "/income-through-software/return/2022/calculate"

  def stubIncomeSources(incomeSources: IncomeSourcesModel): StubMapping = {
    stubGet("/income-tax-submission-service/income-tax/nino/AA123456A/sources\\?taxYear=2022", OK, Json.toJson(incomeSources).toString())
  }

  lazy val emptyIncomeSourcesModel:IncomeSourcesModel = IncomeSourcesModel(
    dividends = None,
    interest = None,
    giftAid = None,
    employment = None
  )
  val allIncomeSources = (incomeSourcesModel, "all income sources in model")
  val interestModelOnly = (emptyIncomeSourcesModel.copy(interest = interestsModel), "interest")
  val dividendsModelOnly = (emptyIncomeSourcesModel.copy(dividends = dividendsModel), "dividends")
  val giftAidModelOnly = (emptyIncomeSourcesModel.copy(giftAid = Some(giftAidModel)), "giftAid")
  val employmentModelOnly = (emptyIncomeSourcesModel.copy(employment = Some(employmentsModel)), "employment")


  val incomeSources = Seq(allIncomeSources, interestModelOnly, dividendsModelOnly, giftAidModelOnly, employmentModelOnly)
  val releasableIncomeSources = Seq(giftAidModelOnly, employmentModelOnly)

  val userScenarios: Seq[UserScenario[CommonExpectedResults, SpecificExpectedResults]] = {
    Seq(UserScenario(isWelsh = false, isAgent = false, CommonExpectedEN, Some(ExpectedIndividualEN)),
      UserScenario(isWelsh = false, isAgent = true, CommonExpectedEN, Some(ExpectedAgentEN)),
      UserScenario(isWelsh = true, isAgent = false, CommonExpectedCY, Some(ExpectedIndividualCY)),
      UserScenario(isWelsh = true, isAgent = true, CommonExpectedCY, Some(ExpectedAgentCY)))
  }

  val headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck")
  val request = FakeRequest("GET", urlPath).withHeaders(headers: _*)

  "The view tax calculation link"  when {
    import Selectors._

    userScenarios.foreach { user =>
      import user.commonExpectedResults._
      import user.specificExpectedResults._

      s"language is ${welshTest(user.isWelsh)} and request is from an ${agentTest(user.isAgent)}" should {

        incomeSources.foreach { incomeSource =>
          s"should show when there is at least one employment source. The model here is ${incomeSource._2}" when {
            lazy val result: Future[Result] = {
              authoriseAgentOrIndividual(user.isAgent)
              stubIncomeSources(incomeSource._1)
              route(app, request, user.isWelsh).get
            }

            implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

            "returns status of OK(200)" in {
              status(result) shouldBe OK
            }
            textOnPageCheck(viewEstimationHeading, viewEstimationHeadingSelector)
            linkCheck(viewEstimationLink, viewEstimationLinkSelector, linkHref)
          }
        }

          "should show paragraphText when there are no Income Sources" when {
            lazy val result: Future[Result] = {
              authoriseAgentOrIndividual(user.isAgent)
              stubIncomeSources(emptyIncomeSourcesModel)
              route(app, request, user.isWelsh).get
            }

            "returns status of OK(200)" in {
              status(result) shouldBe OK
            }

            implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

            textOnPageCheck(viewEstimationHeading, viewEstimationHeadingSelector)
            textOnPageCheck(get.viewEstimationParagraph, viewEstimationParagraphSelector)
          }

        releasableIncomeSources.foreach { incomeSource =>

          s"should show paragraphText when ${incomeSource._2} isn't released even if model is present, and there are no other IncomeSources" when {
            lazy val result: Future[Result] = {
              authoriseAgentOrIndividual(user.isAgent)
              stubIncomeSources(incomeSource._1)
              route(unreleasedIncomeSources, request, user.isWelsh).get
            }

            implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

            "returns status of OK(200)" in {
              status(result) shouldBe OK
            }

            textOnPageCheck(viewEstimationHeading, viewEstimationHeadingSelector)
            textOnPageCheck(get.viewEstimationParagraph, viewEstimationParagraphSelector)
          }
        }
        }
      }
    }

}
