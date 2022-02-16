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
    val bullet1UkInterestPaidText = "UK interest"
    val bullet1UkInterestPaidTextWelsh = "llog y DU"
    val bullet2DividendsFromUKText = "dividends from UK-based companies, trusts and open-ended investment companies"
    val bullet2DividendsFromUKTextWelsh = "difidendau gan gwmnïau, ymddiriedolaethau a chwmnïau buddsoddi penagored yn y DU"
    val bullet3DonationsToCharityText = "donations to charity"
    val bullet3DonationsToCharityTextWelsh = "rhoddion i elusennau"
    val viewEmploymentInformationText = "You can view PAYE employment information, but you cannot update it until 6 April 2022."
    val viewEmploymentInformationTextWelsh = "Gallwch fwrw golwg dros wybodaeth am gyflogaeth TWE, ond ni allwch ei diweddaru tan 6 Ebrill 2022."
    val toUpdateIncomeText = "Use your software package to update anything not on the list."
    val toUpdateIncomeTextWelsh = "Defnyddiwch eich pecyn meddalwedd i ddiweddaru unrhyw beth nad yw’n ymddangos ar y rhestr."
    val continueButtonText = "Continue"
    val continueButtonTextWelsh = "Welsh"
    val continueButtonHref = s"/update-and-submit-income-tax-return/$taxYear/start"
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

  private val urlPath = s"/update-and-submit-income-tax-return/$taxYear/start"

  "Rendering the start page in English" should {

    val headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck")

    "render correctly when the user is an individual" should {
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
      textOnPageCheck(bullet1UkInterestPaidText, Selectors.bullet1)
      textOnPageCheck(bullet2DividendsFromUKText, Selectors.bullet2)
      textOnPageCheck(bullet3DonationsToCharityText, Selectors.bullet3)
      textOnPageCheck(viewEmploymentInformationText, Selectors.p3)
      textOnPageCheck(toUpdateIncomeText, Selectors.p4)
      formPostLinkCheck(continueButtonHref, Selectors.continueButton)
    }

    "render correctly when the user is an agent" should {
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
      textOnPageCheck(bullet1UkInterestPaidText, Selectors.bullet1)
      textOnPageCheck(bullet2DividendsFromUKText, Selectors.bullet2)
      textOnPageCheck(bullet3DonationsToCharityText, Selectors.bullet3)
      textOnPageCheck(viewEmploymentInformationText, Selectors.p3)
      textOnPageCheck(toUpdateIncomeText, Selectors.p4)
      formPostLinkCheck(continueButtonHref, Selectors.continueButton)
    }
  }

  "Rendering the start page in Welsh" should {
    val headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck", HeaderNames.ACCEPT_LANGUAGE -> "cy")

    "render correctly when the user is an individual" should {
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
      textOnPageCheck(bullet1UkInterestPaidTextWelsh, Selectors.bullet1)
      textOnPageCheck(bullet2DividendsFromUKTextWelsh, Selectors.bullet2)
      textOnPageCheck(bullet3DonationsToCharityTextWelsh, Selectors.bullet3)
      textOnPageCheck(viewEmploymentInformationTextWelsh, Selectors.p3)
      textOnPageCheck(toUpdateIncomeTextWelsh, Selectors.p4)
      formPostLinkCheck(continueButtonHref, Selectors.continueButton)
    }

    "render correctly when the user is an agent" should {
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
      textOnPageCheck(bullet1UkInterestPaidTextWelsh, Selectors.bullet1)
      textOnPageCheck(bullet2DividendsFromUKTextWelsh, Selectors.bullet2)
      textOnPageCheck(bullet3DonationsToCharityTextWelsh, Selectors.bullet3)
      textOnPageCheck(viewEmploymentInformationTextWelsh, Selectors.p3)
      textOnPageCheck(toUpdateIncomeTextWelsh, Selectors.p4)
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
          result.header.headers shouldBe Map("Location" -> "/update-and-submit-income-tax-return/iv-uplift")
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
