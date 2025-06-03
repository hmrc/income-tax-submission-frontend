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

package config

import play.api.i18n.Lang
import play.api.mvc.{Call, RequestHeader}

//noinspection ScalaStyle
//@deprecated("Use `ScalamockAppConfig` instead, rather than this concrete class instance. Future refactor to remove this")
class MockAppConfig extends AppConfig  {

  override lazy val signInContinueUrl: String = "/signInContinue"
  override lazy val signInUrl: String = "/signIn"
  override lazy val incomeTaxSubmissionBaseUrl: String = "/incomeTaxSubmission"
  override lazy val incomeTaxSubmissionUrl: String = s"$incomeTaxSubmissionBaseUrl/index"
  override lazy val personalIncomeTaxSubmissionBaseUrl: String = "/personalIncomeTaxSubmissionFrontend"
  override lazy val personalIncomeTaxSubmissionUrl: String = s"$personalIncomeTaxSubmissionBaseUrl/personal-income"
  override lazy val tailorReturnBaseUrl: String = "http://localhost:10007"
  override lazy val tailorReturnServiceUrl: String = s"$tailorReturnBaseUrl/update-and-submit-income-tax-return/tailored-return"

  override val sessionCookieServiceEnabled: Boolean = false

  override lazy val incomeTaxCalculationServiceUrl: String = "/income-tax-calculation/income-tax"
  lazy val additionalInformationTaxSubmissionUrl: String = s"$personalIncomeTaxSubmissionBaseUrl/additional-information"

  override lazy val ivSuccessUrl: String = s"/update-and-submit-income-tax-return/iv-uplift-callback"
  override lazy val ivFailureUrl: String = s"/update-and-submit-income-tax-return/error/we-could-not-confirm-your-details"
  override lazy val ivUpliftUrl: String = s"/mdtp/uplift?origin=update-and-submit-income-tax-return&confidenceLevel=250&completionURL=$ivSuccessUrl&failureURL=$ivFailureUrl"

  override def tailorReturnStartPageUrl(taxYear: Int): String = s"$tailorReturnServiceUrl/$taxYear/start"
  override def tailorReturnAddSectionsPageUrl(taxYear: Int): String = s"$tailorReturnServiceUrl/$taxYear/add-sections"

  override def additionalInformationSummaryUrl(taxYear: Int): String = s"$additionalInformationTaxSubmissionUrl/$taxYear/gains/summary"

  override def additionalInformationGatewayUrl(taxYear: Int): String = s"$additionalInformationTaxSubmissionUrl/$taxYear/gains/gains-gateway"

  override def additionalInformationSubmissionCYAUrl(taxYear: Int): String = s"$additionalInformationTaxSubmissionUrl/$taxYear/gains/policy-summary"
  override def personalIncomeTaxDividendsUrl(taxYear: Int): String = s"$personalIncomeTaxSubmissionUrl/2021/dividends"

  override def personalIncomeTaxDividendsTailorPage(taxYear: Int) = s"$personalIncomeTaxSubmissionUrl/$taxYear/dividends-from-uk-companies"

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

  override val vcSessionServiceBaseUrl: String = "/incomeTaxSessionData"

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

  override lazy val interestSavingsEnabled: Boolean = false

  override lazy val studentLoansEnabled: Boolean = true

  override lazy val employmentEnabled: Boolean = false

  override lazy val employmentReleased: Boolean = true

  override lazy val employmentEOYEnabled: Boolean = true

  override lazy val giftAidEnabled: Boolean = false

  override lazy val giftAidReleased: Boolean = true

  override lazy val nrsEnabled: Boolean = true

  override lazy val gainsEnabled: Boolean = true

  override lazy val gainsReleased: Boolean = true

  override lazy val cisEnabled: Boolean = true

  override lazy val cisReleased: Boolean = true

  override lazy val stateBenefitsEnabled: Boolean = true

  override lazy val stateBenefitsReleased: Boolean = true

  override lazy val selfEmploymentEnabled: Boolean = true

  override lazy val selfEmploymentReleased: Boolean = true

  override lazy val propertyEnabled: Boolean = true

  override lazy val propertyReleased: Boolean = true

  override lazy val crystallisationEnabled: Boolean = false

  override lazy val tailoringEnabled: Boolean = false

  override lazy val tailoringPhase2Enabled: Boolean = false

  override lazy val pensionsEnabled: Boolean = true

  override lazy val pensionsReleased: Boolean = true

  override val stockDividendsEnabled: Boolean = true

  override val stockDividendsReleased: Boolean = true

  override def viewAndChangeViewUrl: String = "http://localhost:9081/report-quarterly/income-and-expenses/view"

  override def viewAndChangeViewInYearEstimateUrl: String = "http://localhost:9081/report-quarterly/income-and-expenses/view/tax-overview"

  override def viewAndChangeViewInYearEstimateUrlAgent: String = "http://localhost:9081/report-quarterly/income-and-expenses/view/agents/tax-overview"

  override val calculationBaseUrl: String = "/calculation"
  override val nrsProxyBaseUrl: String = "/nrs"

  override def personalIncomeTaxDividendsSubmissionCYAUrl(taxYear: Int): String = ???

  override def personalIncomeTaxInterestSubmissionCYAUrl(taxYear: Int): String = ???

  override val employmentIncomeTaxSubmissionBaseUrl: String = "/employment-tax"
  override val employmentIncomeTaxSubmissionUrl: String = "/employment-income"

  override def employmentFEUrl(taxYear: Int): String = ???
  override def employmentFEGatewayUrl(taxYear: Int): String = ???

  override val cisIncomeTaxSubmissionBaseUrl: String = "/cis-tax"
  override val cisIncomeTaxSubmissionUrl: String = "/cis-income"

  override def cisFEUrl(taxYear: Int): String = ???

  override def cisFEGatewayUrl(taxYear: Int): String = ???

  override val stateBenefitsBaseUrl: String = "/state-benefits"
  override val stateBenefitsUrl: String = "/summary"

  override def stateBenefitsFEUrl(taxYear: Int): String = ???

  override val selfEmploymentBaseUrl: String = "/self-employment"
  override val selfEmploymentUrl: String = "/task-list"

  override def selfEmploymentFEUrl(taxYear: Int): String = ???

  override val propertyBaseUrl: String = "/property"
  override val propertyUrl: String = "/summary"

  override def propertyFEUrl(taxYear: Int): String = ???

  override val pensionsFrontendBaseUrl: String = "/pensions-income"
  override val pensionsFrontendUrl: String = "/pensions"

  override def pensionsSummaryUrl(taxYear: Int): String = ???

  override val vcBaseUrl: String = "/view-and-change"

  override def viewAndChangeViewUrlAgent: String = "/view-and-change-view-agent"

  override def viewAndChangePaymentsOwedUrl: String = ???

  override def viewAndChangePaymentsOwedAgentUrl: String = ???

  override val incomeTaxSubmissionFrontendUrl: String = "/income-tax-submission"

  override def overviewUrl(taxYear: Int): String = "/overview"

  override def contactFormServiceIdentifier(implicit isAgent: Boolean): String = ???

  override def languageMap: Map[String, Lang] = Map(
    "english" -> Lang("en"),
    "cymraeg" -> Lang("cy")
  )

  override val useEncryption: Boolean = true
  override val encryptionKey: String = "1234556"

  override def mongoTTL: Long = 2555

  override def routeToSwitchLanguage: String => Call =
    (lang: String) => controllers.routes.LanguageSwitchController.switchToLanguage(lang)

  override def excludedIncomeSources(taxYear: Int): Seq[String] = Seq()

  override val testOnly_authLoginUrl: String = "/auth-login"

  override def personalIncomeTaxInterestGatewayUrl(taxYear: Int): String = s"$taxYear/interest/tailoring-gateway"

  override def personalIncomeTaxDividendsGatewayUrl(taxYear: Int): String = s"$taxYear/dividends/dividends-from-uk-companies"

  override def personalIncomeTaxGiftAidGatewayUrl(taxYear: Int): String = s"$taxYear/charity/charity-donations-to-charity"

  override def getExcludedJourneysUrl(taxYear: Int, nino: String): String = "/get-excluded"

  override def clearExcludedJourneysUrl(taxYear: Int, nino: String): String = "/clear-excluded"

  override def personalIncomeTaxInterestSummaryUrl(taxYear: Int): String = s"/$taxYear/interest/interest-summary"

  override val alwaysEOY: Boolean = false
}

class MockAppConfigTaxYearFeatureOff extends MockAppConfig {
  override lazy val taxYearErrorFeature: Boolean = false
}

class MockAppConfigEncryptionOff extends MockAppConfig {
  override val useEncryption: Boolean = false
}
