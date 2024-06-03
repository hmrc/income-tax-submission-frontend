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

package models.tasklist

import models.{Enumerable, WithName}

sealed trait SectionTitleKeys extends Enumerable.Implicits

object SectionTitleKeys extends SectionTitleKeys {

  case object AboutYouTitleKey extends WithName("aboutYou") with SectionTitleKeys

  case object CharitableDonationsTitleKey extends WithName("charitableDonations") with SectionTitleKeys

  case object EmploymentTitleKey extends WithName("employment") with SectionTitleKeys

  case object SelfEmploymentTitleKey extends WithName("selfEmployment") with SectionTitleKeys

  case object EsaTitleKey extends WithName("esaTitle") with SectionTitleKeys

  case object JsaTitleKey extends WithName("jsaTitle") with SectionTitleKeys

  case object PensionsTitleKey extends WithName("pensions") with SectionTitleKeys

  case object PaymentsIntoPensionsTitleKey extends WithName("paymentsIntoPensions") with SectionTitleKeys

  case object InterestTitleKey extends WithName("interest") with SectionTitleKeys

  case object DividendsTitleKey extends WithName("dividends") with SectionTitleKeys

  val values: Seq[SectionTitleKeys] = Seq(
    AboutYouTitleKey,
    CharitableDonationsTitleKey,
    EmploymentTitleKey,
    SelfEmploymentTitleKey,
    EsaTitleKey,
    JsaTitleKey,
    PensionsTitleKey,
    PaymentsIntoPensionsTitleKey,
    InterestTitleKey,
    DividendsTitleKey
  )

  implicit val enumerable: Enumerable[SectionTitleKeys] =
    Enumerable(values.map(v => v.toString -> v): _*)
}