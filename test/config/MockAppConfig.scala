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

package config

import org.scalamock.scalatest.MockFactory
import play.api.i18n.Lang
import play.api.mvc.{Call, RequestHeader}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

class MockAppConfig extends AppConfig with MockFactory {

  override lazy val signInContinueUrl: String = "/signInContinue"
  override lazy val signInUrl: String = "/signIn"
  override lazy val incomeTaxSubmissionBaseUrl: String = "/incomeTaxSubmission"
  override lazy val incomeTaxSubmissionUrl: String = s"$incomeTaxSubmissionBaseUrl/index"
  override lazy val personalIncomeTaxSubmissionBaseUrl: String = "/personalIncomeTaxSubmissionFrontend"
  override lazy val personalIncomeTaxSubmissionUrl: String = s"$personalIncomeTaxSubmissionBaseUrl/personal-income"

  override lazy val ivSuccessUrl: String = s"/update-and-submit-income-tax-return/iv-uplift-callback"
  override lazy val ivFailureUrl: String = s"/update-and-submit-income-tax-return/error/we-could-not-confirm-your-details"
  override lazy val ivUpliftUrl: String = s"/mdtp/uplift?origin=update-and-submit-income-tax-return&confidenceLevel=200&completionURL=$ivSuccessUrl&failureURL=$ivFailureUrl"

  override def personalIncomeTaxDividendsUrl(taxYear: Int): String = s"$personalIncomeTaxSubmissionUrl/2021/dividends"

  override def personalIncomeTaxInterestUrl(taxYear: Int): String = s"$personalIncomeTaxSubmissionUrl/2021/interest"

  lazy val personalIncomeTaxLocalBaseUrl: String = s"http://localhost:9308/update-and-submit-income-tax-return/personal-income"

  override def personalIncomeTaxGiftAidUrl(taxYear: Int): String = s"$personalIncomeTaxLocalBaseUrl/$taxYear/charity/charity-donation-using-gift-aid"

  override def personalIncomeTaxGiftAidSubmissionCYAUrl(taxYear: Int): String = s"$personalIncomeTaxLocalBaseUrl/$taxYear/charity/check-donations-to-charity"

  override def viewAndChangeFinalCalculationUrl(taxYear: Int): String = {
    s"/report-quarterly/income-and-expenses/view/$taxYear/final-tax-overview-and-declaration/calculate"
  }

  override def viewAndChangeFinalCalculationUrlAgent(taxYear: Int): String = {
    s"/report-quarterly/income-and-expenses/view/agents/$taxYear/final-tax-overview-and-declaration/calculate"
  }

  override lazy val defaultTaxYear: Int = 2021

  override def feedbackSurveyUrl(implicit isAgent: Boolean): String = "/feedbackUrl"

  override def betaFeedbackUrl(implicit request: RequestHeader, isAgent: Boolean): String = "feedbackUrl"

  override def contactUrl(implicit isAgent: Boolean): String = "/contact-frontend/contact"

  override lazy val signOutUrl: String = "/sign-out-url"

  override lazy val timeoutDialogCountdown: Int = 120

  override lazy val timeoutDialogTimeout: Int = 900

  override lazy val taxYearErrorFeature: Boolean = true

  override def viewAndChangeEnterUtrUrl: String = "/utr-entry"

  override lazy val welshToggleEnabled: Boolean = true

  override lazy val dividendsEnabled: Boolean = false

  override lazy val interestEnabled: Boolean = false

  override lazy val employmentEnabled: Boolean = false

  override lazy val employmentReleased: Boolean = true

  override lazy val giftAidEnabled: Boolean = false

  override lazy val giftAidReleased: Boolean = true

  override lazy val nrsEnabled: Boolean = true

  override lazy val cisEnabled: Boolean = true

  override lazy val cisReleased: Boolean = true

  override lazy val crystallisationEnabled: Boolean = false

  override def viewAndChangeViewUrl: String = "http://localhost:9081/report-quarterly/income-and-expenses/view"

  override val calculationBaseUrl: String = "/calculation"
  override val nrsProxyBaseUrl: String = "/nrs"

  override def personalIncomeTaxDividendsSubmissionCYAUrl(taxYear: Int): String = ???

  override def personalIncomeTaxInterestSubmissionCYAUrl(taxYear: Int): String = ???

  override val employmentIncomeTaxSubmissionBaseUrl: String = "/employment-tax"
  override val employmentIncomeTaxSubmissionUrl: String = "/employment-income"

  override def employmentFEUrl(taxYear: Int): String = ???

  override def addEmploymentUrl(taxYear: Int): String = ???

  override val vcBaseUrl: String = "/view-and-change"

  override def viewAndChangeViewUrlAgent: String = "/view-and-change-view-agent"

  override def viewAndChangePaymentsOwedUrl: String = ???

  override def viewAndChangePaymentsOwedAgentUrl: String = ???

  override def viewAndChangeNextUpdatesUrl: String = ???

  override def viewAndChangeNextUpdatesAgentUrl: String = ???

  override def viewAndChangeTaxYearsUrl: String = ???

  override def viewAndChangeTaxYearsAgentUrl: String = ???

  override val incomeTaxSubmissionFrontendUrl: String = "/income-tax-submission"

  override def overviewUrl(taxYear: Int): String = "/overview"

  override def contactFormServiceIdentifier(implicit isAgent: Boolean): String = ???

  override def languageMap: Map[String, Lang] = Map(
    "english" -> Lang("en"),
    "cymraeg" -> Lang("cy")
  )

  override def routeToSwitchLanguage: String => Call =
    (lang: String) => controllers.routes.LanguageSwitchController.switchToLanguage(lang)

  override val excludedIncomeSources: Seq[String] = Seq()
  override val testOnly_authLoginUrl: String = "/auth-login"
}

class MockAppConfigTaxYearFeatureOff extends MockAppConfig {
  override lazy val taxYearErrorFeature: Boolean = false
}
