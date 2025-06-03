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

import com.google.inject.ImplementedBy
import common.IncomeSources._
import play.api.i18n.Lang
import play.api.mvc.{Call, RequestHeader}
import uk.gov.hmrc.play.bootstrap.binders.SafeRedirectUrl
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import utils.TaxYearHelper

import javax.inject.Inject
import scala.concurrent.duration.Duration

//scalastyle:off
class FrontendAppConfig @Inject()(servicesConfig: ServicesConfig,
                                  taxYearHelper: TaxYearHelper) extends AppConfig {

  private lazy val signInBaseUrl: String = servicesConfig.getString(ConfigKeys.signInUrl)
  def defaultTaxYear: Int = servicesConfig.getInt(ConfigKeys.defaultTaxYear)
  val alwaysEOY: Boolean = servicesConfig.getBoolean(ConfigKeys.alwaysEOY)
  private lazy val signInContinueBaseUrl: String = servicesConfig.getString(ConfigKeys.signInContinueUrl)
  lazy val signInContinueUrl: String = SafeRedirectUrl(signInContinueBaseUrl).encodedUrl //TODO add redirect to overview page
  private lazy val signInOrigin = servicesConfig.getString("appName")
  def signInUrl: String = s"$signInBaseUrl?continue=$signInContinueUrl&origin=$signInOrigin"

  lazy val calculationBaseUrl: String = servicesConfig.getString(ConfigKeys.incomeTaxCalculationUrl)
  lazy val nrsProxyBaseUrl: String = servicesConfig.getString(ConfigKeys.incomeTaxNrsProxyUrl)

  lazy val incomeTaxSubmissionBaseUrl: String = servicesConfig.getString(ConfigKeys.incomeTaxSubmissionUrl)
  lazy val incomeTaxSubmissionUrl: String = s"$incomeTaxSubmissionBaseUrl/income-tax-submission-service/income-tax"
  lazy val personalIncomeTaxSubmissionBaseUrl: String = servicesConfig.getString(ConfigKeys.personalIncomeTaxFrontendUrl)
  lazy val personalIncomeTaxSubmissionUrl: String =s"$personalIncomeTaxSubmissionBaseUrl/update-and-submit-income-tax-return/personal-income"
  lazy val additionalInformationSubmissionBaseUrl: String = servicesConfig.getString(ConfigKeys.additionalInformationFrontendUrl)
  lazy val additionalInformationSubmissionUrl: String =s"$additionalInformationSubmissionBaseUrl/update-and-submit-income-tax-return/additional-information"

  //Income tax calculation service
  lazy val incomeTaxCalculationServiceUrl: String = s"$calculationBaseUrl/income-tax-calculation/income-tax"

  def personalIncomeTaxDividendsUrl(taxYear: Int): String = s"$personalIncomeTaxSubmissionUrl/$taxYear/dividends/dividends-from-uk-companies"
  def personalIncomeTaxDividendsSubmissionCYAUrl(taxYear: Int): String = s"$personalIncomeTaxSubmissionUrl/$taxYear/dividends/check-income-from-dividends"
  def personalIncomeTaxInterestSubmissionCYAUrl(taxYear: Int): String = s"$personalIncomeTaxSubmissionUrl/$taxYear/interest/check-interest"
  def personalIncomeTaxInterestUrl(taxYear: Int): String = s"$personalIncomeTaxSubmissionUrl/$taxYear/interest/untaxed-uk-interest"
  def personalIncomeTaxInterestGatewayUrl(taxYear: Int): String = s"$personalIncomeTaxSubmissionUrl/$taxYear/interest/interest-from-UK"
  def personalIncomeTaxInterestSummaryUrl(taxYear: Int): String = s"$personalIncomeTaxSubmissionUrl/$taxYear/interest/interest-summary"
  def additionalInformationSummaryUrl(taxYear: Int): String = s"$additionalInformationSubmissionUrl/$taxYear/gains/summary"
  def additionalInformationGatewayUrl(taxYear: Int): String = s"$additionalInformationSubmissionUrl/$taxYear/gains/gains-gateway"
  def additionalInformationSubmissionCYAUrl(taxYear: Int): String = s"$additionalInformationSubmissionUrl/$taxYear/gains/policy-summary"
  def personalIncomeTaxGiftAidUrl(taxYear: Int): String = s"$personalIncomeTaxSubmissionUrl/$taxYear/charity/charity-donation-using-gift-aid"
  def personalIncomeTaxGiftAidSubmissionCYAUrl(taxYear: Int): String = s"$personalIncomeTaxSubmissionUrl/$taxYear/charity/check-donations-to-charity"
  def personalIncomeTaxGiftAidGatewayUrl(taxYear: Int): String = s"$personalIncomeTaxSubmissionUrl/$taxYear/charity/charity-donations-to-charity"
  def personalIncomeTaxDividendsTailorPage(taxYear: Int): String = s"$personalIncomeTaxSubmissionUrl/$taxYear/dividends/dividends-from-stocks-and-shares"
  def personalIncomeTaxDividendsGatewayUrl(taxYear: Int): String = s"$personalIncomeTaxSubmissionUrl/$taxYear/dividends/dividends-from-stocks-and-shares"

  def getExcludedJourneysUrl(taxYear: Int, nino: String): String = s"$incomeTaxSubmissionBaseUrl/income-tax-submission-service/income-tax/nino/$nino/sources/excluded-journeys/$taxYear"
  def clearExcludedJourneysUrl(taxYear: Int, nino: String): String = s"$incomeTaxSubmissionBaseUrl/income-tax-submission-service/income-tax/nino/$nino/sources/clear-excluded-journeys/$taxYear"

  lazy val employmentIncomeTaxSubmissionBaseUrl: String = servicesConfig.getString(ConfigKeys.incomeTaxEmploymentFrontendUrl)
  lazy val employmentIncomeTaxSubmissionUrl: String =s"$employmentIncomeTaxSubmissionBaseUrl/update-and-submit-income-tax-return/employment-income"
  def employmentFEUrl(taxYear: Int): String = s"$employmentIncomeTaxSubmissionUrl/$taxYear/employment-summary"
  def employmentFEGatewayUrl(taxYear: Int): String = s"$employmentIncomeTaxSubmissionUrl/$taxYear/income-from-employment"

  lazy val cisIncomeTaxSubmissionBaseUrl: String = servicesConfig.getString(ConfigKeys.incomeTaxCisFrontendUrl)
  lazy val cisIncomeTaxSubmissionUrl: String =s"$cisIncomeTaxSubmissionBaseUrl/update-and-submit-income-tax-return/construction-industry-scheme-deductions"
  def cisFEUrl(taxYear: Int): String = s"$cisIncomeTaxSubmissionUrl/$taxYear/summary"
  def cisFEGatewayUrl(taxYear: Int): String = s"$cisIncomeTaxSubmissionUrl/$taxYear/deductions-from-payments"

  lazy val stateBenefitsBaseUrl: String = servicesConfig.getString(ConfigKeys.stateBenefitsFrontendUrl)
  lazy val stateBenefitsUrl: String = s"$stateBenefitsBaseUrl/update-and-submit-income-tax-return/state-benefits"
  def stateBenefitsFEUrl(taxYear: Int): String = s"$stateBenefitsUrl/$taxYear/summary"

  lazy val selfEmploymentBaseUrl: String = servicesConfig.getString(ConfigKeys.selfEmploymentFrontendUrl)
  lazy val selfEmploymentUrl: String = s"$selfEmploymentBaseUrl/update-and-submit-income-tax-return/self-employment"
  def selfEmploymentFEUrl(taxYear: Int): String = s"$selfEmploymentUrl/$taxYear/task-list"

  lazy val propertyBaseUrl: String = servicesConfig.getString(ConfigKeys.propertyFrontendUrl)
  lazy val propertyUrl: String = s"$propertyBaseUrl/update-and-submit-income-tax-return/property"
  def propertyFEUrl(taxYear: Int): String = s"$propertyUrl/$taxYear/summary"

  lazy val pensionsFrontendBaseUrl: String = servicesConfig.getString(ConfigKeys.pensionsFrontendUrl)
  lazy val pensionsFrontendUrl: String = s"$pensionsFrontendBaseUrl/update-and-submit-income-tax-return/pensions"
  def pensionsSummaryUrl(taxYear: Int): String = s"$pensionsFrontendUrl/$taxYear/pensions-summary"

  lazy val vcBaseUrl: String = servicesConfig.getString(ConfigKeys.viewAndChangeUrl)
  lazy val vcPtaNavBarOrigin: String = "?origin=PTA"
  def viewAndChangeViewUrl: String = s"$vcBaseUrl/report-quarterly/income-and-expenses/view$vcPtaNavBarOrigin"
  def viewAndChangeViewInYearEstimateUrl: String = s"$vcBaseUrl/report-quarterly/income-and-expenses/view/tax-overview$vcPtaNavBarOrigin"
  def viewAndChangeEnterUtrUrl: String = s"$vcBaseUrl/report-quarterly/income-and-expenses/view/agents/client-utr"
  def viewAndChangeViewUrlAgent: String = s"$vcBaseUrl/report-quarterly/income-and-expenses/view/agents"
  def viewAndChangeViewInYearEstimateUrlAgent: String = s"$vcBaseUrl/report-quarterly/income-and-expenses/view/agents/tax-overview"
  def viewAndChangePaymentsOwedUrl: String = s"$vcBaseUrl/report-quarterly/income-and-expenses/view/what-you-owe$vcPtaNavBarOrigin"
  def viewAndChangePaymentsOwedAgentUrl: String = s"$vcBaseUrl/report-quarterly/income-and-expenses/view/agents/what-you-owe"

  def viewAndChangeFinalCalculationUrl(taxYear: Int): String = {
    s"$vcBaseUrl/report-quarterly/income-and-expenses/view/$taxYear/final-tax-overview/calculate$vcPtaNavBarOrigin"
  }
  def viewAndChangeFinalCalculationUrlAgent(taxYear: Int): String = {
    s"$vcBaseUrl/report-quarterly/income-and-expenses/view/agents/$taxYear/final-tax-overview/calculate$vcPtaNavBarOrigin"
  }

  lazy val vcSessionServiceBaseUrl: String = servicesConfig.baseUrl("income-tax-session-data")

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
    s"$ivUrl/mdtp/uplift?origin=update-and-submit-income-tax-return&confidenceLevel=250&completionURL=$ivSuccessUrl&failureURL=$ivFailureUrl"
  }

  lazy val timeoutDialogTimeout: Int = servicesConfig.getInt("timeoutDialogTimeout")
  lazy val timeoutDialogCountdown: Int = servicesConfig.getInt("timeoutDialogCountdown")

  def taxYearErrorFeature: Boolean = servicesConfig.getBoolean("feature-switch.taxYearErrorFeatureSwitch")

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
  lazy val interestSavingsEnabled: Boolean = servicesConfig.getBoolean("feature-switch.interestSavingsEnabled")
  lazy val giftAidEnabled: Boolean = servicesConfig.getBoolean("feature-switch.giftAidEnabled")
  lazy val giftAidReleased: Boolean = servicesConfig.getBoolean("feature-switch.giftAidReleased")
  lazy val employmentEnabled: Boolean = servicesConfig.getBoolean("feature-switch.employmentEnabled")
  lazy val studentLoansEnabled: Boolean = servicesConfig.getBoolean("feature-switch.studentLoansEnabled")
  lazy val employmentReleased: Boolean = servicesConfig.getBoolean("feature-switch.employmentReleased")
  lazy val employmentEOYEnabled: Boolean = servicesConfig.getBoolean("feature-switch.employmentEOYEnabled")
  lazy val gainsEnabled: Boolean = servicesConfig.getBoolean("feature-switch.gainsEnabled")
  lazy val gainsReleased: Boolean = servicesConfig.getBoolean("feature-switch.gainsReleased")
  lazy val stockDividendsEnabled: Boolean = servicesConfig.getBoolean("feature-switch.stockDividendsEnabled")
  lazy val stockDividendsReleased: Boolean = servicesConfig.getBoolean("feature-switch.stockDividendsReleased")
  lazy val cisEnabled: Boolean = servicesConfig.getBoolean("feature-switch.cisEnabled")
  lazy val cisReleased: Boolean = servicesConfig.getBoolean("feature-switch.cisReleased")
  lazy val stateBenefitsEnabled: Boolean = servicesConfig.getBoolean("feature-switch.stateBenefitsEnabled")
  lazy val stateBenefitsReleased: Boolean = servicesConfig.getBoolean("feature-switch.stateBenefitsReleased")
  lazy val selfEmploymentEnabled: Boolean = servicesConfig.getBoolean("feature-switch.selfEmploymentEnabled")
  lazy val selfEmploymentReleased: Boolean = servicesConfig.getBoolean("feature-switch.selfEmploymentReleased")
  lazy val propertyEnabled: Boolean = servicesConfig.getBoolean("feature-switch.propertyEnabled")
  lazy val propertyReleased: Boolean = servicesConfig.getBoolean("feature-switch.propertyReleased")
  lazy val pensionsEnabled: Boolean = servicesConfig.getBoolean("feature-switch.pensionsEnabled")
  lazy val pensionsReleased: Boolean = servicesConfig.getBoolean("feature-switch.pensionsReleased")
  lazy val nrsEnabled: Boolean = servicesConfig.getBoolean("feature-switch.nrsEnabled")
  lazy val crystallisationEnabled: Boolean = servicesConfig.getBoolean("feature-switch.crystallisationEnabled")
  lazy val tailoringEnabled: Boolean = servicesConfig.getBoolean("feature-switch.tailoringEnabled")

  //Tailor return phase2
  val tailoringPhase2Enabled: Boolean = servicesConfig.getBoolean("feature-switch.tailoringPhase2Enabled")
  val tailorReturnBaseUrl: String = servicesConfig.getString(ConfigKeys.tailorReturnFrontendUrl)
  val tailorReturnServiceUrl: String = s"$tailorReturnBaseUrl/update-and-submit-income-tax-return/tailored-return"
  def tailorReturnStartPageUrl(taxYear: Int): String = s"$tailorReturnServiceUrl/$taxYear/start"
  def tailorReturnAddSectionsPageUrl(taxYear: Int): String = s"$tailorReturnServiceUrl/$taxYear/add-sections"

  def excludedIncomeSources(inputTaxYear: Int): Seq[String] = {
    val employmentFeatureEnabled: (String, Boolean) = if(inputTaxYear != taxYearHelper.taxYear) (EMPLOYMENT, employmentEOYEnabled) else (EMPLOYMENT, employmentEnabled)
    Seq(
      (DIVIDENDS, dividendsEnabled),
      (INTEREST, interestEnabled),
      (GIFT_AID, giftAidEnabled),
      (PENSIONS, pensionsEnabled),
      (SELF_EMPLOYMENT, selfEmploymentEnabled),
      employmentFeatureEnabled,
      (CIS, cisEnabled),
      (STATE_BENEFITS, stateBenefitsEnabled),
      (INTEREST_SAVINGS, interestSavingsEnabled),
      (GAINS, gainsEnabled),
      (STOCK_DIVIDENDS, stockDividendsEnabled)
    ).filter(!_._2).map(_._1)
  }

  lazy val useEncryption: Boolean = servicesConfig.getBoolean("feature-switch.useEncryption")
  lazy val encryptionKey: String = servicesConfig.getString("mongodb.encryption.key")
  def mongoTTL: Long = Duration(servicesConfig.getString("mongodb.timeToLive")).toDays.toInt

  lazy val testOnly_authLoginUrl: String = servicesConfig.getString("microservice.services.auth-login-api.url")
}

@ImplementedBy(classOf[FrontendAppConfig])
trait AppConfig {
  def defaultTaxYear: Int
  val signInContinueUrl: String
  def signInUrl: String
  val alwaysEOY: Boolean

  val calculationBaseUrl: String
  val nrsProxyBaseUrl: String

  val incomeTaxSubmissionBaseUrl: String
  val incomeTaxSubmissionUrl: String
  val personalIncomeTaxSubmissionBaseUrl: String
  val personalIncomeTaxSubmissionUrl: String

  val incomeTaxCalculationServiceUrl: String
  def personalIncomeTaxDividendsUrl(taxYear: Int): String
  def personalIncomeTaxDividendsTailorPage(taxYear: Int): String
  def personalIncomeTaxDividendsSubmissionCYAUrl(taxYear: Int): String
  def personalIncomeTaxInterestSubmissionCYAUrl(taxYear: Int): String
  def additionalInformationSubmissionCYAUrl(taxYear: Int): String
  def additionalInformationGatewayUrl(taxYear: Int): String
  def personalIncomeTaxInterestUrl(taxYear: Int): String
  def personalIncomeTaxInterestGatewayUrl(taxYear: Int): String
  def personalIncomeTaxInterestSummaryUrl(taxYear: Int): String
  def additionalInformationSummaryUrl(taxYear: Int): String
  def personalIncomeTaxDividendsGatewayUrl(taxYear: Int): String
  def personalIncomeTaxGiftAidUrl(taxYear: Int): String
  def personalIncomeTaxGiftAidSubmissionCYAUrl(taxYear: Int): String
  def personalIncomeTaxGiftAidGatewayUrl(taxYear: Int): String

  def getExcludedJourneysUrl(taxYear: Int, nino: String): String
  def clearExcludedJourneysUrl(taxYear: Int, nino: String): String

  val employmentIncomeTaxSubmissionBaseUrl: String
  val employmentIncomeTaxSubmissionUrl: String
  def employmentFEUrl(taxYear: Int): String
  def employmentFEGatewayUrl(taxYear: Int): String

  val cisIncomeTaxSubmissionBaseUrl: String
  val cisIncomeTaxSubmissionUrl: String
  def cisFEUrl(taxYear: Int): String
  def cisFEGatewayUrl(taxYear: Int): String

  val pensionsFrontendBaseUrl: String
  val pensionsFrontendUrl: String
  def pensionsSummaryUrl(taxYear: Int): String

  val stateBenefitsBaseUrl: String
  val stateBenefitsUrl: String
  def stateBenefitsFEUrl(taxYear: Int): String

  val selfEmploymentBaseUrl: String
  val selfEmploymentUrl: String

  def selfEmploymentFEUrl(taxYear: Int): String

  val propertyBaseUrl: String
  val propertyUrl: String
  def propertyFEUrl(taxYear: Int): String

  val vcBaseUrl: String
  def viewAndChangeViewUrl: String
  def viewAndChangeViewInYearEstimateUrl: String
  def viewAndChangeEnterUtrUrl: String
  def viewAndChangeViewUrlAgent: String
  def viewAndChangeViewInYearEstimateUrlAgent: String
  def viewAndChangePaymentsOwedUrl: String
  def viewAndChangePaymentsOwedAgentUrl: String

  def viewAndChangeFinalCalculationUrl(taxYear: Int): String
  def viewAndChangeFinalCalculationUrlAgent(taxYear: Int): String

  def vcSessionServiceBaseUrl: String

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
  val interestSavingsEnabled: Boolean
  val giftAidEnabled: Boolean
  val giftAidReleased: Boolean
  val gainsEnabled: Boolean
  val gainsReleased: Boolean
  val stockDividendsEnabled: Boolean
  val stockDividendsReleased: Boolean
  val studentLoansEnabled: Boolean
  val employmentEnabled: Boolean
  val employmentReleased: Boolean
  val employmentEOYEnabled: Boolean
  val cisEnabled: Boolean
  val cisReleased: Boolean
  val pensionsEnabled: Boolean
  val pensionsReleased: Boolean
  val propertyEnabled: Boolean
  val propertyReleased: Boolean
  val stateBenefitsEnabled: Boolean
  val stateBenefitsReleased: Boolean
  val selfEmploymentEnabled: Boolean
  val selfEmploymentReleased: Boolean
  val nrsEnabled: Boolean
  val crystallisationEnabled: Boolean
  val tailoringEnabled: Boolean

  //Tailor return Phase2
  val tailoringPhase2Enabled: Boolean
  val tailorReturnBaseUrl: String
  val tailorReturnServiceUrl: String
  def tailorReturnStartPageUrl(taxYear: Int): String
  def tailorReturnAddSectionsPageUrl(taxYear: Int): String

  def excludedIncomeSources(taxYear: Int): Seq[String]

  val useEncryption: Boolean
  val encryptionKey: String
  def mongoTTL: Long
  //Test Only
  val testOnly_authLoginUrl: String
}
