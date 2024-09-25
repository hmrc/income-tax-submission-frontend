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

package controllers

import common.SessionValues
import config.{AppConfig, ErrorHandler}
import controllers.predicates.AuthorisedAction
import itUtils.{IntegrationTest, OverviewPageHelpers, ViewHelpers}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.http.HeaderNames
import play.api.http.Status._
import play.api.mvc.Result
import play.api.test.Helpers.{OK, status, writeableOf_AnyContentAsEmpty}
import play.api.test.{FakeRequest, Helpers}
import services.{TaskListService, ValidTaxYearListService}
import uk.gov.hmrc.http.SessionKeys
import views.html.TaskListPageView

import scala.concurrent.Future

class TaskListPageControllerISpec extends IntegrationTest with ViewHelpers with OverviewPageHelpers {

  object ExpectedIndividualEN extends SpecificExpectedResults {
    val headingExpected = "Your Income Tax Return"
  }

  object ExpectedAgentEN extends SpecificExpectedResults {
    val headingExpected = "Your client’s Income Tax Return"
  }

  object ExpectedIndividualCY extends SpecificExpectedResults {
    val headingExpected = "Eich Ffurflen Dreth Incwm"
  }

  object ExpectedAgentCY extends SpecificExpectedResults {
    val headingExpected = "Ffurflen Dreth Incwm eich cleient"
  }

  trait SpecificExpectedResults {
    val headingExpected: String
  }

  trait CommonExpectedResults {
    def caption(taxYearMinusOne: Int, taxYear: Int): String
  }

  object CommonExpectedEN extends CommonExpectedResults {
    def caption(taxYearMinusOne: Int, taxYear: Int): String = s"6 April $taxYearMinusOne to 5 April $taxYear"
  }

  object CommonExpectedCY extends CommonExpectedResults {
    def caption(taxYearMinusOne: Int, taxYear: Int): String = s"6 Ebrill $taxYearMinusOne i 5 Ebrill $taxYear"
  }

  private val urlPathInYear = s"/update-and-submit-income-tax-return/$taxYear/tasklist"
  private val urlPathEndOfYear = s"/update-and-submit-income-tax-return/$taxYearEOY/tasklist"

  lazy val frontendAppConfig: AppConfig = app.injector.instanceOf[AppConfig]

  val controller: TaskListPageController = new TaskListPageController(
    inYearAction,
    app.injector.instanceOf[TaskListPageView],
    app.injector.instanceOf[TaskListService],
    app.injector.instanceOf[AuthorisedAction],
    app.injector.instanceOf[ValidTaxYearListService],
    app.injector.instanceOf[ErrorHandler]
  )(frontendAppConfig, mcc, scala.concurrent.ExecutionContext.Implicits.global)

  val userScenarios: Seq[UserScenario[CommonExpectedResults, SpecificExpectedResults]] = Seq(
    UserScenario(isWelsh = false, isAgent = false, CommonExpectedEN, Some(ExpectedIndividualEN)),
    UserScenario(isWelsh = false, isAgent = true, CommonExpectedEN, Some(ExpectedAgentEN)),
    UserScenario(isWelsh = true, isAgent = false, CommonExpectedCY, Some(ExpectedIndividualCY)),
    UserScenario(isWelsh = true, isAgent = true, CommonExpectedCY, Some(ExpectedAgentCY))
  )

  ".show for in year" when {

    userScenarios.foreach { user =>
      import user.commonExpectedResults._

      val specific = user.specificExpectedResults.get

      s"language is ${welshTest(user.isWelsh)} and request is from an ${agentTest(user.isAgent)}" should {
        val headers = Seq(
          HeaderNames.COOKIE -> playSessionCookies(taxYear, validTaxYearList),
          "Csrf-Token" -> "nocheck"
        )

        "render a tasklist page" when {
          val request = FakeRequest("GET", urlPathInYear).withHeaders(headers: _*)
          lazy val result: Future[Result] = {
            cleanDatabase(taxYear)
            authoriseAgentOrIndividual(user.isAgent)
            stubTaskListService
            route(app, request, user.isWelsh).get
          }

          implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

          "has a status of OK(200)" in {
            status(result) shouldBe OK
          }

          titleCheck(specific.headingExpected, user.isWelsh)
          h1Check(specific.headingExpected + " " + caption(taxYearEOY, taxYear))
          captionCheck(caption(taxYearEOY, taxYear))
        }

        "redirect to the tailor returns add sections page when no tasklist data is defined" when {

          val headers = Seq(
            HeaderNames.COOKIE -> playSessionCookies(taxYear, validTaxYearList),
            "Csrf-Token" -> "nocheck"
          )

          val request = FakeRequest("GET", urlPathInYear).withHeaders(headers: _*)
          lazy val result: Future[Result] = {
            cleanDatabase(taxYear)
            authoriseAgentOrIndividual(user.isAgent)
            route(app, request, user.isWelsh).get
          }

          "has a status of SEE_OTHER (303)" in {
            status(result) shouldBe SEE_OTHER
          }

          "has the tailor returns add section page redirect link" in {
            redirectUrl(result) shouldBe appConfig.tailorReturnAddSectionsPageUrl(taxYear)
          }
        }
      }
    }
  }

  ".show for end of year" when {

    userScenarios.foreach { user =>
      import user.commonExpectedResults._

      val specific = user.specificExpectedResults.get

      s"language is ${welshTest(user.isWelsh)} and request is from an ${agentTest(user.isAgent)}" should {
        val headers = Seq(
          HeaderNames.COOKIE -> playSessionCookies(taxYearEOY, validTaxYearList),
          "Csrf-Token" -> "nocheck"
        )

        "render a tasklist page" when {
          val request = FakeRequest("GET", urlPathEndOfYear).withHeaders(headers: _*)
          lazy val result: Future[Result] = {
            cleanDatabase(taxYearEOY)
            authoriseAgentOrIndividual(user.isAgent)
            stubTaskListServiceEndOfYear
            route(app, request, user.isWelsh).get
          }

          implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

          "has a status of OK(200)" in {
            status(result) shouldBe OK
          }

          titleCheck(specific.headingExpected, user.isWelsh)
          h1Check(specific.headingExpected + " " + caption(taxYearEndOfYearMinusOne, taxYearEOY))
          captionCheck(caption(taxYearEndOfYearMinusOne, taxYearEOY))
        }

        "redirect to the tailor returns add sections page when no tasklist data is defined" when {

          val headers = Seq(
            HeaderNames.COOKIE -> playSessionCookies(taxYear, validTaxYearList),
            "Csrf-Token" -> "nocheck"
          )

          val request = FakeRequest("GET", urlPathInYear).withHeaders(headers: _*)
          lazy val result: Future[Result] = {
            cleanDatabase(taxYear)
            authoriseAgentOrIndividual(user.isAgent)
            route(app, request, user.isWelsh).get
          }

          "has a status of SEE_OTHER (303)" in {
            status(result) shouldBe SEE_OTHER
          }

          "has the tailor returns add section page redirect link" in {
             redirectUrl(result) shouldBe appConfig.tailorReturnAddSectionsPageUrl(taxYear)
          }
        }
      }
    }
  }

  "Hitting the show endpoint" should {
    s"return an OK (200) for in year" when {
      "all auth requirements are met" in {
        val result = {
          authoriseIndividual()
          stubTaskListService
          await(controller.show(taxYear)(fakeRequest.withSession(
            SessionKeys.authToken -> "mock-bearer-token",
            SessionValues.TAX_YEAR -> s"$taxYear",
            SessionValues.VALID_TAX_YEARS -> validTaxYearList.mkString(","))
          ))
        }

        result.header.status shouldBe OK
      }
    }

    "return an OK(200) when end of year" when {
      "all auth requirements are met" in {
        val result = {
          authoriseIndividual()
          stubTaskListServiceEndOfYear
          await(controller.show(taxYearEOY)(fakeRequest.withSession(
            SessionKeys.authToken -> "mock-bearer-token",
            SessionValues.TAX_YEAR -> s"$taxYearEOY",
            SessionValues.VALID_TAX_YEARS -> validTaxYearList.mkString(","),
            SessionKeys.sessionId -> "sessionId-0101010101")
          ))
        }

        result.header.status shouldBe OK
      }
    }

    s"return an UNAUTHORISED (401)" when {
      "the confidence level is too low" in {
        val result = {
          unauthorisedIndividualInsufficientConfidenceLevel()
          await(controller.show(taxYear)(fakeRequest.withSession(SessionKeys.authToken -> "mock-bearer-token")))
        }

        result.header.status shouldBe SEE_OTHER
        result.header.headers shouldBe Map("Location" -> "/update-and-submit-income-tax-return/iv-uplift")
      }
    }

    "redirect to the sign in page" when {
      "it contains the wrong credentials" which {
        lazy val result = {
          unauthorisedIndividualWrongCredentials()
          await(controller.show(taxYear)(fakeRequest))
        }

        "has a status of SEE_OTHER (303)" in {
          result.header.status shouldBe SEE_OTHER
        }

        "has the sign in page redirect link" in {
          result.header.headers("Location") shouldBe appConfig.signInUrl
        }
      }
    }
  }
}
