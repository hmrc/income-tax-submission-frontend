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

import models.CisBuilder.aCis
import models.DividendsBuilder.aDividends
import models.EmploymentBuilder.aEmployment
import models.GiftAidBuilder.aGiftAid
import models.IncomeSourcesModel
import models.InterestBuilder.aInterest
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.http.HeaderNames
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.mvc.Result
import play.api.test.{FakeRequest, Helpers}
import play.api.test.Helpers.{status, writeableOf_AnyContentAsEmpty}

import scala.concurrent.Future

class OverviewPageControllerTailoringISpec extends OverviewPageControllerISpec {

  ".show for in year" when {
    import Links._
    import Selectors._

    userScenarios.foreach { user =>
      import user.commonExpectedResults._

      val specific = user.specificExpectedResults.get

      s"language is ${welshTest(user.isWelsh)} and request is from an ${agentTest(user.isAgent)}" should {
        val headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck")

        "render overview page with correct status tags when there is full prior data with no tailoring data" should {
          "have a notificationBanner with plural text" when {
            val incomeSources = incomeSourcesModel

            val request = FakeRequest("GET", urlPathInYear).withHeaders(headers: _*)

            lazy val result: Future[Result] = {
              dropTailoringDB()
              authoriseAgentOrIndividual(user.isAgent)
              stubIncomeSources(incomeSources)
              route(app, request, user.isWelsh).get
            }

            implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

            "returns status of OK(200)" in {
              status(result) shouldBe OK
            }

            welshToggleCheck(welshTest(user.isWelsh))
            linkCheck(vcBreadcrumb, vcBreadcrumbSelector, Links.viewAndChangeLink(user.isAgent))
            linkCheck(startPageBreadcrumb, startPageBreadcrumbSelector, startPageBreadcrumbUrl(taxYear))
            textOnPageCheck(overviewBreadcrumb, overviewBreadcrumbSelector)

            titleCheck(specific.headingExpected, user.isWelsh)
            h1Check(specific.headingExpected, "xl")
            textOnPageCheck(caption(taxYearEOY, taxYear), captionSelector)
            textOnPageCheck(specific.notificationBannerPlural, notificationBannerSelector)
            textOnPageCheck(specific.updateIncomeTaxReturnText, updateYourIncomeTaxReturnSubheadingSelector)
            textOnPageCheck(checkSectionsText, checkSectionsSelector)

            "has a dividends section" which {
              linkCheck(dividendsLinkText, dividendsLinkSelector, dividendsLinkWithPriorData(taxYear))
              textOnPageCheck(updatedText, dividendsStatusSelector)
            }

            "has an interest section" which {
              linkCheck(interestsLinkText, interestLinkSelector, interestsLinkWithPriorData(taxYear))
              textOnPageCheck(updatedText, interestStatusSelector)
            }

            "has a donations to charity section" which {
              linkCheck(giftAidLinkText, giftAidLinkSelector, appConfig.personalIncomeTaxGiftAidSubmissionCYAUrl(taxYear))
              textOnPageCheck(updatedText, giftAidStatusSelector)
            }

            "has an employment section" which {
              linkCheck(employmentLinkText, employmentLinkSelector, employmentLink(taxYear))
              textOnPageCheck(updatedText, employmentStatusSelector)
            }

            "has a cis section" which {
              linkCheck(cisLinkText, cisLinkSelector, cisLink(taxYear))
              textOnPageCheck(updatedText, cisStatusSelector)
            }

            "have a add sections link " which {
              linkCheck(specific.addSections, addSectionsSelector, "")
            }

            formPostLinkCheck(controllers.routes.OverviewPageController.inYearEstimate(taxYear).url, formSelector)
            buttonCheck(updateTaxCalculation, updateTaxCalculationSelector, None)

          }
        }
        "render overview page with correct status tags when there prior data (just dividends) with no tailoring data" should {
          "have a notificationBanner with singular text" when {
            val incomeSources = IncomeSourcesModel(Some(aDividends))

            val request = FakeRequest("GET", urlPathInYear).withHeaders(headers: _*)

            lazy val result: Future[Result] = {
              dropTailoringDB()
              authoriseAgentOrIndividual(user.isAgent)
              stubIncomeSources(incomeSources)
              route(app, request, user.isWelsh).get
            }

            implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

            "returns status of OK(200)" in {
              status(result) shouldBe OK
            }

            welshToggleCheck(welshTest(user.isWelsh))
            linkCheck(vcBreadcrumb, vcBreadcrumbSelector, Links.viewAndChangeLink(user.isAgent))
            linkCheck(startPageBreadcrumb, startPageBreadcrumbSelector, startPageBreadcrumbUrl(taxYear))
            textOnPageCheck(overviewBreadcrumb, overviewBreadcrumbSelector)

            titleCheck(specific.headingExpected, user.isWelsh)
            h1Check(specific.headingExpected, "xl")
            textOnPageCheck(caption(taxYearEOY, taxYear), captionSelector)
            textOnPageCheck(specific.notificationBanner, notificationBannerSelector)
            textOnPageCheck(specific.updateIncomeTaxReturnText, updateYourIncomeTaxReturnSubheadingSelector)
            textOnPageCheck(checkSectionsText, checkSectionsSelector)

            "has a dividends section" which {
              linkCheck(dividendsLinkText, dividendsLinkSelector, dividendsLinkWithPriorData(taxYear))
              textOnPageCheck(updatedText, statusTagSelector(1))
            }

            "does not have any other sections with status tags" which {
              textOnPageCheck("", statusTagSelector(2))
            }

            "have a add sections link " which {
              linkCheck(specific.addSections, addSectionsSelector, "")
            }

            formPostLinkCheck(controllers.routes.OverviewPageController.inYearEstimate(taxYear).url, formSelector)
            buttonCheck(updateTaxCalculation, updateTaxCalculationSelector, None)

          }
        }
        "render overview page with correct status tags when there prior data (just Interests) with no tailoring data" should {
          "have a notificationBanner with singular text" when {
            val incomeSources = IncomeSourcesModel(interest = Some(Seq(aInterest)))

            val request = FakeRequest("GET", urlPathInYear).withHeaders(headers: _*)

            lazy val result: Future[Result] = {
              dropTailoringDB()
              authoriseAgentOrIndividual(user.isAgent)
              stubIncomeSources(incomeSources)
              route(app, request, user.isWelsh).get
            }

            implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

            "returns status of OK(200)" in {
              status(result) shouldBe OK
            }

            welshToggleCheck(welshTest(user.isWelsh))
            linkCheck(vcBreadcrumb, vcBreadcrumbSelector, Links.viewAndChangeLink(user.isAgent))
            linkCheck(startPageBreadcrumb, startPageBreadcrumbSelector, startPageBreadcrumbUrl(taxYear))
            textOnPageCheck(overviewBreadcrumb, overviewBreadcrumbSelector)

            titleCheck(specific.headingExpected, user.isWelsh)
            h1Check(specific.headingExpected, "xl")
            textOnPageCheck(caption(taxYearEOY, taxYear), captionSelector)
            textOnPageCheck(specific.notificationBanner, notificationBannerSelector)
            textOnPageCheck(specific.updateIncomeTaxReturnText, updateYourIncomeTaxReturnSubheadingSelector)
            textOnPageCheck(checkSectionsText, checkSectionsSelector)

            "has a interest section" which {
              linkCheck(interestsLinkText, interestLinkSelector, interestsLinkWithPriorData(taxYear))
              textOnPageCheck(updatedText, statusTagSelector(1))
            }

            "does not have any other sections with status tags" which {
              textOnPageCheck("", statusTagSelector(2))
            }

            "have a add sections link " which {
              linkCheck(specific.addSections, addSectionsSelector, "")
            }

            formPostLinkCheck(controllers.routes.OverviewPageController.inYearEstimate(taxYear).url, formSelector)
            buttonCheck(updateTaxCalculation, updateTaxCalculationSelector, None)

          }
        }
        "render overview page with correct status tags when there prior data (just gift-aid) with no tailoring data" should {
          "have a notificationBanner with singular text" when {
            val incomeSources = IncomeSourcesModel(giftAid = Some(aGiftAid))

            val request = FakeRequest("GET", urlPathInYear).withHeaders(headers: _*)

            lazy val result: Future[Result] = {
              dropTailoringDB()
              authoriseAgentOrIndividual(user.isAgent)
              stubIncomeSources(incomeSources)
              route(app, request, user.isWelsh).get
            }

            implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

            "returns status of OK(200)" in {
              status(result) shouldBe OK
            }

            welshToggleCheck(welshTest(user.isWelsh))
            linkCheck(vcBreadcrumb, vcBreadcrumbSelector, Links.viewAndChangeLink(user.isAgent))
            linkCheck(startPageBreadcrumb, startPageBreadcrumbSelector, startPageBreadcrumbUrl(taxYear))
            textOnPageCheck(overviewBreadcrumb, overviewBreadcrumbSelector)

            titleCheck(specific.headingExpected, user.isWelsh)
            h1Check(specific.headingExpected, "xl")
            textOnPageCheck(caption(taxYearEOY, taxYear), captionSelector)
            textOnPageCheck(specific.notificationBanner, notificationBannerSelector)
            textOnPageCheck(specific.updateIncomeTaxReturnText, updateYourIncomeTaxReturnSubheadingSelector)
            textOnPageCheck(checkSectionsText, checkSectionsSelector)

            "has a gift-aid section" which {
              linkCheck(giftAidLinkText, giftAidLinkSelector, appConfig.personalIncomeTaxGiftAidSubmissionCYAUrl(taxYear))
              textOnPageCheck(updatedText, statusTagSelector(1))
            }

            "does not have any other sections with status tags" which {
              textOnPageCheck("", statusTagSelector(2))
            }

            "have a add sections link " which {
              linkCheck(specific.addSections, addSectionsSelector, "")
            }

            formPostLinkCheck(controllers.routes.OverviewPageController.inYearEstimate(taxYear).url, formSelector)
            buttonCheck(updateTaxCalculation, updateTaxCalculationSelector, None)

          }
        }
        "render overview page with correct status tags when there prior data (just employment) with no tailoring data" should {
          "have a notificationBanner with singular text" when {
            val incomeSources = IncomeSourcesModel(employment = Some(aEmployment))

            val request = FakeRequest("GET", urlPathInYear).withHeaders(headers: _*)

            lazy val result: Future[Result] = {
              dropTailoringDB()
              authoriseAgentOrIndividual(user.isAgent)
              stubIncomeSources(incomeSources)
              route(app, request, user.isWelsh).get
            }

            implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

            "returns status of OK(200)" in {
              status(result) shouldBe OK
            }

            welshToggleCheck(welshTest(user.isWelsh))
            linkCheck(vcBreadcrumb, vcBreadcrumbSelector, Links.viewAndChangeLink(user.isAgent))
            linkCheck(startPageBreadcrumb, startPageBreadcrumbSelector, startPageBreadcrumbUrl(taxYear))
            textOnPageCheck(overviewBreadcrumb, overviewBreadcrumbSelector)

            titleCheck(specific.headingExpected, user.isWelsh)
            h1Check(specific.headingExpected, "xl")
            textOnPageCheck(caption(taxYearEOY, taxYear), captionSelector)
            textOnPageCheck(specific.notificationBanner, notificationBannerSelector)
            textOnPageCheck(specific.updateIncomeTaxReturnText, updateYourIncomeTaxReturnSubheadingSelector)
            textOnPageCheck(checkSectionsText, checkSectionsSelector)

            "has a employment section" which {
              linkCheck(employmentLinkText, employmentLinkSelector, employmentLink(taxYear))
              textOnPageCheck(updatedText, statusTagSelector(1))
            }

            "does not have any other sections with status tags" which {
              textOnPageCheck("", statusTagSelector(2))
            }

            "have a add sections link " which {
              linkCheck(specific.addSections, addSectionsSelector, "")
            }

            formPostLinkCheck(controllers.routes.OverviewPageController.inYearEstimate(taxYear).url, formSelector)
            buttonCheck(updateTaxCalculation, updateTaxCalculationSelector, None)

          }
        }
        "render overview page with correct status tags when there prior data (just cis) with no tailoring data" should {
          "have a notificationBanner with singular text" when {
            val incomeSources = IncomeSourcesModel(cis = Some(aCis))

            val request = FakeRequest("GET", urlPathInYear).withHeaders(headers: _*)

            lazy val result: Future[Result] = {
              dropTailoringDB()
              authoriseAgentOrIndividual(user.isAgent)
              stubIncomeSources(incomeSources)
              route(app, request, user.isWelsh).get
            }

            implicit def document: () => Document = () => Jsoup.parse(Helpers.contentAsString(result))

            "returns status of OK(200)" in {
              status(result) shouldBe OK
            }

            welshToggleCheck(welshTest(user.isWelsh))
            linkCheck(vcBreadcrumb, vcBreadcrumbSelector, Links.viewAndChangeLink(user.isAgent))
            linkCheck(startPageBreadcrumb, startPageBreadcrumbSelector, startPageBreadcrumbUrl(taxYear))
            textOnPageCheck(overviewBreadcrumb, overviewBreadcrumbSelector)

            titleCheck(specific.headingExpected, user.isWelsh)
            h1Check(specific.headingExpected, "xl")
            textOnPageCheck(caption(taxYearEOY, taxYear), captionSelector)
            textOnPageCheck(specific.notificationBanner, notificationBannerSelector)
            textOnPageCheck(specific.updateIncomeTaxReturnText, updateYourIncomeTaxReturnSubheadingSelector)
            textOnPageCheck(checkSectionsText, checkSectionsSelector)

            "has a cis section" which {
              linkCheck(cisLinkText, cisLinkSelector, cisLink(taxYear))
              textOnPageCheck(updatedText, statusTagSelector(1))
            }

            "does not have any other sections with status tags" which {
              textOnPageCheck("", statusTagSelector(2))
            }

            "have a add sections link " which {
              linkCheck(specific.addSections, addSectionsSelector, "")
            }

            formPostLinkCheck(controllers.routes.OverviewPageController.inYearEstimate(taxYear).url, formSelector)
            buttonCheck(updateTaxCalculation, updateTaxCalculationSelector, None)

          }
        }
        "mongo fails" should {
          "return an internal server error" when {
            val incomeSources = IncomeSourcesModel(cis = Some(aCis))

            val request = FakeRequest("GET", urlPathInYear).withHeaders(headers: _*)

            lazy val result: Future[Result] = {
              dropTailoringDB()
              authoriseAgentOrIndividual(user.isAgent)
              stubIncomeSources(incomeSources)
              route(appWithInvalidEncryptionKey, request, user.isWelsh).get
            }

            "returns status of INTERNAL_SERVER_ERROR(500)" in {
              status(result) shouldBe INTERNAL_SERVER_ERROR
            }

          }
        }

      }
    }
  }
}
