/*
 * Copyright 2022 HM Revenue & Customs
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

case class OverviewTailoringModel(tailoring: Seq[String], incomeSources: IncomeSourcesModel) {
  private def bool2int(b:Boolean) = if (b) 1 else 0

  val hasDividends: Boolean = tailoring.contains("dividends") || incomeSources.dividends.nonEmpty
  val hasInterest: Boolean = tailoring.contains("interest") || incomeSources.interest.nonEmpty
  val hasGiftAid: Boolean = tailoring.contains("gift-aid") || incomeSources.giftAid.nonEmpty
  val hasEmployment: Boolean = tailoring.contains("employment") || incomeSources.employment.nonEmpty
  val hasCis: Boolean = tailoring.contains("cis") || incomeSources.cis.nonEmpty

  val sourceCount: Int =
    (tailoring.size - (bool2int(hasDividends) + bool2int(hasInterest) + bool2int(hasGiftAid) + bool2int(hasEmployment) + bool2int(hasCis))) * -1
}
