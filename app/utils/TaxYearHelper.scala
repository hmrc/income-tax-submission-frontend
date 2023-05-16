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

package utils

import common.SessionValues
import models.User

import java.time.LocalDate

trait TaxYearHelper extends SessionDataHelper {

  private val dateNow: LocalDate = LocalDate.now()
  private val taxYearCutoffDate: LocalDate = LocalDate.parse(s"${dateNow.getYear}-04-05")

  val taxYear: Int = if (dateNow.isAfter(taxYearCutoffDate)) LocalDate.now().getYear + 1 else LocalDate.now().getYear

  def retrieveTaxYearList(implicit user: User[_]): Seq[Int] = {
    user.session.get(SessionValues.VALID_TAX_YEARS).getOrElse("").split(",").toSeq.map(_.toInt)
  }

  def firstClientTaxYear(implicit user: User[_]): Int = retrieveTaxYearList.head
  def latestClientTaxYear(implicit user: User[_]): Int = retrieveTaxYearList.last

  def singleValidTaxYear(implicit user: User[_]): Boolean = firstClientTaxYear == latestClientTaxYear
}
