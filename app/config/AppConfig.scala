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

import play.api.mvc.RequestHeader
import uk.gov.hmrc.play.bootstrap.binders.SafeRedirectUrl
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}

@Singleton
class AppConfig @Inject()(servicesConfig: ServicesConfig) {

  private lazy val signInBaseUrl: String = servicesConfig.getString(ConfigKeys.signInUrl)
  lazy val defaultTaxYear: Int = servicesConfig.getInt(ConfigKeys.defaultTaxYear)
  private lazy val signInContinueBaseUrl: String = servicesConfig.getString(ConfigKeys.signInContinueBaseUrl)
  lazy val signInContinueUrl: String = SafeRedirectUrl(signInContinueBaseUrl).encodedUrl //TODO add redirect to overview page
  private lazy val signInOrigin = servicesConfig.getString("appName")
  lazy val signInUrl: String = s"$signInBaseUrl?continue=$signInContinueUrl&origin=$signInOrigin"

  lazy val calculationBaseUrl: String = servicesConfig.baseUrl("income-tax-calculation")
  lazy val incomeTaxSubmissionBaseUrl: String = servicesConfig.baseUrl("income-tax-submission")
  lazy val incomeTaxSubmissionUrl: String = s"$incomeTaxSubmissionBaseUrl/income-tax-submission-service/income-tax"
  lazy val personalIncomeTaxSubmissionBaseUrl: String = servicesConfig.getString(ConfigKeys.personalIncomeBaseUrl)
  lazy val personalIncomeTaxSubmissionUrl: String =s"$personalIncomeTaxSubmissionBaseUrl/income-through-software/return/personal-income"
  def personalIncomeTaxDividendsUrl(taxYear: Int): String = s"$personalIncomeTaxSubmissionUrl/$taxYear/dividends/uk-dividends"
  def personalIncomeTaxDividendsSubmissionCYAUrl(taxYear: Int): String = s"$personalIncomeTaxSubmissionUrl/$taxYear/dividends/check-your-answers"
  def personalIncomeTaxInterestSubmissionCYAUrl(taxYear: Int): String = s"$personalIncomeTaxSubmissionUrl/$taxYear/interest/check-your-answers"
  def personalIncomeTaxInterestUrl(taxYear: Int): String = s"$personalIncomeTaxSubmissionUrl/$taxYear/interest/untaxed-uk-interest"
  private lazy val vcBaseUrl: String = servicesConfig.getString(ConfigKeys.viewAndChangeBaseUrl)
  def viewAndChangeCalculationUrl(taxYear: Int): String = s"$vcBaseUrl/report-quarterly/income-and-expenses/view/calculation/$taxYear/submitted"
  def viewAndChangeViewUrl: String = s"$vcBaseUrl/report-quarterly/income-and-expenses/view"
  def viewAndChangeViewUrlAgent: String = s"$vcBaseUrl/report-quarterly/income-and-expenses/view/client"

  lazy private val appUrl: String = servicesConfig.getString("microservice.url")
  lazy private val contactFrontEndUrl = {
    val contactUrl = servicesConfig.baseUrl("contact-frontend")
    servicesConfig.getConfString("contact-frontend.baseUrl", contactUrl)
  }

  lazy private val contactFormServiceIdentifier = "update-and-submit-income-tax-return"

  private def requestUri(implicit request: RequestHeader): String = SafeRedirectUrl(appUrl + request.uri).encodedUrl

  def betaFeedbackUrl(implicit request: RequestHeader): String = {
    s"$contactFrontEndUrl/contact/beta-feedback?service=$contactFormServiceIdentifier&backUrl=$requestUri"
  }

  private lazy val feedbackFrontendUrl = {
    val feedbackSurveyUrl = servicesConfig.baseUrl("feedback-frontend")
    servicesConfig.getConfString("feedback-frontend.relativeUrl", feedbackSurveyUrl)
  }

  lazy val feedbackSurveyUrl: String = s"$feedbackFrontendUrl/feedback/$contactFormServiceIdentifier"

  lazy val contactUrl = s"$contactFrontEndUrl/contact/contact-hmrc?service=$contactFormServiceIdentifier"

  private lazy val basGatewayUrl = {
    val basGatewayUrl = servicesConfig.baseUrl("bas-gateway-frontend")
    servicesConfig.getConfString("bas-gateway-frontend.relativeUrl", basGatewayUrl)
  }

  lazy val signOutUrl: String = s"$basGatewayUrl/bas-gateway/sign-out-without-state"

  lazy val timeoutDialogTimeout: Int = servicesConfig.getInt("timeoutDialogTimeout")
  lazy val timeoutDialogCountdown: Int = servicesConfig.getInt("timeoutDialogCountdown")


}