
# income-tax-submission-frontend
This is where users can view the overall state of their income tax submission, tailoring their return, start submitting or append an income source,
or submit a completed Submission.

## Running the service locally

You will need to have the following:
- Installed [MongoDB](https://docs.mongodb.com/manual/installation/)
- Installed/configured [service manager](https://github.com/hmrc/service-manager).

The service manager profile for this service is:

    sm --start INCOME_TAX_SUBMISSION_FRONTEND
Run the following command to start the remaining services locally:

    sudo mongod (If not already running)
    sm --start INCOME_TAX_SUBMISSION_ALL -r

This service runs on port: `localhost:9302`

### Feature Switches

| Feature | Environments Enabled In |
| --- | --- |
| Welsh | Local, QA, Staging |
| NRS | Local, QA, Staging, Production |
| Tax Year Error | Production |
| Dividends | Local, QA, Staging, Production, ET |
| Interest | Local, QA, Staging, Production, ET |
| GiftAid | Local, QA, Staging, Production, ET |
| Student loans | Local, QA, Staging, ET |
| EmploymentsEnabled | Local, QA, Staging, Production, ET |
| EmploymentsReleased | Local, QA, Staging, Production, ET |
| End of year employment | Local, QA, Staging, ET |
| CISEnabled | Local, QA, Staging |
| CISReleased | Local, QA, Staging |
| CrystallisationEnabled | Local, QA, Staging, Production, ET |
| TailoringEnabled | Local |

## Auth Setup - How to enter the service

auth-wizard - http://localhost:9949/auth-login-stub/gg-sign-in

### Example Auth Setup - Individual

| FieldName | Value                                                                |
| --- |----------------------------------------------------------------------|
| Redirect url        | http://localhost:9302/update-and-submit-income-tax-return/2022/start |
| Credential Strength | strong                                                               |
| Confidence Level    | 250                                                                  |
| Affinity Group      | Individual                                                           |
| Nino                | AA123456A                                                            |
| Enrolment Key 1     | HMRC-MTD-IT                                                          |
| Identifier Name 1   | MTDITID                                                              |
| Identifier Value 1  | 1234567890                                                           |

### Example Auth Setup - Agent

if running locally outside service manager ensure service is ran including testOnly Routes:

    sbt run -Dapplication.router=testOnlyDoNotUseInAppConf.Routes

| FieldName | Value                                                                             |
| --- |-----------------------------------------------------------------------------------|
| Redirect url        | /test-only/2022/additional-parameters?ClientNino=AA123457A&ClientMTDID=1234567890 |
| Credential Strength | weak                                                                              |
| Confidence Level    | 250                                                                               |
| Affinity Group      | Agent                                                                             |
| Enrolment Key 1     | HMRC-MTD-IT                                                                       |
| Identifier Name 1   | MTDITID                                                                           |
| Identifier Value 1  | 1234567890                                                                        |
| Enrolment Key 2     | HMRC-AS-AGENT                                                                     |
| Identifier Name 2   | AgentReferenceNumber                                                              |
| Identifier Value 2  | XARN1234567                                                                       

## Income Sources
Income-Tax-Submission-Frontend is the root of the users journey and links outward to all income sources
passing along any previously submitted data.
<details>
<summary>Click here to see an example of a user with previously submitted data(JSON)</summary>

```json
{
  "dividends": {
    "ukDividends": 99999999999.99
  },
  "interest": [
    {
      "accountName": "Rick Owens Bank",
      "incomeSourceId": "000000000000001",
      "taxedUkInterest": 99999999999.99,
      "untaxedUkInterest": 99999999999.99
    },
    {
      "accountName": "Rick Owens Taxed Bank",
      "incomeSourceId": "000000000000002",
      "taxedUkInterest": 99999999999.99
    },
    {
      "accountName": "Rick Owens Untaxed Bank",
      "incomeSourceId": "000000000000003",
      "untaxedUkInterest": 99999999999.99
    }
  ],
  "giftAid": {
    "giftAidPayments": {
      "nonUkCharitiesCharityNames": [
        "Rick Owens Charity"
      ],
      "currentYear": 99999999999.99,
      "oneOffCurrentYear": 99999999999.99,
      "currentYearTreatedAsPreviousYear": 99999999999.99,
      "nextYearTreatedAsCurrentYear": 99999999999.99,
      "nonUkCharities": 99999999999.99
    },
    "gifts": {
      "investmentsNonUkCharitiesCharityNames": [
        "Rick Owens Non-UK Charity"
      ],
      "landAndBuildings": 99999999999.99,
      "sharesOrSecurities": 99999999999.99,
      "investmentsNonUkCharities": 99999999999.99
    }
  },
  "employment": {
    "hmrcEmploymentData": [
      {
        "employmentId": "00000000-0000-0000-0000-000000000001",
        "employerName": "Rick Owens Milan LTD",
        "employerRef": "666/66666",
        "payrollId": "123456789",
        "startDate": "2020-01-04",
        "cessationDate": "2020-01-04",
        "employmentData": {
          "submittedOn": "2020-01-04T05:01:01Z",
          "pay": {
            "taxablePayToDate": 666.66,
            "totalTaxToDate": 666.66,
            "payFrequency": "CALENDAR MONTHLY",
            "paymentDate": "2020-04-23",
            "taxWeekNo": 32
          }
        }
      }
    ],
    "hmrcExpenses": {
      "submittedOn": "2022-12-12T12:12:12Z",
      "totalExpenses": 100,
      "expenses": {
        "businessTravelCosts": 100,
        "jobExpenses": 100,
        "flatRateJobExpenses": 100,
        "professionalSubscriptions": 100,
        "hotelAndMealExpenses": 100,
        "otherAndCapitalAllowances": 100,
        "vehicleExpenses": 100,
        "mileageAllowanceRelief": 100
      }
    },
    "customerEmploymentData": [
      {
        "employmentId": "00000000-0000-0000-0000-000000000002",
        "employerName": "Rick Owens London LTD",
        "employerRef": "666/66666",
        "payrollId": "123456789",
        "startDate": "2020-02-04",
        "cessationDate": "2020-02-04",
        "submittedOn": "2020-02-04T05:01:01Z",
        "employmentData": {
          "submittedOn": "2020-02-04T05:01:01Z",
          "pay": {
            "taxablePayToDate": 555.55,
            "totalTaxToDate": 555.55,
            "payFrequency": "CALENDAR MONTHLY",
            "paymentDate": "2020-04-23",
            "taxWeekNo": 32
          }
        }
      }
    ]
  },
  "pensions": [
  {
          "taxYear": 2023,
          "pensionReliefs": {
              "submittedOn": "2020-07-27T17:00:19Z",
              "pensionReliefs": {
                  "regularPensionContributions": 50,
                  "oneOffPensionContributionsPaid": 170,
                  "retirementAnnuityPayments": 180,
                  "paymentToEmployersSchemeNoTaxRelief": 60,
                  "overseasPensionSchemeContributions": 40
              }
          },
          "pensionCharges": {
              "submittedOn": "2020-07-27T17:00:19Z",
              "pensionSavingsTaxCharges": {
                  "pensionSchemeTaxReference": ["00123456RA", "00123456RB"],
                  "lumpSumBenefitTakenInExcessOfLifetimeAllowance": {
                      "amount": 800.02,
                      "taxPaid": 200.02
                  },
                  "benefitInExcessOfLifetimeAllowance": {
                      "amount": 800.02,
                      "taxPaid": 200.02
                  },
                  "isAnnualAllowanceReduced": false,
                  "taperedAnnualAllowance": false,
                  "moneyPurchasedAllowance": false
              },
              "pensionSchemeOverseasTransfers": {
                  "overseasSchemeProvider": [
                {
                      "providerName": "overseas providerName 1 qualifying scheme",
                      "providerAddress": "overseas address 1",
                      "providerCountryCode": "ESP",
                      "qualifyingRecognisedOverseasPensionScheme": ["Q100000", "Q100002"]
                  }
                ],
                  "transferCharge": 123.45,
                  "transferChargeTaxPaid": 0
              },
              "pensionSchemeUnauthorisedPayments": {
                  "pensionSchemeTaxReference": [
                    "00123456RA", "00123456RB"
                  ],
                  "surcharge": {
                      "amount": 124.44,
                      "foreignTaxPaid": 123.33
                  },
                  "noSurcharge": {
                      "amount": 222.44,
                      "foreignTaxPaid": 223.33
                  }
              },
              "pensionContributions": {
                  "pensionSchemeTaxReference": [
                  "00123456RA", "00123456RB"
                  ],
                  "inExcessOfTheAnnualAllowance": 150.67,
                  "annualAllowanceTaxPaid": 178.65
              },
              "overseasPensionContributions": {
                  "overseasSchemeProvider": [
                    {
                      "providerName": "overseas providerName 1 tax ref",
                      "providerAddress": "overseas address 1",
                      "providerCountryCode": "ESP",
                      "pensionSchemeTaxReference": [
                      "00123456RA", "00123456RB"
                    ]
                  }
              ],
                  "shortServiceRefund": 1.11,
                  "shortServiceRefundTaxPaid": 2.22
              }
          },
          "stateBenefits": {
              "stateBenefits": {
                  "incapacityBenefit": [
                    {
                      "benefitId": "a1e8057e-fbbc-47a8-a8b4-78d9f015c934",
                      "startDate": "2019-11-13",
                      "dateIgnored": "2019-04-11T16:22:00Z",
                      "submittedOn": "2020-09-11T17:23:00Z",
                      "endDate": "2020-08-23",
                      "amount": 1212.34,
                      "taxPaid": 22323.23
                    }
                  ],
                  "statePension": {
                      "benefitId": "a1e8057e-fbbc-47a8-a8b4-78d9f015c935",
                      "startDate": "2018-06-03",
                      "dateIgnored": "2018-09-09T19:23:00Z",
                      "submittedOn": "2020-08-07T12:23:00Z",
                      "endDate": "2020-09-13",
                      "amount": 42323.23,
                      "taxPaid": 2323.44
                  },
                  "statePensionLumpSum": {
                      "benefitId": "a1e8057e-fbbc-47a8-a8b4-78d9f015c936",
                      "startDate": "2019-04-23",
                      "dateIgnored": "2019-07-08T05:23:00Z",
                      "submittedOn": "2020-03-13T19:23:00Z",
                      "endDate": "2020-08-13",
                      "amount": 45454.23,
                      "taxPaid": 45432.56
                  },
                  "employmentSupportAllowance": [
                    {
                      "benefitId": "a1e8057e-fbbc-47a8-a8b4-78d9f015c937",
                      "startDate": "2019-09-23",
                      "dateIgnored": "2019-09-28T10:23:00Z",
                      "submittedOn": "2020-11-13T19:23:00Z",
                      "endDate": "2020-08-23",
                      "amount": 44545.43,
                      "taxPaid": 35343.23
                    }
                  ],
                  "jobSeekersAllowance": [
                    {
                      "benefitId": "a1e8057e-fbbc-47a8-a8b4-78d9f015c938",
                      "startDate": "2019-09-19",
                      "dateIgnored": "2019-08-18T13:23:00Z",
                      "submittedOn": "2020-07-10T18:23:00Z",
                      "endDate": "2020-09-23",
                      "amount": 33223.12,
                      "taxPaid": 44224.56
                    }
                  ],
                  "bereavementAllowance": {
                      "benefitId": "a1e8057e-fbbc-47a8-a8b4-78d9f015c939",
                      "startDate": "2019-05-22",
                      "dateIgnored": "2020-08-10T12:23:00Z",
                      "submittedOn": "2020-09-19T19:23:00Z",
                      "endDate": "2020-09-26",
                      "amount": 56534.23,
                      "taxPaid": 34343.57
                  },
                  "otherStateBenefits": {
                      "benefitId": "a1e8057e-fbbc-47a8-a8b4-78d9f015c940",
                      "startDate": "2018-09-03",
                      "dateIgnored": "2020-01-11T15:23:00Z",
                      "submittedOn": "2020-09-13T15:23:00Z",
                      "endDate": "2020-06-03",
                      "amount": 56532.45,
                      "taxPaid": 5656.89
                  }
              },
              "customerAddedStateBenefits": {
                  "incapacityBenefit": [
                    {
                      "benefitId": "a1e8057e-fbbc-47a8-a8b4-78d9f015c941",
                      "startDate": "2018-07-17",
                      "submittedOn": "2020-11-17T19:23:00Z",
                      "endDate": "2020-09-23",
                      "amount": 45646.78,
                      "taxPaid": 4544.34
                    }
                  ],
                  "statePension": {
                      "benefitId": "a1e8057e-fbbc-47a8-a8b4-78d9f015c943",
                      "startDate": "2018-04-03",
                      "submittedOn": "2020-06-11T10:23:00Z",
                      "endDate": "2020-09-13",
                      "amount": 45642.45,
                      "taxPaid": 6764.34
                  },
                  "statePensionLumpSum": {
                      "benefitId": "a1e8057e-fbbc-47a8-a8b4-78d9f015c956",
                      "startDate": "2019-09-23",
                      "submittedOn": "2020-06-13T05:29:00Z",
                      "endDate": "2020-09-26",
                      "amount": 34322.34,
                      "taxPaid": 4564.45
                  },
                  "employmentSupportAllowance": [
                    {
                      "benefitId": "a1e8057e-fbbc-47a8-a8b4-78d9f015c988",
                      "startDate": "2019-09-11",
                      "submittedOn": "2020-02-10T11:20:00Z",
                      "endDate": "2020-06-13",
                      "amount": 45424.23,
                      "taxPaid": 23232.34
                    }
                  ],
                  "jobSeekersAllowance": [
                    {
                      "benefitId": "a1e8057e-fbbc-47a8-a8b4-78d9f015c990",
                      "startDate": "2019-07-10",
                      "submittedOn": "2020-05-13T14:23:00Z",
                      "endDate": "2020-05-11",
                      "amount": 34343.78,
                      "taxPaid": 3433.56
                    } 
                  ],
                  "bereavementAllowance": {
                      "benefitId": "a1e8057e-fbbc-47a8-a8b4-78d9f015c997",
                      "startDate": "2018-08-12",
                      "submittedOn": "2020-02-13T11:23:00Z",
                      "endDate": "2020-07-13",
                      "amount": 45423.45,
                      "taxPaid": 4543.64
                  },
                  "otherStateBenefits": {
                      "benefitId": "a1e8057e-fbbc-47a8-a8b4-78d9f015c957",
                      "startDate": "2018-01-13",
                      "submittedOn": "2020-09-12T12:23:00Z",
                      "endDate": "2020-08-13",
                      "amount": 63333.33,
                      "taxPaid": 4644.45
                  }
              }
          }
      }
  ],
  "cis": [
    {
        "taxYear": 2023,
        "customerCISDeductions": {
            "totalDeductionAmount": 400,
            "totalCostOfMaterials": 400,
            "totalGrossAmountPaid": 400,
            "cisDeductions": [
              {
                "fromDate": "2021-04-06",
                "toDate": "2022-04-05",
                "contractorName": "Michele Lamy Paving Ltd",
                "employerRef": "111/11111",
                "totalDeductionAmount": 200,
                "totalCostOfMaterials": 200,
                "totalGrossAmountPaid": 200,
                "periodData": [
                  {
                    "deductionFromDate": "2021-04-06",
                    "deductionToDate": "2021-05-05",
                    "deductionAmount": 100,
                    "costOfMaterials": 100,
                    "grossAmountPaid": 100,
                    "submissionDate": "2022-05-11T16:38:57.489Z",
                    "submissionId": "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
                    "source": "customer"
                  }, {
                    "deductionFromDate": "2021-05-06",
                    "deductionToDate": "2021-06-05",
                    "deductionAmount": 100,
                    "costOfMaterials": 100,
                    "grossAmountPaid": 100,
                    "submissionDate": "2022-05-11T16:38:57.489Z",
                    "submissionId": "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
                    "source": "customer"
                  }
                ]
              }, {
                "fromDate": "2021-04-06",
                "toDate": "2022-04-05",
                "contractorName": "Jun Takahashi Window Fitting",
                "employerRef": "222/11111",
                "totalDeductionAmount": 200,
                "totalCostOfMaterials": 200,
                "totalGrossAmountPaid": 200,
                "periodData": [
                  {
                    "deductionFromDate": "2021-04-06",
                    "deductionToDate": "2021-05-05",
                    "deductionAmount": 100,
                    "costOfMaterials": 100,
                    "grossAmountPaid": 100,
                    "submissionDate": "2022-05-11T16:38:57.489Z",
                    "submissionId": "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
                    "source": "customer"
                  }, {
                    "deductionFromDate": "2021-05-06",
                    "deductionToDate": "2021-06-05",
                    "deductionAmount": 100,
                    "costOfMaterials": 100,
                    "grossAmountPaid": 100,
                    "submissionDate": "2022-05-11T16:38:57.489Z",
                    "submissionId": "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
                    "source": "customer"
                  }
                ]
              }
            ]
        },
        "contractorCISDeductions": {
            "totalDeductionAmount": 400,
            "totalCostOfMaterials": 400,
            "totalGrossAmountPaid": 400,
            "cisDeductions": [
              {
                "fromDate": "2021-04-06",
                "toDate": "2022-04-05",
                "contractorName": "Michele Lamy Paving Ltd",
                "employerRef": "111/11111",
                "totalDeductionAmount": 200,
                "totalCostOfMaterials": 200,
                "totalGrossAmountPaid": 200,
                "periodData": [
                  {
                    "deductionFromDate": "2021-04-06",
                    "deductionToDate": "2021-05-05",
                    "deductionAmount": 100,
                    "costOfMaterials": 100,
                    "grossAmountPaid": 100,
                    "submissionDate": "2022-05-11T16:38:57.489Z",
                    "source": "contractor"
                  }, {
                    "deductionFromDate": "2021-05-06",
                    "deductionToDate": "2021-06-05",
                    "deductionAmount": 100,
                    "costOfMaterials": 100,
                    "grossAmountPaid": 100,
                    "submissionDate": "2022-05-11T16:38:57.489Z",
                    "source": "contractor"
                  }
                ]
              }, {
                "fromDate": "2021-04-06",
                "toDate": "2022-04-05",
                "contractorName": "Jun Takahashi Window Fitting",
                "employerRef": "222/11111",
                "totalDeductionAmount": 200,
                "totalCostOfMaterials": 200,
                "totalGrossAmountPaid": 200,
                "periodData": [
                  {
                    "deductionFromDate": "2021-04-06",
                    "deductionToDate": "2021-05-05",
                    "deductionAmount": 100,
                    "costOfMaterials": 100,
                    "grossAmountPaid": 100,
                    "submissionDate": "2022-05-11T16:38:57.489Z",
                    "source": "contractor"
                  }, {
                    "deductionFromDate": "2021-05-06",
                    "deductionToDate": "2021-06-05",
                    "deductionAmount": 100,
                    "costOfMaterials": 100,
                    "grossAmountPaid": 100,
                    "submissionDate": "2022-05-11T16:38:57.489Z",
                    "source": "contractor"
                  }
                ]
              }
            ]
        }
    }
  ]
}
```
</details>

## Dividends, Interest, GiftAid
These journeys are a part of the Personal-Income-Tax-Submission-Frontend repository see its [readMe](https://github.com/hmrc/personal-income-tax-submission-frontend/blob/main/README.md) for more.

## Employments
This journey is a part of the Income-Tax-Employment-Frontend repository see its [readMe](https://github.com/hmrc/income-tax-employment-frontend/blob/main/README.md) for more.

## CIS
This journey is a part of the Income-Tax-Cis-Frontend repository see its [readMe](https://github.com/hmrc/income-tax-cis-frontend/blob/main/README.md) for more.

## Pensions
This journey is a part of the Income-Tax-Pensions-Frontend repository see its [readMe](https://github.com/hmrc/income-tax-pensions-frontend/blob/main/README.md) for more.

## Crystallisation
It must be the end of the tax year for a user to submit for crystallisation.

The user also requires the following extra enrollment:

| IR-SA | UTR | Identifier Value, e.g. 1234567890 |
| --- | --- | --- |

### Crystallisation in Staging
Currently, the crystallisation journey and tax account in staging can only be accessed using the following:

| Nino | MTDITID |
| --- | --- |
| AA888888A | XAIT00000888888 |

## Ninos with stub data for Income Tax Submission Frontend

### In-Year
| Nino | Income Tax Submission Frontend data | Source |
| --- | --- | --- |
| AA133742A | PAYE data | HMRC-Held |
| AA000003A | Interest & Dividends data |

### End of Year
| Nino | Income Tax Submission Frontend data | Source |
| --- | --- | --- |
| BB444444A | PAYE data |HMRC-Held, Customer |
| AA123459A | All employments |HMRC-Held, Customer |

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
