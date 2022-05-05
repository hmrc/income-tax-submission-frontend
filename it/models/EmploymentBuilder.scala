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

import models.employment._

object EmploymentBuilder {

  val aEmployment: AllEmploymentData = AllEmploymentData(
    Seq(
      HmrcEmploymentSource(
        employmentId = "00000000-0000-0000-1111-000000000000",
        employerRef = Some("666/66666"),
        employerName = "Business",
        payrollId = Some("1234567890"),
        startDate = Some("2020-01-01"),
        cessationDate = Some("2020-01-01"),
        dateIgnored = None,
        submittedOn = None,
        hmrcEmploymentFinancialData = Some(
          EmploymentFinancialData(
            employmentData = Some(EmploymentData(
              "2020-01-04T05:01:01Z",
              employmentSequenceNumber = Some("1002"),
              companyDirector = Some(false),
              closeCompany = Some(true),
              directorshipCeasedDate = Some("2020-02-12"),
              occPen = Some(false),
              disguisedRemuneration = Some(false),
              pay = Some(Pay(
                taxablePayToDate = Some(34234.15),
                totalTaxToDate = Some(6782.92),
                payFrequency = Some("CALENDAR MONTHLY"),
                paymentDate = Some("2020-04-23"),
                taxWeekNo = Some(32),
                taxMonthNo = Some(2)
              ))
            )),
            employmentBenefits = Some(
              EmploymentBenefits(
                "2020-01-04T05:01:01Z",
                benefits = Some(Benefits(
                  Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100),
                  Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100),
                  Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100)
                ))
              )
            )
          )
        ),
        customerEmploymentFinancialData = None
      )
    ),
    hmrcExpenses = Some(
      EmploymentExpenses(
        Some("2020-01-04T05:01:01Z"),
        totalExpenses = Some(800),
        expenses = Some(Expenses(
          Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100)
        ))
      )
    ),
    Seq(
      EmploymentSource(
        employmentId = "00000000-0000-0000-2222-000000000000",
        employerRef = Some("666/66666"),
        employerName = "Business",
        payrollId = Some("1234567890"),
        startDate = Some("2020-01-01"),
        cessationDate = Some("2020-01-01"),
        dateIgnored = None,
        submittedOn = Some("2020-01-01T10:00:38Z"),
        employmentData = Some(
          EmploymentData(
            "2020-01-04T05:01:01Z",
            employmentSequenceNumber = Some("1002"),
            companyDirector = Some(false),
            closeCompany = Some(true),
            directorshipCeasedDate = Some("2020-02-12"),
            occPen = Some(false),
            disguisedRemuneration = Some(false),
            pay = Some(Pay(
              taxablePayToDate = Some(34234.15),
              totalTaxToDate = Some(6782.92),
              payFrequency = Some("CALENDAR MONTHLY"),
              paymentDate = Some("2020-04-23"),
              taxWeekNo = Some(32),
              taxMonthNo = Some(2)
            ))
          )
        ),
        employmentBenefits = Some(
          EmploymentBenefits(
            "2020-01-04T05:01:01Z",
            benefits = Some(Benefits(
              Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100),
              Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100),
              Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100)
            ))
          )
        )
      )
    ),
    customerExpenses = Some(
      EmploymentExpenses(
        Some("2020-01-04T05:01:01Z"),
        totalExpenses = Some(800),
        expenses = Some(Expenses(
          Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100)
        ))
      )
    )
  )

}
