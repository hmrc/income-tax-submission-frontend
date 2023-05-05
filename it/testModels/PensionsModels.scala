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

package testModels

import itUtils.IntegrationTest
import models.pensions.Pensions
import models.pensions.charges._
import models.pensions.employmentPensions.{EmploymentPensionModel, EmploymentPensions}
import models.pensions.income.{ForeignPension, OverseasPensionContribution, PensionIncome}
import models.pensions.reliefs.{PensionReliefs, Reliefs}

object PensionsModels {

  object ReliefsModels {
    val anReliefs: Reliefs = Reliefs(regularPensionContributions = Some(100.00),
      oneOffPensionContributionsPaid = Some(200.00),
      retirementAnnuityPayments = Some(300.00),
      paymentToEmployersSchemeNoTaxRelief = Some(400.00),
      overseasPensionSchemeContributions = Some(500.00)
    )

    val anPensionReliefs: PensionReliefs = PensionReliefs(
      submittedOn = "2020-07-27T17:00:19Z",
      deletedOn = None,
      pensionReliefs = anReliefs
    )
  }

  object ChargesModels {
    val anLifetimeAllowance: LifetimeAllowance = LifetimeAllowance(
      amount = 22.22,
      taxPaid = 11.11
    )

    val anPensionSavngTaxCharges: PensionSavingsTaxCharges = PensionSavingsTaxCharges(
      lumpSumBenefitTakenInExcessOfLifetimeAllowance = Some(anLifetimeAllowance),
      benefitInExcessOfLifetimeAllowance = Some(anLifetimeAllowance),
      pensionSchemeTaxReference = Seq("00123456RA", "00123456RB"),
      isAnnualAllowanceReduced = true,
      taperedAnnualAllowance = Some(true),
      moneyPurchasedAllowance = Some(true)
    )

    val anOverseasSchemeProvider: OverseasSchemeProvider = OverseasSchemeProvider(
      providerName = "overseas providerName",
      providerAddress = "overseas address 1",
      providerCountryCode = "GBR",
      qualifyingRecognisedOverseasPensionScheme = Some(Seq("Q100000", "Q100002")),
      pensionSchemeTaxReference = Some(Seq("00123456RA", "00123456RB"))
    )

    val anPensionSchemeOverseasTransfers: PensionSchemeOverseasTransfers = PensionSchemeOverseasTransfers(
      overseasSchemeProvider = Seq(anOverseasSchemeProvider),
      transferCharge = 55.55,
      transferChargeTaxPaid = 66.66

    )

    val anCharge: Charge = Charge(
      amount = 666.66,
      foreignTaxPaid = 441.22
    )

    val anPensionSchemeUnauthorisedPayments: PensionSchemeUnauthorisedPayments = PensionSchemeUnauthorisedPayments(
      pensionSchemeTaxReference = Seq("00123456RA", "00123456RB"),
      surcharge = Some(anCharge),
      noSurcharge = Some(anCharge)
    )

    val anPensionContributions: PensionContributions = PensionContributions(
      pensionSchemeTaxReference = Seq("00123456RA", "00123456RB"),
      inExcessOfTheAnnualAllowance = 321.71,
      annualAllowanceTaxPaid = 888.67
    )

    val anOverseasPensionContributions: OverseasPensionContributions = OverseasPensionContributions(
      overseasSchemeProvider = Seq(anOverseasSchemeProvider),
      shortServiceRefund = 52.33,
      shortServiceRefundTaxPaid = 11.33
    )

    val anPensionCharges: PensionCharges = PensionCharges(
      submittedOn = "2020-07-27T17:00:19Z",
      pensionSavingsTaxCharges = Some(anPensionSavngTaxCharges),
      pensionSchemeOverseasTransfers = Some(anPensionSchemeOverseasTransfers),
      pensionSchemeUnauthorisedPayments = Some(anPensionSchemeUnauthorisedPayments),
      pensionContributions = Some(anPensionContributions),
      overseasPensionContributions = Some(anOverseasPensionContributions)
    )
  }

  object StateBenefitsModels extends IntegrationTest{
    val aStateBenefitsModel = allStateBenefitsData
  }

  object EmploymentPensionsModels {

    val anEmploymentPensionModel: EmploymentPensionModel = EmploymentPensionModel(
      employmentId = "00000000-0000-1000-8000-000000000001",
      pensionId = Some("Some Customer ref 1"),
      startDate = Some("2019-10-23"),
      endDate = Some("2020-10-24"),
      pensionSchemeName = "pension name 1",
      pensionSchemeRef = Some("666/66666"),
      amount = Some(211.33),
      taxPaid = Some(14.77),
      isCustomerEmploymentData = Some(true)
    )

    val anEmploymentPensions: EmploymentPensions = EmploymentPensions(
      employmentData = Seq(anEmploymentPensionModel)
    )
  }

  object PensionIncomeModel {

    val aPensionIncome: PensionIncome =
      PensionIncome(
        submittedOn = "2022-07-28T07:59:39.041Z",
        deletedOn = Some("2022-07-28T07:59:39.041Z"),
        foreignPension = Some(Seq(
          ForeignPension(
            countryCode = "FRA",
            taxableAmount = 1999.99,
            amountBeforeTax = Some(1999.99),
            taxTakenOff = Some(1999.99),
            specialWithholdingTax = Some(1999.99),
            foreignTaxCreditRelief = Some(true)
          )
        )),
        overseasPensionContribution = Some(Seq(
          OverseasPensionContribution(
            customerReference = Some("PENSIONINCOME245"),
            exemptEmployersPensionContribs = 1999.99,
            migrantMemReliefQopsRefNo = None,
            dblTaxationRelief = None,
            dblTaxationCountry = None,
            dblTaxationArticle = None,
            dblTaxationTreaty = None,
            sf74Reference = Some("SF74-123456")
          )
        ))
      )

  }


  val allPensionsModel: Pensions = Pensions(
    pensionReliefs = Some(ReliefsModels.anPensionReliefs),
    pensionCharges = Some(ChargesModels.anPensionCharges),
    stateBenefits = Some(StateBenefitsModels.aStateBenefitsModel),
    employmentPensions = Some(EmploymentPensionsModels.anEmploymentPensions),
    pensionIncome = Some(PensionIncomeModel.aPensionIncome)
  )

}
