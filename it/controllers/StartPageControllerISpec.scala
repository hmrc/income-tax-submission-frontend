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

package controllers

import audit.AuditService
import common.SessionValues
import config.{AppConfig, ErrorHandler}
import controllers.predicates.{AuthorisedAction, InYearAction}
import itUtils.{IntegrationTest, ViewHelpers}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.http.HeaderNames
import play.api.mvc.Result
import play.api.test.Helpers.{OK, SEE_OTHER, status, writeableOf_AnyContentAsEmpty}
import play.api.test.{FakeRequest, Helpers}
import services.{AuthService, ValidTaxYearListService}
import uk.gov.hmrc.http.SessionKeys
import views.html.StartPageView

import scala.concurrent.Future

class StartPageControllerISpec extends IntegrationTest with ViewHelpers {

  private lazy val frontendAppConfig: AppConfig = app.injector.instanceOf[AppConfig]

  private def controller: StartPageController = new StartPageController(
    app.injector.instanceOf[AuthorisedAction],
    app.injector.instanceOf[AuthService],
    app.injector.instanceOf[StartPageView],
    app.injector.instanceOf[AuditService],
    app.injector.instanceOf[InYearAction],
    frontendAppConfig,
    mcc,
    scala.concurrent.ExecutionContext.Implicits.global,
    app.injector.instanceOf[ValidTaxYearListService],
    app.injector.instanceOf[ErrorHandler]
  )

  private def controllerWithoutSL: StartPageController = new StartPageController(
    app.injector.instanceOf[AuthorisedAction],
    app.injector.instanceOf[AuthService],
    app.injector.instanceOf[StartPageView],
    app.injector.instanceOf[AuditService],
    app.injector.instanceOf[InYearAction],
    customApp(studentLoansEnabled = false).injector.instanceOf[AppConfig],
    mcc,
    scala.concurrent.ExecutionContext.Implicits.global,
    app.injector.instanceOf[ValidTaxYearListService],
    app.injector.instanceOf[ErrorHandler]
  )

  object CommonExpectedResults {
    val vcAgentBreadcrumbUrl = "http://localhost:9081/report-quarterly/income-and-expenses/view/agents"
    val vcBreadcrumbUrl = s"http://localhost:9081/report-quarterly/income-and-expenses/view$vcPtaNavBarOrigin"
    val vcBreadcrumb = "Income Tax Account"
    val vcBreadcrumbWelsh = "Cyfrif Treth Incwm"
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
    val inYearText = s"You can view employment information but cannot use this service to update it until 6 April $taxYear."
    val inYearWelshText = s"Gallwch fwrw golwg dros wybodaeth am gyflogaeth ond ni allwch ddefnyddio’r gwasanaeth hwn i’w diweddaru tan 6 Ebrill $taxYear."
    val newServiceTextWelsh = "Mae hwn yn wasanaeth newydd. Ar hyn o bryd, gallwch ond diweddaru gwybodaeth am y canlynol:"
    val interestBullet = "UK interest"
    val interestBulletWelsh = "llog y DU"
    val dividendsBullet = "dividends from UK-based companies"
    val dividendsBulletWelsh = "difidendau o gwmnïau sydd wedi’u lleoli yn y DU"
    val employmentBullet = "PAYE employment"
    val employmentBulletWelsh = "cyflogaeth TWE"
    val employmentSLBullet = "PAYE employment (including student loans)"
    val employmentSLBulletWelsh = "cyflogaeth TWE (gan gynnwys Benthyciadau Myfyrwyr)"
    val charityBullet = "donations to charity"
    val charityBulletWelsh = "cyfraniadau at elusennau"
    val softwarePackageText = s"To update your self-employment and property income, you must use your software package."
    val softwarePackageAgentText = s"To update your client’s self-employment and property income, you must use your software package."
    val softwarePackageWelshText = s"I ddiweddaru’ch incwm o hunangyflogaeth a’ch incwm o eiddo, mae’n rhaid i chi ddefnyddio’ch pecyn meddalwedd."
    val softwarePackageWelshAgentText = s"I ddiweddaru incwm o hunangyflogaeth ac incwm o eiddo ar gyfer eich cleient," +
      s" mae’n rhaid i chi ddefnyddio’ch pecyn meddalwedd."
    val onlyUpdateText = "You can only update information about:"
    val onlyUpdateTextWelsh = "Gallwch ddiweddaru gwybodaeth am y canlynol yn unig:"
    val continueButtonText = "Continue"
    val continueButtonTextWelsh = "Welsh"
    val continueButtonHref = s"/update-and-submit-income-tax-return/$taxYear/start"
  }


  object Selectors {
    val vcBreadcrumbSelector = "body > div > div.govuk-breadcrumbs > ol > li:nth-child(1) > a"
    val startPageBreadcrumbSelector = "body > div > div.govuk-breadcrumbs > ol > li:nth-child(2)"
    val p1 = "#main-content > div > div > div:nth-child(2) > p:nth-child(1)"
    val p2 = "#main-content > div > div > div:nth-child(2) > p:nth-child(2)"
    val p3 = "#main-content > div > div > div:nth-child(2) > p:nth-child(3)"
    val p4 = "#main-content > div > div > div:nth-child(2) > p:nth-child(4)"
    val bullet1 = "#main-content > div > div > ul > li:nth-child(1)"
    val bullet2 = "#main-content > div > div > ul > li:nth-child(2)"
    val bullet3 = "#main-content > div > div > ul > li:nth-child(3)"
    val bullet4 = "#main-content > div > div > ul > li:nth-child(4)"
    val continueButton = "#main-content > div > div > form"
  }

  import CommonExpectedResults._

  private val urlPath = s"/update-and-submit-income-tax-return/$taxYear/start"

  "Rendering the start page in English" should {

    val headers = Seq(
      HeaderNames.COOKIE -> playSessionCookies(taxYear, validTaxYearList),
      "Csrf-Token" -> "nocheck"
    )

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
      h1Check(pageHeadingText + " " + caption)
      captionCheck(caption)
      textOnPageCheck(useThisServiceText, Selectors.p1)
      textOnPageCheck(inYearText, Selectors.p2)
      textOnPageCheck(dividendsBullet, Selectors.bullet1)
      textOnPageCheck(charityBullet, Selectors.bullet2)
      textOnPageCheck(employmentSLBullet, Selectors.bullet3)
      textOnPageCheck(interestBullet, Selectors.bullet4)
      textOnPageCheck(softwarePackageText, Selectors.p3)
      textOnPageCheck(onlyUpdateText, Selectors.p4)
      formPostLinkCheck(continueButtonHref, Selectors.continueButton)
    }

    "render correctly when the user is an individual and student loans is off" should {

      lazy val result: Future[Result] = {
        authoriseIndividual()
        controllerWithoutSL.show(taxYear)(fakeRequest.withSession(
          SessionKeys.authToken -> "mock-bearer-token",
          SessionValues.TAX_YEAR -> taxYear.toString,
          SessionValues.VALID_TAX_YEARS -> validTaxYearList.mkString(",")
        ))
      }

      implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

      "returns status of OK(200)" in {
        status(result) shouldBe OK
      }

      welshToggleCheck("English")
      textOnPageCheck(employmentBullet, Selectors.bullet3)
    }

    "render correctly when the user is an individual and student loans is off and is in welsh" should {
      val headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear, validTaxYearList), "Csrf-Token" -> "nocheck", HeaderNames.ACCEPT_LANGUAGE -> "cy")

      lazy val result: Future[Result] = {
        authoriseIndividual()
        controllerWithoutSL.show(taxYear)(fakeRequest.withHeaders(headers: _*).withSession(
          SessionKeys.authToken -> "mock-bearer-token",
          SessionValues.TAX_YEAR -> taxYear.toString,
          SessionValues.VALID_TAX_YEARS -> validTaxYearList.mkString(",")
        ))
      }

      implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

      "returns status of OK(200)" in {
        status(result) shouldBe OK
      }

      welshToggleCheck("Welsh")
      textOnPageCheck(employmentBulletWelsh, Selectors.bullet3)
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
      h1Check(pageHeadingText + " " + caption)
      captionCheck(caption)
      textOnPageCheck(useThisServiceText, Selectors.p1)
      textOnPageCheck(inYearText, Selectors.p2)
      textOnPageCheck(dividendsBullet, Selectors.bullet1)
      textOnPageCheck(charityBullet, Selectors.bullet2)
      textOnPageCheck(employmentSLBullet, Selectors.bullet3)
      textOnPageCheck(interestBullet, Selectors.bullet4)
      textOnPageCheck(softwarePackageAgentText, Selectors.p3)
      textOnPageCheck(onlyUpdateText, Selectors.p4)
      formPostLinkCheck(continueButtonHref, Selectors.continueButton)
    }
  }

  "Rendering the start page in Welsh" should {
    val headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear, validTaxYearList), "Csrf-Token" -> "nocheck", HeaderNames.ACCEPT_LANGUAGE -> "cy")

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
      h1Check(pageHeadingTextWelsh + " " + captionWelsh)
      captionCheck(captionWelsh)
      textOnPageCheck(useThisServiceTextWelsh, Selectors.p1)
      textOnPageCheck(inYearWelshText, Selectors.p2)
      textOnPageCheck(dividendsBulletWelsh, Selectors.bullet1)
      textOnPageCheck(charityBulletWelsh, Selectors.bullet2)
      textOnPageCheck(employmentSLBulletWelsh, Selectors.bullet3)
      textOnPageCheck(interestBulletWelsh, Selectors.bullet4)
      textOnPageCheck(softwarePackageWelshText, Selectors.p3)
      textOnPageCheck(onlyUpdateTextWelsh, Selectors.p4)
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
      h1Check(pageHeadingTextWelsh + " " + captionWelsh)
      captionCheck(captionWelsh)
      textOnPageCheck(useThisServiceTextWelsh, Selectors.p1)
      textOnPageCheck(inYearWelshText, Selectors.p2)
      textOnPageCheck(dividendsBulletWelsh, Selectors.bullet1)
      textOnPageCheck(charityBulletWelsh, Selectors.bullet2)
      textOnPageCheck(employmentSLBulletWelsh, Selectors.bullet3)
      textOnPageCheck(interestBulletWelsh, Selectors.bullet4)
      textOnPageCheck(softwarePackageWelshAgentText, Selectors.p3)
      textOnPageCheck(onlyUpdateTextWelsh, Selectors.p4)
      formPostLinkCheck(continueButtonHref, Selectors.continueButton)
    }
  }

  "Hitting the show endpoint" should {

    s"return an OK (200)" when {

      "all auth requirements are met" in {
        val result = {
          authoriseIndividual()
          await(controller.show(taxYear)(fakeRequest.withSession(SessionValues.TAX_YEAR -> s"$taxYear",
            SessionKeys.authToken -> "mock-bearer-token",
            SessionValues.VALID_TAX_YEARS -> validTaxYearList.mkString(","))
          ))
        }

        result.header.status shouldBe OK
      }

    }

    s"redirect to the iv journey" when {

      "the confidence level is too low" which {
        lazy val result = {
          unauthorisedIndividualInsufficientConfidenceLevel()
          await(controller.show(taxYear)(fakeRequest.withSession(SessionValues.TAX_YEAR -> s"$taxYear",
            SessionKeys.authToken -> "mock-bearer-token",
            SessionValues.VALID_TAX_YEARS -> validTaxYearList.mkString(","))
          ))
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
          await(controller.show(taxYear)(fakeRequest.withSession(SessionValues.TAX_YEAR -> s"$taxYear",
            SessionValues.VALID_TAX_YEARS -> validTaxYearList.mkString(","))
          ))
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
          controller.submit(taxYear)(fakeRequest.withSession(
            SessionKeys.authToken -> "mock-bearer-token",
            SessionValues.TAX_YEAR -> taxYear.toString,
            SessionValues.VALID_TAX_YEARS -> validTaxYearList.mkString(",")
          ))
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
