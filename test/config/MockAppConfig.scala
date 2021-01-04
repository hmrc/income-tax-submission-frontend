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

class MockAppConfig extends AppConfig {
  override val signInContinueUrl: String = "/signInContinue"
  override val signInUrl: String = "/signIn"
  override val incomeTaxSubmissionBaseUrl: String = "/incomeTaxSubmission"
  override val incomeTaxSubmissionUrl: String = s"$incomeTaxSubmissionBaseUrl/index"
  override val personalIncomeTaxSubmissionBaseUrl: String = "/personalIncomeTaxSubmissionFrontend"
  override val personalIncomeTaxSubmissionUrl: String = s"$personalIncomeTaxSubmissionBaseUrl/personal-income"
  override def personalIncomeTaxDividendsUrl(taxYear: Int): String =  s"$personalIncomeTaxSubmissionUrl/2021/dividends"
  override def personalIncomeTaxInterestUrl(taxYear: Int): String =  s"$personalIncomeTaxSubmissionUrl/2021/interest"
}
