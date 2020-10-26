/*
 * Copyright 2020 HM Revenue & Customs
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

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.binders.SafeRedirectUrl
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

trait AppConfig {
  val footerLinkItems: Seq[String]

  val signInContinueUrl: String
  val signInUrl: String

  val incomeTaxSubmissionBaseUrl: String
  val incomeTaxSubmissionUrl: String
  val personalIncomeTaxSubmissionBaseUrl: String
  val personalIncomeTaxSubmissionUrl: String
  val personalIncomeTaxSubmissionDividendsUrl: String
}

@Singleton
class FrontendAppConfig @Inject()(config: Configuration, servicesConfig: ServicesConfig) extends AppConfig {
  val footerLinkItems: Seq[String] = config.getOptional[Seq[String]]("footerLinkItems").getOrElse(Seq())

  private val signInBaseUrl: String = config.get[String](ConfigKeys.signInUrl)
  private val signInContinueBaseUrl: String = config.get[String](ConfigKeys.signInContinueBaseUrl)
  override val signInContinueUrl: String = SafeRedirectUrl(signInContinueBaseUrl).encodedUrl //TODO add redirect to overview page
  private val signInOrigin = servicesConfig.getString("appName")
  override val signInUrl: String = s"$signInBaseUrl?continue=$signInContinueUrl&origin=$signInOrigin"

  lazy val incomeTaxSubmissionBaseUrl: String = servicesConfig.baseUrl("income-tax-submission")
  lazy val incomeTaxSubmissionUrl: String = s"$incomeTaxSubmissionBaseUrl/income-tax-submission-service/income-tax"
  lazy val personalIncomeTaxSubmissionBaseUrl: String = servicesConfig.baseUrl("personal-income-tax-submission-frontend")
  lazy val personalIncomeTaxSubmissionUrl: String =s"$personalIncomeTaxSubmissionBaseUrl/income-through-software/return/personal-income"
  lazy val personalIncomeTaxSubmissionDividendsUrl: String =s"$personalIncomeTaxSubmissionUrl/dividends/uk-dividends"
}
