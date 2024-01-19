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

import play.api.libs.json.{Json, OFormat}

case class StockDividendsModel(
                                submittedOn: Option[String] = None,
                                foreignDividend: Option[Seq[ForeignInterestModel]] = None,
                                dividendIncomeReceivedWhilstAbroad: Option[Seq[ForeignInterestModel]] = None,
                                stockDividend: Option[Dividend] = None,
                                redeemableShares: Option[Dividend] = None,
                                bonusIssuesOfSecurities: Option[Dividend] = None,
                                closeCompanyLoansWrittenOff: Option[Dividend] = None
                              )

object StockDividendsModel {
  implicit val formats: OFormat[StockDividendsModel] = Json.format[StockDividendsModel]
}

case class Dividend(customerReference: Option[String] = None, grossAmount: Option[BigDecimal] = None)

object Dividend {
  implicit val formats: OFormat[Dividend] = Json.format[Dividend]
}
