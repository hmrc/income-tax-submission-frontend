
# income-tax-submission-fronted
This is where users can view the overall state of their income tax submission, start submitting or append an income source,
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
| Dividends | Local, QA, Staging, Production |
| Interest | Local, QA, Staging, Production |
| GiftAid | Local, QA, Staging, Production |
| EmploymentsEnabled | Local, QA, Staging, Production |
| EmploymentsReleased | Local, QA, Staging, Production |
| NRS | Local, QA, Staging, Production |
| Tax Year Error | Production |
| Welsh | Local, QA, Staging |

## Auth Setup - How to enter the service

auth-wizard - http://localhost:9949/auth-login-stub/gg-sign-in

### Example Auth Setup - Individual

| FieldName | Value |
| --- | --- |
| Redirect url        | http://localhost:9302/update-and-submit-income-tax-return/2022/start |
| Credential Strength | strong      |
| Confidence Level    | 200         |
| Affinity Group      | Individual  |
| Nino                | AA123456A   |
| Enrolment Key 1     | HMRC-MTD-IT |
| Identifier Name 1   | MTDITID     |
| Identifier Value 1  | 1234567890  |

### Example Auth Setup - Agent

if running locally outside service manager ensure service is ran including testOnly Routes:

    sbt run -Dapplication.router=testOnlyDoNotUseInAppConf.Routes

| FieldName | Value |
| --- | --- |
| Redirect url        | /test-only/2022/additional-parameters?ClientNino=AA123457A&ClientMTDID=1234567890 |
| Credential Strength | weak                                                                              |
| Confidence Level    | 200                                                                               |
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
  }
}
```
</details>

### Dividends, Interest, GiftAid
These journeys are a part of the Personal-Income-Tax-Submission-Frontend repository see its readMe for more.

### Employments
This journey is a part of the Income-Tax-Employment-Frontend repository see its readMe for more.

## Crystallisation
It must be end of year for a user to submit for crystallisation.

The user also requires the following extra enrollment:

| IR-SA | UTR | Identifier Value, e.g. 1234567890 |
| --- | --- | --- |

### Crystallisation in Staging
Currently, the crystallisation journey and tax account in staging can only be accessed using the following:

| Nino | MTDITID |
| --- | --- |
| AA888888A | XAIT00000888888 |

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
