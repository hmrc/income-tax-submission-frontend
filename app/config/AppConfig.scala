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

import com.google.inject.ImplementedBy
import common.IncomeSources._
import play.api.i18n.Lang
import play.api.mvc.{Call, RequestHeader}
import uk.gov.hmrc.play.bootstrap.binders.SafeRedirectUrl
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.Inject

class FrontendAppConfig @Inject()(servicesConfig: ServicesConfig) extends AppConfig {
  private lazy val signInBaseUrl: String = servicesConfig.getString(ConfigKeys.signInUrl)
  def defaultTaxYear: Int = servicesConfig.getInt(ConfigKeys.defaultTaxYear)
  private lazy val signInContinueBaseUrl: String = servicesConfig.getString(ConfigKeys.signInContinueUrl)
  lazy val signInContinueUrl: String = SafeRedirectUrl(signInContinueBaseUrl).encodedUrl //TODO add redirect to overview page
  private lazy val signInOrigin = servicesConfig.getString("appName")
  lazy val signInUrl: String = s"$signInBaseUrl?continue=$signInContinueUrl&origin=$signInOrigin"

  lazy val calculationBaseUrl: String = servicesConfig.getString(ConfigKeys.incomeTaxCalculationUrl)
  lazy val nrsProxyBaseUrl: String = servicesConfig.getString(ConfigKeys.incomeTaxNrsProxyUrl)

  lazy val incomeTaxSubmissionBaseUrl: String = servicesConfig.getString(ConfigKeys.incomeTaxSubmissionUrl)
  lazy val incomeTaxSubmissionUrl: String = s"$incomeTaxSubmissionBaseUrl/income-tax-submission-service/income-tax"
  lazy val personalIncomeTaxSubmissionBaseUrl: String = servicesConfig.getString(ConfigKeys.personalIncomeTaxFrontendUrl)
  lazy val personalIncomeTaxSubmissionUrl: String =s"$personalIncomeTaxSubmissionBaseUrl/update-and-submit-income-tax-return/personal-income"
  def personalIncomeTaxDividendsUrl(taxYear: Int): String = s"$personalIncomeTaxSubmissionUrl/$taxYear/dividends/dividends-from-uk-companies"
  def personalIncomeTaxDividendsSubmissionCYAUrl(taxYear: Int): String = s"$personalIncomeTaxSubmissionUrl/$taxYear/dividends/check-income-from-dividends"
  def personalIncomeTaxInterestSubmissionCYAUrl(taxYear: Int): String = s"$personalIncomeTaxSubmissionUrl/$taxYear/interest/check-interest"
  def personalIncomeTaxInterestUrl(taxYear: Int): String = s"$personalIncomeTaxSubmissionUrl/$taxYear/interest/untaxed-uk-interest"
  def personalIncomeTaxGiftAidUrl(taxYear: Int): String = s"$personalIncomeTaxSubmissionUrl/$taxYear/charity/charity-donation-using-gift-aid"
  def personalIncomeTaxGiftAidSubmissionCYAUrl(taxYear: Int): String = s"$personalIncomeTaxSubmissionUrl/$taxYear/charity/check-donations-to-charity"

  lazy val employmentIncomeTaxSubmissionBaseUrl: String = servicesConfig.getString(ConfigKeys.incomeTaxEmploymentFrontendUrl)
  lazy val employmentIncomeTaxSubmissionUrl: String =s"$employmentIncomeTaxSubmissionBaseUrl/update-and-submit-income-tax-return/employment-income"
  def employmentFEUrl(taxYear: Int): String = s"$employmentIncomeTaxSubmissionUrl/$taxYear/employment-summary"
  def addEmploymentUrl(taxYear: Int): String = s"$employmentIncomeTaxSubmissionUrl/$taxYear/add-employment"

  lazy val vcBaseUrl: String = servicesConfig.getString(ConfigKeys.viewAndChangeUrl)
  def viewAndChangeViewUrl: String = s"$vcBaseUrl/report-quarterly/income-and-expenses/view"
  def viewAndChangeEnterUtrUrl: String = s"$vcBaseUrl/report-quarterly/income-and-expenses/view/agents/client-utr"
  def viewAndChangeViewUrlAgent: String = s"$vcBaseUrl/report-quarterly/income-and-expenses/view/agents/income-tax-account"
  def viewAndChangePaymentsOwedUrl: String = s"$vcBaseUrl/report-quarterly/income-and-expenses/view/payments-owed"
  def viewAndChangePaymentsOwedAgentUrl: String = s"$vcBaseUrl/report-quarterly/income-and-expenses/view/agents/payments-owed"
  def viewAndChangeNextUpdatesUrl: String = s"$vcBaseUrl/report-quarterly/income-and-expenses/view/obligations"
  def viewAndChangeNextUpdatesAgentUrl: String = s"$vcBaseUrl/report-quarterly/income-and-expenses/view/agents/obligations"
  def viewAndChangeTaxYearsUrl: String = s"$vcBaseUrl/report-quarterly/income-and-expenses/view/tax-years"
  def viewAndChangeTaxYearsAgentUrl: String = s"$vcBaseUrl/report-quarterly/income-and-expenses/view/agents/tax-years"

  def viewAndChangeFinalCalculationUrl(taxYear: Int): String = {
    s"$vcBaseUrl/report-quarterly/income-and-expenses/view/$taxYear/final-tax-overview/calculate"
  }
  def viewAndChangeFinalCalculationUrlAgent(taxYear: Int): String = {
    s"$vcBaseUrl/report-quarterly/income-and-expenses/view/agents/$taxYear/final-tax-overview/calculate"
  }

  lazy private val appUrl: String = servicesConfig.getString("microservice.url")
  lazy val incomeTaxSubmissionFrontendUrl: String = s"$appUrl/update-and-submit-income-tax-return"
  def overviewUrl(taxYear: Int): String = s"$incomeTaxSubmissionFrontendUrl/$taxYear/view"
  lazy private val contactFrontEndUrl = servicesConfig.getString(ConfigKeys.contactFrontendUrl)

  lazy private val contactFormServiceIndividual = "update-and-submit-income-tax-return"
  lazy private val contactFormServiceAgent = "update-and-submit-income-tax-return-agent"
  def contactFormServiceIdentifier(implicit isAgent: Boolean): String = if(isAgent) contactFormServiceAgent else contactFormServiceIndividual

  private def requestUri(implicit request: RequestHeader): String = SafeRedirectUrl(appUrl + request.uri).encodedUrl

  def betaFeedbackUrl(implicit request: RequestHeader, isAgent: Boolean): String = {
    s"$contactFrontEndUrl/contact/beta-feedback?service=$contactFormServiceIdentifier&backUrl=$requestUri"
  }

  private lazy val feedbackFrontendUrl = servicesConfig.getString(ConfigKeys.feedbackFrontendUrl)

  def feedbackSurveyUrl(implicit isAgent: Boolean): String = s"$feedbackFrontendUrl/feedback/$contactFormServiceIdentifier"

  def contactUrl(implicit isAgent: Boolean): String = s"$contactFrontEndUrl/contact/contact-hmrc?service=$contactFormServiceIdentifier"

  private lazy val basGatewayUrl = servicesConfig.getString(ConfigKeys.basGatewayFrontendUrl)

  lazy val signOutUrl: String = s"$basGatewayUrl/bas-gateway/sign-out-without-state"

  private lazy val ivUrl = servicesConfig.getString(ConfigKeys.identityVerificationFrontendUrl)

  lazy val ivSuccessUrl: String = s"/update-and-submit-income-tax-return/iv-uplift-callback"
  lazy val ivFailureUrl: String = s"/update-and-submit-income-tax-return/error/we-could-not-confirm-your-details"

  lazy val ivUpliftUrl: String = {
    s"$ivUrl/mdtp/uplift?origin=update-and-submit-income-tax-return&confidenceLevel=200&completionURL=$ivSuccessUrl&failureURL=$ivFailureUrl"
  }

  lazy val timeoutDialogTimeout: Int = servicesConfig.getInt("timeoutDialogTimeout")
  lazy val timeoutDialogCountdown: Int = servicesConfig.getInt("timeoutDialogCountdown")

  def taxYearErrorFeature: Boolean = servicesConfig.getBoolean("taxYearErrorFeatureSwitch")

  def languageMap: Map[String, Lang] = Map(
    "english" -> Lang("en"),
    "cymraeg" -> Lang("cy")
  )

  def routeToSwitchLanguage: String => Call =
    (lang: String) => controllers.routes.LanguageSwitchController.switchToLanguage(lang)

  lazy val welshToggleEnabled: Boolean = servicesConfig.getBoolean("feature-switch.welshToggleEnabled")

  //Enabled income sources
  lazy val dividendsEnabled: Boolean = servicesConfig.getBoolean("feature-switch.dividendsEnabled")
  lazy val interestEnabled: Boolean = servicesConfig.getBoolean("feature-switch.interestEnabled")
  lazy val giftAidEnabled: Boolean = servicesConfig.getBoolean("feature-switch.giftAidEnabled")
  lazy val giftAidReleased: Boolean = servicesConfig.getBoolean("feature-switch.giftAidReleased")
  lazy val employmentEnabled: Boolean = servicesConfig.getBoolean("feature-switch.employmentEnabled")
  lazy val employmentReleased: Boolean = servicesConfig.getBoolean("feature-switch.employmentReleased")
  lazy val nrsEnabled: Boolean = servicesConfig.getBoolean("feature-switch.nrsEnabled")
  lazy val crystallisationEnabled: Boolean = servicesConfig.getBoolean("feature-switch.crystallisationEnabled")

  lazy val excludedIncomeSources: Seq[String] = {
    Seq(
      (DIVIDENDS, dividendsEnabled),
      (INTEREST, interestEnabled),
      (GIFT_AID, giftAidEnabled),
      (EMPLOYMENT, employmentEnabled)
    ).filter(!_._2).map(_._1)
  }

  lazy val testOnly_authLoginUrl: String = servicesConfig.getString("microservice.services.auth-login-api.url")
}

@ImplementedBy(classOf[FrontendAppConfig])
trait AppConfig {
  def defaultTaxYear: Int
  val signInContinueUrl: String
  val signInUrl: String

  val calculationBaseUrl: String
  val nrsProxyBaseUrl: String

  val incomeTaxSubmissionBaseUrl: String
  val incomeTaxSubmissionUrl: String
  val personalIncomeTaxSubmissionBaseUrl: String
  val personalIncomeTaxSubmissionUrl: String
  def personalIncomeTaxDividendsUrl(taxYear: Int): String
  def personalIncomeTaxDividendsSubmissionCYAUrl(taxYear: Int): String
  def personalIncomeTaxInterestSubmissionCYAUrl(taxYear: Int): String
  def personalIncomeTaxInterestUrl(taxYear: Int): String
  def personalIncomeTaxGiftAidUrl(taxYear: Int): String
  def personalIncomeTaxGiftAidSubmissionCYAUrl(taxYear: Int): String

  val employmentIncomeTaxSubmissionBaseUrl: String
  val employmentIncomeTaxSubmissionUrl: String
  def employmentFEUrl(taxYear: Int): String
  def addEmploymentUrl(taxYear: Int): String

  val vcBaseUrl: String
  def viewAndChangeViewUrl: String
  def viewAndChangeEnterUtrUrl: String
  def viewAndChangeViewUrlAgent: String
  def viewAndChangePaymentsOwedUrl: String
  def viewAndChangePaymentsOwedAgentUrl: String
  def viewAndChangeNextUpdatesUrl: String
  def viewAndChangeNextUpdatesAgentUrl: String
  def viewAndChangeTaxYearsUrl: String
  def viewAndChangeTaxYearsAgentUrl: String

  def viewAndChangeFinalCalculationUrl(taxYear: Int): String
  def viewAndChangeFinalCalculationUrlAgent(taxYear: Int): String

  val incomeTaxSubmissionFrontendUrl: String
  def overviewUrl(taxYear: Int): String

  def contactFormServiceIdentifier(implicit isAgent: Boolean): String

  def betaFeedbackUrl(implicit request: RequestHeader, isAgent: Boolean): String

  def feedbackSurveyUrl(implicit isAgent: Boolean): String

  def contactUrl(implicit isAgent: Boolean): String

  val signOutUrl: String

  val ivSuccessUrl: String
  val ivFailureUrl: String

  val ivUpliftUrl: String

  val timeoutDialogTimeout: Int
  val timeoutDialogCountdown: Int

  def taxYearErrorFeature: Boolean

  def languageMap: Map[String, Lang]

  def routeToSwitchLanguage: String => Call

  val welshToggleEnabled: Boolean

  //Enabled income sources
  val dividendsEnabled: Boolean
  val interestEnabled: Boolean
  val giftAidEnabled: Boolean
  val giftAidReleased: Boolean
  val employmentEnabled: Boolean
  val employmentReleased: Boolean
  val nrsEnabled: Boolean
  val crystallisationEnabled: Boolean

  val excludedIncomeSources: Seq[String]

  //Test Only
  val testOnly_authLoginUrl: String
}
