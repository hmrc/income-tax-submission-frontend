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

package config

import common.IncomeSources._
import play.api.i18n.Lang
import play.api.mvc.{Call, RequestHeader}
import uk.gov.hmrc.play.bootstrap.binders.SafeRedirectUrl
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import javax.inject.{Inject, Singleton}

@Singleton
class AppConfig @Inject()(servicesConfig: ServicesConfig) {

  private lazy val signInBaseUrl: String = servicesConfig.getString(ConfigKeys.signInUrl)
  def defaultTaxYear: Int = servicesConfig.getInt(ConfigKeys.defaultTaxYear)
  private lazy val signInContinueBaseUrl: String = servicesConfig.getString(ConfigKeys.signInContinueUrl)
  lazy val signInContinueUrl: String = SafeRedirectUrl(signInContinueBaseUrl).encodedUrl //TODO add redirect to overview page
  private lazy val signInOrigin = servicesConfig.getString("appName")
  lazy val signInUrl: String = s"$signInBaseUrl?continue=$signInContinueUrl&origin=$signInOrigin"

  lazy val calculationBaseUrl: String = servicesConfig.getString(ConfigKeys.incomeTaxCalculationUrl)
  lazy val incomeTaxSubmissionBaseUrl: String = servicesConfig.getString(ConfigKeys.incomeTaxSubmissionUrl)
  lazy val incomeTaxSubmissionUrl: String = s"$incomeTaxSubmissionBaseUrl/income-tax-submission-service/income-tax"
  lazy val personalIncomeTaxSubmissionBaseUrl: String = servicesConfig.getString(ConfigKeys.personalIncomeTaxFrontendUrl)
  lazy val personalIncomeTaxSubmissionUrl: String =s"$personalIncomeTaxSubmissionBaseUrl/income-through-software/return/personal-income"
  def personalIncomeTaxDividendsUrl(taxYear: Int): String = s"$personalIncomeTaxSubmissionUrl/$taxYear/dividends/dividends-from-uk-companies"
  def personalIncomeTaxDividendsSubmissionCYAUrl(taxYear: Int): String = s"$personalIncomeTaxSubmissionUrl/$taxYear/dividends/check-income-from-dividends"
  def personalIncomeTaxInterestSubmissionCYAUrl(taxYear: Int): String = s"$personalIncomeTaxSubmissionUrl/$taxYear/interest/check-interest"
  def personalIncomeTaxInterestUrl(taxYear: Int): String = s"$personalIncomeTaxSubmissionUrl/$taxYear/interest/untaxed-uk-interest"

  lazy val employmentIncomeTaxSubmissionBaseUrl: String = servicesConfig.getString(ConfigKeys.incomeTaxEmploymentFrontendUrl)
  lazy val employmentIncomeTaxSubmissionUrl: String =s"$employmentIncomeTaxSubmissionBaseUrl/income-through-software/return/employment-income"
  def employmentFEUrl(taxYear: Int): String = s"$employmentIncomeTaxSubmissionUrl/$taxYear/employment-summary"

  def personalIncomeTaxGiftAidUrl(taxYear: Int): String = s"$personalIncomeTaxSubmissionUrl/$taxYear/charity/charity-donation-using-gift-aid"
  def personalIncomeTaxGiftAidSubmissionCYAUrl(taxYear: Int): String = s"$personalIncomeTaxSubmissionUrl/$taxYear/charity/check-donations-to-charity"
  private lazy val vcBaseUrl: String = servicesConfig.getString(ConfigKeys.viewAndChangeUrl)
  def viewAndChangeCalculationUrl(taxYear: Int): String = s"$vcBaseUrl/report-quarterly/income-and-expenses/view/calculation/$taxYear/submitted"
  def viewAndChangeViewUrl: String = s"$vcBaseUrl/report-quarterly/income-and-expenses/view"
  def viewAndChangeEnterUtrUrl: String = s"$vcBaseUrl/report-quarterly/income-and-expenses/view/agents/client-utr"
  def viewAndChangeViewUrlAgent: String = s"$vcBaseUrl/report-quarterly/income-and-expenses/view/agents/income-tax-account"

  lazy private val appUrl: String = servicesConfig.getString("microservice.url")
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

  lazy val ivSuccessUrl: String = s"/income-through-software/return/iv-uplift-callback"
  lazy val ivFailureUrl: String = s"/income-through-software/return/error/we-could-not-confirm-your-details"

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

  lazy val excludedIncomeSources: Seq[String] = {
    Seq(
      (DIVIDENDS, dividendsEnabled),
      (INTEREST, interestEnabled),
      (GIFT_AID, giftAidEnabled),
      (EMPLOYMENT, employmentEnabled)
    ).filter(!_._2).map(_._1)
  }

}
