/*
 * Copyright 2023 HM Revenue & Customs
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

package models

import config.AppConfig

case class OverviewTailoringModel(tailoring: Seq[String], incomeSources: IncomeSourcesModel)(implicit appConfig: AppConfig) {
  private def bool2int(b: Boolean) = if (b) 1 else 0

  val hasDividends: Boolean = tailoring.contains("dividends") || incomeSources.dividends.nonEmpty
  val hasInterest: Boolean = tailoring.contains("interest") || incomeSources.interest.exists(accounts => accounts.exists(_.hasAmounts)) ||
    (appConfig.interestSavingsEnabled && incomeSources.interestSavings.nonEmpty)
  val hasGiftAid: Boolean = tailoring.contains("gift-aid") || incomeSources.giftAid.nonEmpty
  val hasEmployment: Boolean = tailoring.contains("employment") || incomeSources.employment.nonEmpty
  val hasCis: Boolean = tailoring.contains("cis") || incomeSources.cis.nonEmpty
  val hasPensions: Boolean = tailoring.contains("pensions") || incomeSources.pensions.nonEmpty
  val hasProperty: Boolean = tailoring.contains("property") || incomeSources.property.nonEmpty
  val hasStateBenefits: Boolean = tailoring.contains("state-benefits") || incomeSources.stateBenefits.nonEmpty
  val hasGains: Boolean = tailoring.contains("gains") || incomeSources.gains.nonEmpty
  val hasStockDividends: Boolean = tailoring.contains("stock-dividends") || incomeSources.stockDividends.nonEmpty

  val allJourneys: List[Boolean] =
    List(hasDividends, hasInterest, hasGiftAid, hasEmployment, hasGains, hasCis, hasPensions, hasProperty, hasStateBenefits, hasStockDividends)

  val sourceCount: Int = (tailoring.size - (bool2int(hasDividends) + bool2int(hasInterest) + bool2int(hasGiftAid) + bool2int(hasEmployment)
    + bool2int(hasCis) + bool2int(hasPensions) + bool2int(hasProperty) + bool2int(hasGains) + bool2int(hasStateBenefits) + bool2int(hasStockDividends))) * -1
}
