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

package models

import models.cis.AllCISDeductions
import models.employment.{AllEmploymentData, HmrcEmploymentSource}
import models.pensions.Pensions
import models.statebenefits.AllStateBenefitsData
import models.gains.InsurancePoliciesModel
import models.property.AllPropertyData
import play.api.libs.json.{Format, Json, OFormat}

case class IncomeSourcesModel(
  errors: Option[Seq[(String, APIErrorBody)]] = None,
  dividends: Option[DividendsModel] = None,
  interest: Option[Seq[InterestModel]] = None,
  giftAid: Option[GiftAidModel] = None,
  employment: Option[AllEmploymentData] = None,
  cis: Option[AllCISDeductions] = None,
  pensions: Option[Pensions] = None,
  gains: Option[InsurancePoliciesModel] = None,
  stateBenefits: Option[AllStateBenefitsData] = None,
  interestSavings: Option[SavingsIncomeDataModel] = None,
  stockDividends: Option[StockDividendsModel] = None,
  property: Option[AllPropertyData] = None,
  selfEmployment: Option[SelfEmploymentData] = None
) {
  def excludeNotRelevantEmploymentData: IncomeSourcesModel = {

    val employmentData: Option[AllEmploymentData] = this.employment.map(
      allEmploymentData => allEmploymentData.copy(
        hmrcEmploymentData = excludeIgnoredHmrcEmployment(allEmploymentData.hmrcEmploymentData)
      )
    )

    val hasData: Boolean = employmentData.exists { data =>
      data.customerEmploymentData.nonEmpty || data.hmrcEmploymentData.nonEmpty || data.customerExpenses.nonEmpty || data.hmrcExpenses.nonEmpty
    }

    this.copy(employment = if (hasData) employmentData else None)
  }

  private def excludeIgnoredHmrcEmployment(employmentSources: Seq[HmrcEmploymentSource]): Seq[HmrcEmploymentSource] = {
    employmentSources.filterNot {
      _employment =>
        _employment.dateIgnored.isDefined
    }
  }

  val hasDataForEachIncomeSource: Boolean = {
    dividends.nonEmpty &&
      interest.nonEmpty &&
      giftAid.nonEmpty &&
      employment.nonEmpty &&
      cis.nonEmpty &&
      pensions.nonEmpty &&
      stateBenefits.nonEmpty &&
      interestSavings.nonEmpty &&
      gains.nonEmpty &&
      stockDividends.nonEmpty &&
      property.nonEmpty &&
      selfEmployment.nonEmpty
  }
}

object IncomeSourcesModel {
  implicit val errorFormat: Format[APIErrorBody] = Json.format[APIErrorBody]
  implicit val formats: OFormat[IncomeSourcesModel] = Json.format[IncomeSourcesModel]
}
