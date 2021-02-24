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

import org.scalamock.scalatest.MockFactory
import play.api.mvc.RequestHeader
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

class MockAppConfig extends MockFactory {

  val config = new AppConfig(mock[ServicesConfig]) {
    override lazy val signInContinueUrl: String = "/signInContinue"
    override lazy val signInUrl: String = "/signIn"
    override lazy val incomeTaxSubmissionBaseUrl: String = "/incomeTaxSubmission"
    override lazy val incomeTaxSubmissionUrl: String = s"$incomeTaxSubmissionBaseUrl/index"
    override lazy val personalIncomeTaxSubmissionBaseUrl: String = "/personalIncomeTaxSubmissionFrontend"
    override lazy val personalIncomeTaxSubmissionUrl: String = s"$personalIncomeTaxSubmissionBaseUrl/personal-income"

    override def personalIncomeTaxDividendsUrl(taxYear: Int): String = s"$personalIncomeTaxSubmissionUrl/2021/dividends"

    override def personalIncomeTaxInterestUrl(taxYear: Int): String = s"$personalIncomeTaxSubmissionUrl/2021/interest"

    override def viewAndChangeCalculationUrl(taxYear: Int): String = s"/report-quarterly/income-and-expenses/view/calculation/$taxYear/submitted"

    override lazy val defaultTaxYear: Int = 2021

    override lazy val feedbackSurveyUrl: String = "/feedbackUrl"

    override def betaFeedbackUrl(implicit request: RequestHeader): String = "feedbackUrl"

    override lazy val contactUrl: String = "/contact-frontend/contact"

    override lazy val signOutUrl: String = "/sign-out-url"
  }

}
