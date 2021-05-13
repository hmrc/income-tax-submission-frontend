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

import scala.concurrent.duration.Duration

@Singleton
class AppConfig @Inject()(servicesConfig: ServicesConfig) {

  private lazy val signInBaseUrl: String = servicesConfig.getString(ConfigKeys.signInUrl)
  def defaultTaxYear: Int = servicesConfig.getInt(ConfigKeys.defaultTaxYear)
  private lazy val signInContinueBaseUrl: String = servicesConfig.getString(ConfigKeys.signInContinueBaseUrl)
  lazy val signInContinueUrl: String = SafeRedirectUrl(signInContinueBaseUrl).encodedUrl //TODO add redirect to overview page
  private lazy val signInOrigin = servicesConfig.getString("appName")
  lazy val signInUrl: String = s"$signInBaseUrl?continue=$signInContinueUrl&origin=$signInOrigin"

  lazy val calculationBaseUrl: String = servicesConfig.baseUrl("income-tax-calculation")
  lazy val incomeTaxSubmissionBaseUrl: String = servicesConfig.baseUrl("income-tax-submission")
  lazy val incomeTaxSubmissionUrl: String = s"$incomeTaxSubmissionBaseUrl/income-tax-submission-service/income-tax"
  lazy val personalIncomeTaxSubmissionBaseUrl: String = servicesConfig.getString(ConfigKeys.personalIncomeBaseUrl)
  lazy val personalIncomeTaxSubmissionUrl: String =s"$personalIncomeTaxSubmissionBaseUrl/income-through-software/return/personal-income"
  def personalIncomeTaxDividendsUrl(taxYear: Int): String = s"$personalIncomeTaxSubmissionUrl/$taxYear/dividends/dividends-from-uk-companies"
  def personalIncomeTaxDividendsSubmissionCYAUrl(taxYear: Int): String = s"$personalIncomeTaxSubmissionUrl/$taxYear/dividends/check-income-from-dividends"
  def personalIncomeTaxInterestSubmissionCYAUrl(taxYear: Int): String = s"$personalIncomeTaxSubmissionUrl/$taxYear/interest/check-your-answers"
  def personalIncomeTaxInterestUrl(taxYear: Int): String = s"$personalIncomeTaxSubmissionUrl/$taxYear/interest/untaxed-uk-interest"
  private lazy val vcBaseUrl: String = servicesConfig.getString(ConfigKeys.viewAndChangeBaseUrl)
  def viewAndChangeCalculationUrl(taxYear: Int): String = s"$vcBaseUrl/report-quarterly/income-and-expenses/view/calculation/$taxYear/submitted"
  def viewAndChangeViewUrl: String = s"$vcBaseUrl/report-quarterly/income-and-expenses/view"
  def viewAndChangeEnterUtrUrl: String = s"$vcBaseUrl/report-quarterly/income-and-expenses/view/agents/client-utr"
  def viewAndChangeViewUrlAgent: String = s"$vcBaseUrl/report-quarterly/income-and-expenses/view/agents/income-tax-account"

  lazy private val appUrl: String = servicesConfig.getString("microservice.url")
  lazy private val contactFrontEndUrl = {
    val contactUrl = servicesConfig.baseUrl("contact-frontend")
    servicesConfig.getConfString("contact-frontend.baseUrl", contactUrl)
  }

  lazy private val contactFormServiceIndividual = "update-and-submit-income-tax-return"
  lazy private val contactFormServiceAgent = "update-and-submit-income-tax-return-agent"
  def contactFormServiceIdentifier(implicit isAgent: Boolean): String = if(isAgent) contactFormServiceAgent else contactFormServiceIndividual


  private def requestUri(implicit request: RequestHeader): String = SafeRedirectUrl(appUrl + request.uri).encodedUrl

  def betaFeedbackUrl(implicit request: RequestHeader, isAgent: Boolean): String = {
    s"$contactFrontEndUrl/contact/beta-feedback?service=$contactFormServiceIdentifier&backUrl=$requestUri"
  }

  private lazy val feedbackFrontendUrl = {
    val feedbackSurveyUrl = servicesConfig.baseUrl("feedback-frontend")
    servicesConfig.getConfString("feedback-frontend.relativeUrl", feedbackSurveyUrl)
  }

  def feedbackSurveyUrl(implicit isAgent: Boolean): String = s"$feedbackFrontendUrl/feedback/$contactFormServiceIdentifier"

  def contactUrl(implicit isAgent: Boolean): String = s"$contactFrontEndUrl/contact/contact-hmrc?service=$contactFormServiceIdentifier"

  private lazy val basGatewayUrl = {
    val basGatewayUrl = servicesConfig.baseUrl("bas-gateway-frontend")
    servicesConfig.getConfString("bas-gateway-frontend.relativeUrl", basGatewayUrl)
  }

  lazy val signOutUrl: String = s"$basGatewayUrl/bas-gateway/sign-out-without-state"

  private lazy val ivUrl = {
    val ivBaseUrl = servicesConfig.baseUrl("identity-verification-frontend")
    servicesConfig.getConfString("identity-verification-frontend.relativeUrl", ivBaseUrl)
  }

  lazy val ivSuccessUrl: String = s"/income-through-software/return/iv-uplift-callback"
  lazy val ivFailureUrl: String = s"/income-through-software/return/error/we-could-not-confirm-your-details"

  lazy val ivUpliftUrl: String = {
    s"$ivUrl/mdtp/uplift?origin=update-and-submit-income-tax-return&confidenceLevel=200&completionURL=$ivSuccessUrl&failureURL=$ivFailureUrl"
  }

  lazy val timeoutDialogTimeout: Int = servicesConfig.getInt("timeoutDialogTimeout")
  lazy val timeoutDialogCountdown: Int = servicesConfig.getInt("timeoutDialogCountdown")

  //Mongo config
  lazy val encryptionKey: String = servicesConfig.getString("mongodb.encryption.key")
  lazy val mongoTTL: Int = Duration(servicesConfig.getString("mongodb.timeToLive")).toMinutes.toInt

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
  lazy val employmentEnabled: Boolean = servicesConfig.getBoolean("feature-switch.employmentEnabled")

  lazy val excludedIncomeSources: Seq[String] = {
    Seq(
      (DIVIDENDS, dividendsEnabled),
      (INTEREST, interestEnabled),
      (GIFT_AID, giftAidEnabled),
      (EMPLOYMENT, employmentEnabled)
    ).filter(!_._2).map(_._1)
  }

}
