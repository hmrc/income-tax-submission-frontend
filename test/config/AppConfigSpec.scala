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

import uk.gov.hmrc.play.bootstrap.binders.SafeRedirectUrl
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import utils.UnitTest

class AppConfigSpec extends UnitTest {

  private val mockServicesConfig: ServicesConfig = mock[ServicesConfig]
  private val appUrl = "http://localhost:9302"
  private val appConfig = new AppConfig(mockServicesConfig)

  (mockServicesConfig.getString(_: String)).expects("microservice.services.sign-in.url").returns("http://localhost:9949/auth-login-stub/gg-sign-in")
  (mockServicesConfig.getString(_: String)).expects("microservice.services.sign-in.continueUrl").returns("http://localhost:9152")

  (mockServicesConfig.getString(_: String)).expects("microservice.services.income-tax-submission.url").returns("http://income-tax-submission:9250")
  (mockServicesConfig.getString(_: String)).expects("microservice.services.personal-income-tax-submission-frontend.url").returns("http://personal:9308")
  (mockServicesConfig.getString(_: String)).expects("microservice.services.bas-gateway-frontend.url").returns("http://bas-gateway-frontend:9553")
  (mockServicesConfig.getString(_: String)).expects("microservice.services.feedback-frontend.url").returns("http://feedback-frontend:9514")
  (mockServicesConfig.getString(_: String)).expects("microservice.services.contact-frontend.url").returns("http://contact-frontend:9250")
  (mockServicesConfig.getString(_: String)).expects("microservice.services.income-tax-employment-frontend.url").returns("http://employment-frontend:9250")
  (mockServicesConfig.getString(_: String)).expects("microservice.services.view-and-change.url").returns("http://localhost:9081")
  (mockServicesConfig.getString(_: String)).expects("microservice.services.identity-verification-frontend.url").returns("http://identity")
  (mockServicesConfig.getString(_: String)).expects("microservice.services.income-tax-calculation.url").returns("http://calculation")

  (mockServicesConfig.getString _).expects("microservice.url").returns(appUrl)
  (mockServicesConfig.getString _).expects("appName").returns("income-tax-submission-frontend")

  "AppConfig" should {

    "return correct feedbackUrl when the user is an individual" in {
      val expectedBackUrl = SafeRedirectUrl(appUrl + fakeRequest.uri).encodedUrl
      val expectedServiceIdentifier = "update-and-submit-income-tax-return"

      val expectedBetaFeedbackUrl =
        s"http://contact-frontend:9250/contact/beta-feedback?service=$expectedServiceIdentifier&backUrl=$expectedBackUrl"

      val expectedFeedbackSurveyUrl = s"http://feedback-frontend:9514/feedback/$expectedServiceIdentifier"
      val expectedContactUrl = s"http://contact-frontend:9250/contact/contact-hmrc?service=$expectedServiceIdentifier"
      val expectedSignOutUrl = s"http://bas-gateway-frontend:9553/bas-gateway/sign-out-without-state"
      val expectedSignInUrl = "http://localhost:9949/auth-login-stub/gg-sign-in?continue=http%3A%2F%2Flocalhost%3A9152&origin=income-tax-submission-frontend"
      val expectedSignInContinueUrl = "http%3A%2F%2Flocalhost%3A9152"

      implicit val isAgent: Boolean = false

      appConfig.betaFeedbackUrl(fakeRequest, isAgent) shouldBe expectedBetaFeedbackUrl

      appConfig.feedbackSurveyUrl shouldBe expectedFeedbackSurveyUrl

      appConfig.contactUrl shouldBe expectedContactUrl

      appConfig.signOutUrl shouldBe expectedSignOutUrl

      appConfig.signInUrl shouldBe expectedSignInUrl
      appConfig.signInContinueUrl shouldBe expectedSignInContinueUrl

      appConfig.incomeTaxSubmissionBaseUrl shouldBe "http://income-tax-submission:9250"
      appConfig.viewAndChangeEnterUtrUrl shouldBe "http://localhost:9081/report-quarterly/income-and-expenses/view/agents/client-utr"
      appConfig.personalIncomeTaxSubmissionBaseUrl shouldBe "http://personal:9308"
      appConfig.ivUpliftUrl shouldBe "http://identity/mdtp/uplift?origin=update-and-submit-income-tax-return&confidenceLevel=200&completionURL=/income-through-software/return/iv-uplift-callback&failureURL=/income-through-software/return/error/we-could-not-confirm-your-details"
      appConfig.employmentIncomeTaxSubmissionUrl shouldBe "http://employment-frontend:9250/income-through-software/return/employment-income"
      appConfig.calculationBaseUrl shouldBe "http://calculation"
    }

    "return the correct feedback url when the user is an agent" in {
      val expectedBackUrl = SafeRedirectUrl(appUrl + fakeRequest.uri).encodedUrl
      val expectedServiceIdentifierAgent = "update-and-submit-income-tax-return-agent"

      val expectedBetaFeedbackUrl =
        s"http://contact-frontend:9250/contact/beta-feedback?service=$expectedServiceIdentifierAgent&backUrl=$expectedBackUrl"

      val expectedFeedbackSurveyUrl = s"http://feedback-frontend:9514/feedback/$expectedServiceIdentifierAgent"
      val expectedContactUrl = s"http://contact-frontend:9250/contact/contact-hmrc?service=$expectedServiceIdentifierAgent"
      val expectedSignOutUrl = s"http://bas-gateway-frontend:9553/bas-gateway/sign-out-without-state"

      implicit val isAgent: Boolean = true

      appConfig.betaFeedbackUrl(fakeRequest, isAgent) shouldBe expectedBetaFeedbackUrl

      appConfig.feedbackSurveyUrl shouldBe expectedFeedbackSurveyUrl

      appConfig.contactUrl shouldBe expectedContactUrl

      appConfig.signOutUrl shouldBe expectedSignOutUrl
    }
  }
}