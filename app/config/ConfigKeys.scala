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

object ConfigKeys {
  val incomeTaxSubmissionUrl = "microservice.services.income-tax-submission.url"
  val incomeTaxCalculationUrl = "microservice.services.income-tax-calculation.url"
  val incomeTaxNrsProxyUrl = "microservice.services.income-tax-nrs-proxy.url"

  val contactFrontendUrl = "microservice.services.contact-frontend.url"
  val incomeTaxEmploymentFrontendUrl = "microservice.services.income-tax-employment-frontend.url"
  val incomeTaxCisFrontendUrl = "microservice.services.income-tax-cis-frontend.url"
  val stateBenefitsFrontendUrl = "microservice.services.income-tax-state-benefits-frontend.url"
  val selfEmploymentFrontendUrl = "microservice.services.income-tax-self-employment-frontend.url"
  val propertyFrontendUrl = "microservice.services.income-tax-property-frontend.url"
  val personalIncomeTaxFrontendUrl = "microservice.services.personal-income-tax-submission-frontend.url"
  val pensionsFrontendUrl = "microservice.services.income-tax-pensions-frontend.url"
  val additionalInformationFrontendUrl = "microservice.services.income-tax-additional-information-frontend.url"
  val basGatewayFrontendUrl = "microservice.services.bas-gateway-frontend.url"
  val feedbackFrontendUrl = "microservice.services.feedback-frontend.url"
  val identityVerificationFrontendUrl = "microservice.services.identity-verification-frontend.url"
  val viewAndChangeUrl = "microservice.services.view-and-change.url"
  val signInUrl = "microservice.services.sign-in.url"
  val signInContinueUrl = "microservice.services.sign-in.continueUrl"

  //Tailor return Phase2
  val tailorReturnFrontendUrl = "microservice.services.income-tax-tailor-returns-frontend.url"

  val defaultTaxYear = "defaultTaxYear"
  val alwaysEOY = "feature-switch.alwaysEOY"
}
