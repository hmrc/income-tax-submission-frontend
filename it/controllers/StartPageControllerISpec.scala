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

import audit.AuditService
import common.SessionValues
import config.AppConfig
import controllers.predicates.AuthorisedAction
import itUtils.{IntegrationTest, ViewHelpers}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.http.HeaderNames
import play.api.mvc.Result
import play.api.test.Helpers.{OK, SEE_OTHER, status, writeableOf_AnyContentAsEmpty}
import play.api.test.{FakeRequest, Helpers}
import services.AuthService
import views.html.StartPageView

import scala.concurrent.Future

class StartPageControllerISpec extends IntegrationTest with ViewHelpers {

  lazy val frontendAppConfig: AppConfig = app.injector.instanceOf[AppConfig]

  def controller: StartPageController = new StartPageController(
    app.injector.instanceOf[AuthorisedAction],
    app.injector.instanceOf[AuthService],
    app.injector.instanceOf[StartPageView],
    app.injector.instanceOf[AuditService],
    frontendAppConfig,
    mcc,
    scala.concurrent.ExecutionContext.Implicits.global
  )

  object CommonExpectedResults {
    val taxYear = 2022
    val vcAgentBreadcrumbUrl = "http://localhost:9081/report-quarterly/income-and-expenses/view/agents/income-tax-account"
    val vcBreadcrumbUrl = "http://localhost:9081/report-quarterly/income-and-expenses/view"
    val vcBreadcrumb = "Income Tax"
    val vcBreadcrumbWelsh = "Treth Incwm"
    val startPageBreadcrumb = "Update and submit an Income Tax Return"
    val startPageBreadcrumbWelsh = "Diweddaru a chyflwyno Ffurflen Dreth Incwm"
    val pageTitleText = "Update and submit an Income Tax Return"
    val pageTitleTextWelsh = "Diweddaru a chyflwyno Ffurflen Dreth Incwm"
    val pageHeadingText = "Update and submit an Income Tax Return"
    val pageHeadingTextWelsh = "Diweddaru a chyflwyno Ffurflen Dreth Incwm"
    val caption = s"6 April ${taxYear - 1} to 5 April $taxYear"
    val captionWelsh = s"6 Ebrill ${taxYear - 1} i 5 Ebrill $taxYear"
    val useThisServiceText = "Use this service to update and submit an Income Tax Return."
    val useThisServiceTextWelsh = "Defnyddiwch y gwasanaeth hwn i ddiweddaru a chyflwyno Ffurflen Dreth Incwm."
    val newServiceText = "This is a new service. At the moment you can only update information about:"
    val newServiceTextWelsh = "Mae hwn yn wasanaeth newydd. Ar hyn o bryd, gallwch ond diweddaru gwybodaeth am y canlynol:"
    val bullet2DividendsFromUKText = "dividends from UK companies, trusts and open-ended investment companies"
    val bullet2DividendsFromUKTextWelsh = "difidendau gan gwmnïau, ymddiriedolaethau a chwmnïau buddsoddi penagored yn y DU"
    val continueButtonText = "Continue"
    val continueButtonTextWelsh = "Welsh"
    val continueButtonHref = s"/income-through-software/return/$taxYear/start"
  }

  object IndividualExpectedResultsEN {
    val bullet1InterestPaidIndividualText = "interest paid to you in the UK"
    val bullet3DonationsToCharityIndividual = "your donations to charity"
    val viewEmploymentInformationIndividual = "You can view your employment information but cannot use this service to update it until 6 April 2022."
    val toUpdateIncomeIndividualText = "To update your self-employment and property income, you must use your chosen commercial software."
  }

  object IndividualExpectedResultsCY {
    val bullet1InterestPaidIndividualText = "llog a dalwyd i chi yn y DU"
    val bullet3DonationsToCharityIndividual = "eich rhoddion chi i elusennau"
    val viewEmploymentInformationIndividual = "Gallwch weld eich gwybodaeth am gyflogaeth ond ni allwch ddefnyddio’r gwasanaeth hwn i’w diweddaru tan 6 Ebrill 2022."
    val toUpdateIncomeIndividualText = "I ddiweddaru’ch incwm o hunangyflogaeth a’ch incwm o eiddo, mae’n rhaid i chi ddefnyddio’ch meddalwedd fasnachol ddewisedig."
  }

  object AgentExpectedResultsEN {
    val bullet1InterestPaidAgentText = "interest paid to your client in the UK"
    val bullet3DonationsToCharityAgent = "your client’s donations to charity"
    val viewEmploymentInformationAgent = "You can view your client’s employment information but cannot use this service to update it until 6 April 2022."
    val toUpdateIncomeAgentText = "To update your client’s self-employment and property income, you must use your chosen commercial software."
  }

  object AgentExpectedResultsCY {
    val bullet1InterestPaidAgentText = "llog a dalwyd i’ch cleient yn y DU"
    val bullet3DonationsToCharityAgent = "rhoddion eich cleient i elusennau"
    val viewEmploymentInformationAgent = "Gallwch weld gwybodaeth am gyflogaeth eich cleient ond ni allwch ddefnyddio’r gwasanaeth hwn i’w diweddaru tan 6 Ebrill 2022."
    val toUpdateIncomeAgentText = "I ddiweddaru incwm o hunangyflogaeth ac incwm o eiddo eich cleient, mae’n rhaid i chi ddefnyddio’ch meddalwedd fasnachol ddewisedig."
  }


  object Selectors {
    val vcBreadcrumbSelector = "body > div > div.govuk-breadcrumbs > ol > li:nth-child(1) > a"
    val startPageBreadcrumbSelector = "body > div > div.govuk-breadcrumbs > ol > li:nth-child(2)"
    val pageHeading = "#main-content > div > div > header > h1"
    val caption = "#main-content > div > div > header > p"
    val p1 = "#main-content > div > div > div:nth-child(2) > p:nth-child(1)"
    val p2 = "#main-content > div > div > div:nth-child(2) > p:nth-child(2)"
    val bullet1 = "#main-content > div > div > ul > li:nth-child(1)"
    val bullet2 = "#main-content > div > div > ul > li:nth-child(2)"
    val bullet3 = "#main-content > div > div > ul > li:nth-child(3)"
    val p3 = "#main-content > div > div > div:nth-child(4) > p:nth-child(1)"
    val p4 =  "#main-content > div > div > div:nth-child(4) > p:nth-child(2)"
    val continueButton = "#main-content > div > div > form"
  }

  import CommonExpectedResults._

  private val urlPath = s"/income-through-software/return/$taxYear/start"

  "Rendering the start page in English" should {

    val headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck")

    "render correctly when the user is an individual" should {
      import IndividualExpectedResultsEN._
      val request = FakeRequest("GET", urlPath).withHeaders(headers: _*)

      lazy val result: Future[Result] = {
        authoriseIndividual()
        route(app, request).get
      }

      implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

      "returns status of OK(200)" in {
        status(result) shouldBe OK
      }

      welshToggleCheck("English")
      linkCheck(vcBreadcrumb, Selectors.vcBreadcrumbSelector, vcBreadcrumbUrl)
      textOnPageCheck(startPageBreadcrumb, Selectors.startPageBreadcrumbSelector)
      titleCheck(pageTitleText, isWelsh = false)
      h1Check(pageHeadingText, "xl")
      textOnPageCheck(caption, Selectors.caption)
      textOnPageCheck(useThisServiceText, Selectors.p1)
      textOnPageCheck(newServiceText, Selectors.p2)
      textOnPageCheck(bullet1InterestPaidIndividualText, Selectors.bullet1)
      textOnPageCheck(bullet2DividendsFromUKText, Selectors.bullet2)
      textOnPageCheck(bullet3DonationsToCharityIndividual, Selectors.bullet3)
      textOnPageCheck(viewEmploymentInformationIndividual, Selectors.p3)
      textOnPageCheck(toUpdateIncomeIndividualText, Selectors.p4)
      formPostLinkCheck(continueButtonHref, Selectors.continueButton)
    }

    "render correctly when the user is an agent" should {
      import AgentExpectedResultsEN._
      val request = FakeRequest("GET", urlPath).withHeaders(headers: _*)

      lazy val result: Future[Result] = {
        authoriseAgent()
        route(app, request).get
      }

      implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

      "returns status of OK(200)" in {
        status(result) shouldBe OK
      }

      welshToggleCheck("English")
      linkCheck(vcBreadcrumb, Selectors.vcBreadcrumbSelector, vcAgentBreadcrumbUrl)
      textOnPageCheck(startPageBreadcrumb, Selectors.startPageBreadcrumbSelector)

      titleCheck(pageTitleText, isWelsh = false)
      h1Check(pageHeadingText, "xl")
      textOnPageCheck(caption, Selectors.caption)
      textOnPageCheck(useThisServiceText, Selectors.p1)
      textOnPageCheck(newServiceText, Selectors.p2)
      textOnPageCheck(bullet1InterestPaidAgentText, Selectors.bullet1)
      textOnPageCheck(bullet2DividendsFromUKText, Selectors.bullet2)
      textOnPageCheck(bullet3DonationsToCharityAgent, Selectors.bullet3)
      textOnPageCheck(viewEmploymentInformationAgent, Selectors.p3)
      textOnPageCheck(toUpdateIncomeAgentText, Selectors.p4)
      formPostLinkCheck(continueButtonHref, Selectors.continueButton)
    }
  }

  "Rendering the start page in Welsh" should {
    val headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck", HeaderNames.ACCEPT_LANGUAGE -> "cy")

    "render correctly when the user is an individual" should {
      import IndividualExpectedResultsCY._
      val request = FakeRequest("GET", urlPath).withHeaders(headers: _*)

      lazy val result: Future[Result] = {
        authoriseIndividual()
        route(app, request).get
      }

      implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

      "returns status of OK(200)" in {
        status(result) shouldBe OK
      }

      welshToggleCheck("Welsh")
      linkCheck(vcBreadcrumbWelsh, Selectors.vcBreadcrumbSelector, vcBreadcrumbUrl)
      textOnPageCheck(startPageBreadcrumbWelsh, Selectors.startPageBreadcrumbSelector)
      titleCheck(pageTitleTextWelsh, isWelsh = true)
      h1Check(pageHeadingTextWelsh, "xl")
      textOnPageCheck(captionWelsh, Selectors.caption)
      textOnPageCheck(useThisServiceTextWelsh, Selectors.p1)
      textOnPageCheck(newServiceTextWelsh, Selectors.p2)
      textOnPageCheck(bullet1InterestPaidIndividualText, Selectors.bullet1)
      textOnPageCheck(bullet2DividendsFromUKTextWelsh, Selectors.bullet2)
      textOnPageCheck(bullet3DonationsToCharityIndividual, Selectors.bullet3)
      textOnPageCheck(viewEmploymentInformationIndividual, Selectors.p3)
      textOnPageCheck(toUpdateIncomeIndividualText, Selectors.p4)
      formPostLinkCheck(continueButtonHref, Selectors.continueButton)
    }

    "render correctly when the user is an agent" should {
      import AgentExpectedResultsCY._
      val request = FakeRequest("GET", urlPath).withHeaders(headers: _*)

      lazy val result: Future[Result] = {
        authoriseAgent()
        route(app, request).get
      }

      implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

      "returns status of OK(200)" in {
        status(result) shouldBe OK
      }

      welshToggleCheck("Welsh")
      linkCheck(vcBreadcrumbWelsh, Selectors.vcBreadcrumbSelector, vcAgentBreadcrumbUrl)
      textOnPageCheck(startPageBreadcrumbWelsh, Selectors.startPageBreadcrumbSelector)

      titleCheck(pageTitleTextWelsh, isWelsh = true)
      h1Check(pageHeadingTextWelsh, "xl")
      textOnPageCheck(captionWelsh, Selectors.caption)
      textOnPageCheck(useThisServiceTextWelsh, Selectors.p1)
      textOnPageCheck(newServiceTextWelsh, Selectors.p2)
      textOnPageCheck(bullet1InterestPaidAgentText, Selectors.bullet1)
      textOnPageCheck(bullet2DividendsFromUKTextWelsh, Selectors.bullet2)
      textOnPageCheck(bullet3DonationsToCharityAgent, Selectors.bullet3)
      textOnPageCheck(viewEmploymentInformationAgent, Selectors.p3)
      textOnPageCheck(toUpdateIncomeAgentText, Selectors.p4)
      formPostLinkCheck(continueButtonHref, Selectors.continueButton)
    }
  }

  "Hitting the show endpoint" should {

    s"return an OK (200)" when {

      "all auth requirements are met" in {
        val result = {
          authoriseIndividual()
          await(controller.show(taxYear)(fakeRequest))
        }

        result.header.status shouldBe OK
      }

    }

    s"redirect to the iv journey" when {

      "the confidence level is too low" which {
        lazy val result = {
          unauthorisedIndividualInsufficientConfidenceLevel()
          await(controller.show(taxYear)(fakeRequest))
        }

        "has a status of SEE_OTHER (303)" in {
          result.header.status shouldBe SEE_OTHER
        }

        "has the iv journey url as the redirect link" in {
          result.header.headers shouldBe Map("Location" -> "/income-through-software/return/iv-uplift")
        }
      }

    }

    "redirect to the sign in link" when {

      "it contains the wrong credentials" which {
        lazy val result = {
          unauthorisedIndividualWrongCredentials()
          await(controller.show(taxYear)(fakeRequest))
        }

        "has a status of SEE_OTHER (303)" in {
          result.header.status shouldBe SEE_OTHER
        }

        "has the sign in url as the redirect link" in {
          result.header.headers("Location") shouldBe appConfig.signInUrl
        }
      }

    }

  }

  "Hitting the submit endpoint" should {

    "redirect to the overview page" when {

      "the user is an individual" which {
        lazy val result: Future[Result] = {
          wireMockServer.resetAll()
          authoriseIndividual()
          controller.submit(taxYear)(fakeRequest.withSession(SessionValues.TAX_YEAR -> "2022"))
        }

        "has a result of SEE_OTHER(303)" in {
          status(result) shouldBe SEE_OTHER
        }

        "has overview page as the redirect url" in {
          redirectUrl(result) shouldBe controllers.routes.OverviewPageController.show(taxYear).url
        }
      }

    }

  }
  
}
