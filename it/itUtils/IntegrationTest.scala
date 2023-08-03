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

package itUtils

import akka.actor.ActorSystem
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import common.SessionValues
import config.AppConfig
import controllers.predicates.{AuthorisedAction, InYearAction}
import helpers.{PlaySessionCookieBaker, WireMockHelper}
import models._
import models.cis.{AllCISDeductions, CISDeductions, CISSource, PeriodData}
import models.employment._
import models.gains.{CapitalRedemptionModel, ForeignModel, InsurancePoliciesModel, LifeAnnuityModel, LifeInsuranceModel, VoidedIsaModel}
import models.property.AllPropertyData
import models.statebenefits.{AllStateBenefitsData, CustomerAddedStateBenefit, CustomerAddedStateBenefitsData, StateBenefitsData}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, OptionValues}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.http.{HeaderNames, Writeable}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc._
import play.api.test.Helpers.OK
import play.api.test.{DefaultAwaitTimeout, FakeRequest, Helpers}
import play.api.{Application, Environment, Mode}
import services.AuthService
import testModels.PensionsModels.allPensionsModel
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.auth.core.syntax.retrieved.authSyntaxForRetrieved
import uk.gov.hmrc.auth.core.{AffinityGroup, ConfidenceLevel}
import uk.gov.hmrc.http.{HeaderCarrier, SessionId, SessionKeys}
import views.html.authErrorPages.AgentAuthErrorPageView

import java.time.{Instant, LocalDate}
import java.util.UUID
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Awaitable, ExecutionContext, Future}

trait IntegrationTest extends AnyWordSpecLike with Matchers with GuiceOneServerPerSuite with WireMockHelper with BeforeAndAfterAll
  with BeforeAndAfterEach with DefaultAwaitTimeout with OptionValues {

  private val dateNow: LocalDate = LocalDate.now()
  private val taxYearCutoffDate: LocalDate = LocalDate.parse(s"${dateNow.getYear}-04-05")

  val taxYear: Int = if (dateNow.isAfter(taxYearCutoffDate)) LocalDate.now().getYear + 1 else LocalDate.now().getYear
  val taxYearEOY: Int = taxYear - 1
  val taxYearEndOfYearMinusOne: Int = taxYearEOY - 1

  val nino = "AA123456A"
  val mtditid = "1234567890"
  val affinityGroup = "Individual"

  val vcPtaNavBarOrigin = "?origin=PTA"

  val validTaxYearList: Seq[Int] = Seq(taxYearEndOfYearMinusOne, taxYearEOY, taxYear)
  val singleValidTaxYear: Seq[Int] = Seq(taxYearEndOfYearMinusOne)

  implicit lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  val inYearAction = new InYearAction

  implicit val actorSystem: ActorSystem = ActorSystem()

  val startUrl = s"http://localhost:$port/update-and-submit-income-tax-return"

  implicit def wsClient: WSClient = app.injector.instanceOf[WSClient]

  def await[T](awaitable: Awaitable[T]): T = Await.result(awaitable, Duration.Inf)

  //scalastyle:off
  def config(useEncryption: Boolean = true,
             invalidEncryptionKey: Boolean = false,
             dividendsEnabled: Boolean = true,
             interestEnabled: Boolean = true,
             interestSavingsEnabled: Boolean = false,
             giftAidEnabled: Boolean = true,
             studentLoansEnabled: Boolean = true,
             employmentEnabled: Boolean = true,
             employmentEOYEnabled: Boolean = true,
             gainsEnabled: Boolean = true,
             stockDividendsEnabled: Boolean = true,
             cisEnabled: Boolean = true,
             pensionsEnabled: Boolean = true,
             stateBenefitsEnabled: Boolean = true,
             crystallisationEnabled: Boolean = true,
             taxYearErrorFeatureSwitch: Boolean = false,
             tailoringEnabled: Boolean = false
            ): Map[String, String] = Map(
    "defaultTaxYear" -> taxYear.toString,
    "auditing.enabled" -> "false",
    "taxYearErrorFeatureSwitch" -> taxYearErrorFeatureSwitch.toString,
    "play.filters.csrf.header.bypassHeaders.Csrf-Token" -> "nocheck",
    "microservice.services.income-tax-submission.url" -> s"http://$wiremockHost:$wiremockPort",
    "microservice.services.income-tax-calculation.url" -> s"http://$wiremockHost:$wiremockPort",
    "microservice.services.income-tax-nrs-proxy.url" -> s"http://$wiremockHost:$wiremockPort",
    "microservice.services.auth.host" -> wiremockHost,
    "microservice.services.auth.port" -> wiremockPort.toString,
    "microservice.services.auth-login-api.url" -> s"http://$wiremockHost:$wiremockPort",
    "feature-switch.dividendsEnabled" -> dividendsEnabled.toString,
    "feature-switch.interestEnabled" -> interestEnabled.toString,
    "feature-switch.interestSavingsEnabled" -> interestSavingsEnabled.toString,
    "feature-switch.giftAidEnabled" -> giftAidEnabled.toString,
    "feature-switch.studentLoansEnabled" -> studentLoansEnabled.toString,
    "feature-switch.employmentEnabled" -> employmentEnabled.toString,
    "feature-switch.gainsEnabled" -> gainsEnabled.toString,
    "feature-switch.gainsReleased" -> "true",
    "feature-switch.stockDividendsEnabled" -> stockDividendsEnabled.toString,
    "feature-switch.stockDividendsReleased" -> "true",
    "feature-switch.employmentEOYEnabled" -> employmentEOYEnabled.toString,
    "feature-switch.cisEnabled" -> cisEnabled.toString,
    "feature-switch.pensionsEnabled" -> pensionsEnabled.toString,
    "feature-switch.stateBenefitsEnabled" -> stateBenefitsEnabled.toString,
    "feature-switch.crystallisationEnabled" -> crystallisationEnabled.toString,
    "feature-switch.tailoringEnabled" -> tailoringEnabled.toString,
    "metrics.enabled" -> "false",
    "play.http.router" -> "testOnlyDoNotUseInAppConf.Routes",
    "useEncryption" -> useEncryption.toString,
    "mongodb.encryption.key" -> (if (invalidEncryptionKey) "key" else "QmFyMTIzNDVCYXIxMjM0NQ==")
  )

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .in(Environment.simple(mode = Mode.Dev))
    .configure(config())
    .build

  def customApp(useEncryption: Boolean = true,
                invalidEncryptionKey: Boolean = false,
                dividendsEnabled: Boolean = true,
                interestEnabled: Boolean = true,
                interestSavingsEnabled: Boolean = false,
                giftAidEnabled: Boolean = true,
                studentLoansEnabled: Boolean = true,
                employmentEnabled: Boolean = true,
                employmentEOYEnabled: Boolean = true,
                gainsEnabled: Boolean = true,
                cisEnabled: Boolean = true,
                pensionsEnabled: Boolean = true,
                propertyEnabled: Boolean = true,
                stateBenefitsEnabled: Boolean = true,
                stockDividendsEnabled: Boolean = true,
                crystallisationEnabled: Boolean = true,
                taxYearErrorFeatureSwitch: Boolean = false,
                tailoringEnabled: Boolean = false
               ): Application = new GuiceApplicationBuilder()
    .in(Environment.simple(mode = Mode.Dev))
    .configure(
      config(
        useEncryption,
        invalidEncryptionKey,
        dividendsEnabled,
        interestEnabled,
        interestSavingsEnabled,
        giftAidEnabled,
        studentLoansEnabled,
        employmentEnabled,
        employmentEOYEnabled,
        gainsEnabled,
        cisEnabled,
        pensionsEnabled,
        stateBenefitsEnabled,
        stockDividendsEnabled,
        crystallisationEnabled,
        taxYearErrorFeatureSwitch,
        tailoringEnabled
      )
    ).build

  //scalastyle:on

  override protected def beforeEach(): Unit = {
    resetWiremock()
  }

  override def beforeAll(): Unit = {
    super.beforeAll()
    startWiremock()
  }

  override def afterAll(): Unit = {
    stopWiremock()
    super.afterAll()
  }

  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

  val sessionId: String = "eb3158c2-0aff-4ce8-8d1b-f2208ace52fe"
  implicit lazy val user: User[AnyContent] = new User[AnyContent](mtditid, None, nino, sessionId)(FakeRequest().withHeaders("X-Session-ID" -> sessionId))
  val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withHeaders("X-Session-ID" -> sessionId)
  val fallBackSessionIdFakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withHeaders("sessionId" -> sessionId)
  val fakeRequestAgent: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
    .withSession("ClientMTDID" -> "1234567890", "ClientNino" -> "AA123456A").withHeaders("X-Session-ID" -> sessionId)
  val fakeRequestAgentNoMtditid: FakeRequest[AnyContentAsEmpty.type] = fakeRequest.withSession("ClientNino" -> "AA123456A")
  val fakeRequestAgentNoNino: FakeRequest[AnyContentAsEmpty.type] = fakeRequest.withSession("ClientMTDID" -> "1234567890")

  implicit val headerCarrierWithSession: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId(sessionId)))
  val emptyHeaderCarrier: HeaderCarrier = HeaderCarrier()

  lazy val agentAuthErrorPage: AgentAuthErrorPageView = app.injector.instanceOf[AgentAuthErrorPageView]
  lazy val mcc: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]

  val defaultAcceptedConfidenceLevels: Seq[ConfidenceLevel] = Seq(
    ConfidenceLevel.L250,
    ConfidenceLevel.L500
  )

  def redirectUrl(awaitable: Future[Result]): String = await(awaitable).header.headers("Location")

  def route[T](app: Application, request: Request[T], isWelsh: Boolean = false)(implicit w: Writeable[T]): Option[Future[Result]] = {
    val newHeaders = request.headers.add((HeaderNames.ACCEPT_LANGUAGE, if (isWelsh) "cy" else "en"))
    val requestWithHeaders = request.withHeaders(newHeaders)

    Helpers.route(app, requestWithHeaders)
  }

  def authService(stubbedRetrieval: Future[_]): AuthService = new AuthService(
    new MockAuthConnector(stubbedRetrieval)
  )

  def authAction(stubbedRetrieval: Future[_]): AuthorisedAction = new AuthorisedAction(
    appConfig,
    agentAuthErrorPage
  )(authService(stubbedRetrieval), mcc)

  def stubIncomeSources(incomeSources: IncomeSourcesModel, status: Int = OK): StubMapping = {
    stubGet(s"/income-tax-submission-service/income-tax/nino/AA123456A/sources\\?taxYear=$taxYear", status, Json.toJson(incomeSources).toString())
  }

  //noinspection ScalaStyle
  def mockIVCredentials(affinityGroup: AffinityGroup, confidenceLevel: Int): Future[Some[AffinityGroup] ~ ConfidenceLevel] = {
    val confidenceLevelResponse = confidenceLevel match {
      case 500 => ConfidenceLevel.L500
      case 250 => ConfidenceLevel.L250
      case 50 => ConfidenceLevel.L50
    }

    Future.successful(Some(affinityGroup) and confidenceLevelResponse)
  }

  def bodyOf(awaitable: Future[Result]): String = {
    val awaited = await(awaitable)
    await(awaited.body.consumeData.map(_.utf8String)(ExecutionContext.Implicits.global))
  }

  lazy val incomeSourcesModel: IncomeSourcesModel = IncomeSourcesModel(
    dividends = dividendsModel,
    interest = interestsModel,
    giftAid = Some(giftAidModel),
    employment = Some(employmentsModel),
    cis = Some(allCISDeductions),
    gains = insurancePoliciesModel,
    pensions = Some(allPensionsModel),
    stateBenefits = Some(allStateBenefitsData),
    interestSavings = Some(savingsInterestModel),
    stockDividends = Some(aStockDividends),
    property = Some(allProperty)
  )

  lazy val dividendsModel: Option[DividendsModel] = Some(DividendsModel(Some(100.00), Some(100.00)))
  lazy val interestsModel: Option[Seq[InterestModel]] = Some(Seq(InterestModel("TestName", "TestSource", Some(100.00), Some(100.00))))
  lazy val savingsInterestModel: SavingsIncomeDataModel = SavingsIncomeDataModel(
    submittedOn = Some("2020-02-04T05:01:01Z"),
    securities = Some(SecuritiesModel(Some(800.67), 7455.99, Some(6123.2))),
    foreignInterest = Some(Seq(ForeignInterestModel("BES", Some(1232.56), Some(3422.22), Some(5622.67), Some(true), 2821.92)
    ))
  )
  lazy val employmentsModel: AllEmploymentData = AllEmploymentData(
    hmrcEmploymentData = Seq(
      HmrcEmploymentSource(
        employmentId = "001",
        employerName = "maggie",
        employerRef = Some("223/AB12399"),
        payrollId = Some("123456789999"),
        startDate = Some("2019-04-21"),
        cessationDate = Some("2020-03-11"),
        dateIgnored = None,
        submittedOn = Some("2020-01-04T05:01:01Z"),
        hmrcEmploymentFinancialData = Some(
          EmploymentFinancialData(
            employmentData = Some(EmploymentData(
              submittedOn = "2020-02-12",
              employmentSequenceNumber = Some("123456789999"),
              companyDirector = Some(true),
              closeCompany = Some(false),
              directorshipCeasedDate = Some("2020-02-12"),
              disguisedRemuneration = Some(false),
              pay = Some(Pay(Some(34234.15), Some(6782.92), Some("CALENDAR MONTHLY"), Some("2020-04-23"), Some(32), Some(2)))
            )),
            None
          )
        ),
        customerEmploymentFinancialData = None
      )
    ),
    hmrcExpenses = None,
    customerEmploymentData = Seq(),
    customerExpenses = None
  )
  val cisSource: CISSource = CISSource(
    Some(400), Some(400), Some(400), Seq(
      CISDeductions(
        s"${2020 - 1}-04-06",
        s"${2020}-04-05",
        Some("Contractor 1"),
        "111/11111",
        Some(200.00),
        Some(200.00),
        Some(200.00),
        Seq(
          PeriodData(
            s"${2020 - 1}-04-06",
            s"${2020 - 1}-05-05",
            Some(100.00),
            Some(100.00),
            Some(100.00),
            "2022-05-11T16:38:57.489Z",
            None,
            "contractor"
          ), PeriodData(
            s"${2020 - 1}-05-06",
            s"${2020 - 1}-06-05",
            Some(100.00),
            Some(100.00),
            Some(100.00),
            "2022-05-11T16:38:57.489Z",
            None,
            "contractor"
          )
        )
      ), CISDeductions(
        s"${2020 - 1}-04-06",
        s"${2020}-04-05",
        Some("Contractor 2"),
        "222/11111",
        Some(200.00),
        Some(200.00),
        Some(200.00),
        Seq(
          PeriodData(
            s"${2020 - 1}-04-06",
            s"${2020 - 1}-05-05",
            Some(100.00),
            Some(100.00),
            Some(100.00),
            "2022-05-11T16:38:57.489Z",
            None,
            "contractor"
          ), PeriodData(
            s"${2020 - 1}-05-06",
            s"${2020 - 1}-06-05",
            Some(100.00),
            Some(100.00),
            Some(100.00),
            "2022-05-11T16:38:57.489Z",
            None,
            "contractor"
          )
        )
      )
    ))

  private val allCISDeductions = AllCISDeductions(Some(cisSource), Some(cisSource))

  lazy val allStateBenefitsData: AllStateBenefitsData = AllStateBenefitsData(
    stateBenefitsData = Some(aStateBenefitsData),
    customerAddedStateBenefitsData = Some(aCustomerAddedStateBenefitsData)
  )

  lazy val aStateBenefitsData: StateBenefitsData = StateBenefitsData(
    incapacityBenefits = Some(Set(aStateBenefit)),
    statePension = Some(aStateBenefit),
    statePensionLumpSum = Some(aStateBenefit),
    employmentSupportAllowances = Some(Set(aStateBenefit)),
    jobSeekersAllowances = Some(Set(aStateBenefit)),
    bereavementAllowance = Some(aStateBenefit),
    other = Some(aStateBenefit)
  )

  lazy val aStateBenefit: models.statebenefits.StateBenefit = models.statebenefits.StateBenefit(
    benefitId = UUID.fromString("a1e8057e-fbbc-47a8-a8b4-78d9f015c936"),
    startDate = LocalDate.parse(s"${taxYearEOY - 1}-04-23"),
    endDate = Some(LocalDate.parse(s"$taxYearEOY-08-13")),
    dateIgnored = Some(Instant.parse(s"${taxYearEOY - 1}-07-08T05:23:00Z")),
    submittedOn = Some(Instant.parse(s"$taxYearEOY-03-13T19:23:00Z")),
    amount = Some(300.00),
    taxPaid = Some(400.00)
  )

  lazy val aCustomerAddedStateBenefitsData: CustomerAddedStateBenefitsData = CustomerAddedStateBenefitsData(
    incapacityBenefits = Some(Set(aCustomerAddedStateBenefit)),
    statePensions = Some(Set(aCustomerAddedStateBenefit)),
    statePensionLumpSums = Some(Set(aCustomerAddedStateBenefit)),
    employmentSupportAllowances = Some(Set(aCustomerAddedStateBenefit)),
    jobSeekersAllowances = Some(Set(aCustomerAddedStateBenefit)),
    bereavementAllowances = Some(Set(aCustomerAddedStateBenefit)),
    otherStateBenefits = Some(Set(aCustomerAddedStateBenefit))
  )

  lazy val aCustomerAddedStateBenefit: CustomerAddedStateBenefit = CustomerAddedStateBenefit(
    benefitId = UUID.fromString("a1e8057e-fbbc-47a8-a8b4-78d9f015c941"),
    startDate = LocalDate.parse(s"${taxYearEOY - 1}-04-23"),
    endDate = Some(LocalDate.parse(s"$taxYearEOY-08-13")),
    submittedOn = Some(Instant.parse(s"$taxYearEOY-03-13T19:23:00Z")),
    amount = Some(100.00),
    taxPaid = Some(200.00)
  )

  val giftAidPaymentsModel: Option[GiftAidPaymentsModel] = Some(GiftAidPaymentsModel(
    nonUkCharitiesCharityNames = Some(List("non uk charity name", "non uk charity name 2")),
    currentYear = Some(1234.56),
    oneOffCurrentYear = Some(1234.56),
    currentYearTreatedAsPreviousYear = Some(1234.56),
    nextYearTreatedAsCurrentYear = Some(1234.56),
    nonUkCharities = Some(1234.56),
  ))

  val giftsModel: Option[GiftsModel] = Some(GiftsModel(
    investmentsNonUkCharitiesCharityNames = Some(List("charity 1", "charity 2")),
    landAndBuildings = Some(10.21),
    sharesOrSecurities = Some(10.21),
    investmentsNonUkCharities = Some(1234.56)
  ))

  val giftAidModel: GiftAidModel = GiftAidModel(
    giftAidPaymentsModel,
    giftsModel
  )

  val validLifeInsuranceModel: LifeInsuranceModel = LifeInsuranceModel(
    customerReference = Some("RefNo13254687"),
    event = Some("Life"),
    gainAmount = 123.45,
    taxPaid = Some(true),
    yearsHeld = Some(4),
    yearsHeldSinceLastGain = Some(3),
    deficiencyRelief = Some(123.45)
  )

  val validCapitalRedemptionModel: CapitalRedemptionModel = CapitalRedemptionModel(
    customerReference = Some("RefNo13254687"),
    event = Some("Capital"),
    gainAmount = 123.45,
    taxPaid = Some(true),
    yearsHeld = Some(3),
    yearsHeldSinceLastGain = Some(2),
    deficiencyRelief = Some(0)
  )

  val validLifeAnnuityModel: LifeAnnuityModel = LifeAnnuityModel(
    customerReference = Some("RefNo13254687"),
    event = Some("Life"),
    gainAmount = 0,
    taxPaid = Some(true),
    yearsHeld = Some(2),
    yearsHeldSinceLastGain = Some(22),
    deficiencyRelief = Some(123.45)
  )

  val validVoidedIsaModel: VoidedIsaModel = VoidedIsaModel(
    customerReference = Some("RefNo13254687"),
    event = Some("isa"),
    gainAmount = 123.45,
    taxPaidAmount = Some(123.45),
    yearsHeld = Some(5),
    yearsHeldSinceLastGain = Some(6)
  )

  val validForeignModel: ForeignModel = ForeignModel(
    customerReference = Some("RefNo13254687"),
    gainAmount = 123.45,
    taxPaidAmount = Some(123.45),
    yearsHeld = Some(3)
  )

  lazy val insurancePoliciesModel: Option[InsurancePoliciesModel] = Some(InsurancePoliciesModel("2020-01-04T05:01:01Z",
    Seq(validLifeInsuranceModel), Some(Seq(validCapitalRedemptionModel)) , Some(Seq(validLifeAnnuityModel)), Some(Seq(validVoidedIsaModel))
    , Some(Seq(validForeignModel))))

  val foreignInterest: Seq[ForeignInterestModel] = Seq(ForeignInterestModel(
    "BES",
    Some(1232.56),
    Some(3422.22),
    Some(5622.67),
    Some(true),
    2821.92)
  )

  val dividend: Dividend = Dividend(customerReference = Some("reference"), grossAmount = Some(123.45))

  val aStockDividends: StockDividendsModel = StockDividendsModel(
    submittedOn = Some("2020-01-04T05:01:01Z"),
    foreignDividend = Some(foreignInterest),
    dividendIncomeReceivedWhilstAbroad = Some(foreignInterest),
    stockDividend = Some(dividend),
    redeemableShares = Some(dividend),
    bonusIssuesOfSecurities = Some(dividend),
    closeCompanyLoansWrittenOff = Some(dividend)
  )

  val allProperty: AllPropertyData = AllPropertyData("text")

  def playSessionCookies(taxYear: Int, validTaxYears: Seq[Int]): String = PlaySessionCookieBaker.bakeSessionCookie(Map(
    SessionValues.TAX_YEAR -> taxYear.toString,
    SessionValues.VALID_TAX_YEARS -> validTaxYears.mkString(","),
    SessionKeys.sessionId -> sessionId,
    SessionValues.CLIENT_NINO -> "AA123456A",
    SessionValues.CLIENT_MTDITID -> "1234567890",
    SessionKeys.authToken -> "mock-bearer-token"
  ))
}
