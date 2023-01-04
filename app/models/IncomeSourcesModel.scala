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

import models.cis.AllCISDeductions
import models.employment.{AllEmploymentData, HmrcEmploymentSource}
import models.pensions.Pensions
import models.statebenefits.AllStateBenefitsData
import play.api.libs.json.{Json, OFormat}

case class IncomeSourcesModel(dividends: Option[DividendsModel] = None,
                              interest: Option[Seq[InterestModel]] = None,
                              giftAid: Option[GiftAidModel] = None,
                              employment: Option[AllEmploymentData] = None,
                              cis: Option[AllCISDeductions] = None,
                              pensions: Option[Pensions] = None,
                              stateBenefits: Option[AllStateBenefitsData] = None) {

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

  val hasDataForEachIncomeSource: Boolean = dividends.nonEmpty &&
    interest.nonEmpty &&
    giftAid.nonEmpty &&
    employment.nonEmpty &&
    cis.nonEmpty &&
    stateBenefits.nonEmpty
}

object IncomeSourcesModel {
  implicit val formats: OFormat[IncomeSourcesModel] = Json.format[IncomeSourcesModel]
}

