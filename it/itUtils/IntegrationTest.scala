/*
 * Copyright 2021 HM Revenue & Customs
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

import common.SessionValues
import config.AppConfig
import controllers.predicates.AuthorisedAction
import helpers.{PlaySessionCookieBaker, WireMockHelper}
import models._
import models.employment.{AllEmploymentData, EmploymentData, EmploymentSource, Pay}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, OptionValues}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.http.{HeaderNames, Writeable}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSClient
import play.api.mvc.{MessagesControllerComponents, Request, Result}
import play.api.test.{DefaultAwaitTimeout, FakeRequest, Helpers}
import play.api.{Application, Environment, Mode}
import services.AuthService
import uk.gov.hmrc.auth.core.ConfidenceLevel
import uk.gov.hmrc.http.{HeaderCarrier, SessionKeys}
import views.html.authErrorPages.AgentAuthErrorPageView

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Awaitable, Future}

trait IntegrationTest extends AnyWordSpecLike with Matchers with GuiceOneServerPerSuite with WireMockHelper with BeforeAndAfterAll
  with BeforeAndAfterEach with DefaultAwaitTimeout with OptionValues {

  implicit val emptyHeaderCarrier: HeaderCarrier = HeaderCarrier()

  val startUrl = s"http://localhost:$port/income-through-software/return"

  lazy val wsClient: WSClient = app.injector.instanceOf[WSClient]

  def await[T](awaitable: Awaitable[T]): T = Await.result(awaitable, Duration.Inf)

  def config: Map[String, String] = Map(
    "auditing.enabled" -> "false",
    "play.filters.csrf.header.bypassHeaders.Csrf-Token" -> "nocheck",
    "microservice.services.income-tax-submission.url" -> s"http://$wiremockHost:$wiremockPort",
    "microservice.services.income-tax-calculation.url" -> s"http://$wiremockHost:$wiremockPort",
    "microservice.services.auth.host" -> wiremockHost,
    "microservice.services.auth.port" -> wiremockPort.toString,
    "feature-switch.dividendsEnabled" -> "true",
    "feature-switch.interestEnabled" -> "true",
    "feature-switch.giftAidEnabled" -> "true",
    "feature-switch.employmentEnabled" -> "true",
    "metrics.enabled" -> "false"
  )

  def sourcesTurnedOffConfig: Map[String, String] = Map(
    "auditing.enabled" -> "false",
    "play.filters.csrf.header.bypassHeaders.Csrf-Token" -> "nocheck",
    "microservice.services.income-tax-submission.url" -> s"http://$wiremockHost:$wiremockPort",
    "microservice.services.income-tax-calculation.url" -> s"http://$wiremockHost:$wiremockPort",
    "microservice.services.auth.host" -> wiremockHost,
    "microservice.services.auth.port" -> wiremockPort.toString,
    "feature-switch.dividendsEnabled" -> "false",
    "feature-switch.interestEnabled" -> "false",
    "feature-switch.giftAidEnabled" -> "false",
    "feature-switch.employmentEnabled" -> "false",
    "metrics.enabled" -> "false"
  )

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .in(Environment.simple(mode = Mode.Dev))
    .configure(config)
    .build

  lazy val appWithSourcesTurnedOff: Application = new GuiceApplicationBuilder()
    .in(Environment.simple(mode = Mode.Dev))
    .configure(sourcesTurnedOffConfig)
    .build

  lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

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

  val sessionId: String = "eb3158c2-0aff-4ce8-8d1b-f2208ace52fe"
  val fakeRequest = FakeRequest().withHeaders("X-Session-ID" -> sessionId)

  lazy val agentAuthErrorPage: AgentAuthErrorPageView = app.injector.instanceOf[AgentAuthErrorPageView]
  lazy val mcc: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]

  val defaultAcceptedConfidenceLevels = Seq(
    ConfidenceLevel.L200,
    ConfidenceLevel.L500
  )

  def route[T](app: Application, request: Request[T], isWelsh: Boolean = false)(implicit w: Writeable[T]): Option[Future[Result]] = {
    val newHeaders = request.headers.add((HeaderNames.ACCEPT_LANGUAGE, if(isWelsh) "cy" else "en"))
    val requestWithHeaders = request.withHeaders(newHeaders)

    Helpers.route(app, requestWithHeaders)
  }

  def authService(stubbedRetrieval: Future[_]): AuthService = new AuthService(
    new MockAuthConnector(stubbedRetrieval)
  )

  def authAction(stubbedRetrieval: Future[_]): AuthorisedAction = new AuthorisedAction(
    appConfig,
    agentAuthErrorPage
  )(
    authService(stubbedRetrieval),
    mcc
  )

  lazy val dividendsModel:Option[DividendsModel] = Some(DividendsModel(Some(100.00), Some(100.00)))
  lazy val interestsModel:Option[Seq[InterestModel]] = Some(Seq(InterestModel("TestName", "TestSource", Some(100.00), Some(100.00))))
  lazy val employmentsModel: AllEmploymentData = AllEmploymentData(
    hmrcEmploymentData = Seq(
      EmploymentSource(
        employmentId = "001",
        employerName = "maggie",
        employerRef = Some("223/AB12399"),
        payrollId = Some("123456789999"),
        startDate = Some("2019-04-21"),
        cessationDate = Some("2020-03-11"),
        dateIgnored = Some("2020-04-04T01:01:01Z"),
        submittedOn = Some("2020-01-04T05:01:01Z"),
        employmentData = Some(EmploymentData(
          submittedOn = ("2020-02-12"),
          employmentSequenceNumber = Some("123456789999"),
          companyDirector = Some(true),
          closeCompany = Some(false),
          directorshipCeasedDate = Some("2020-02-12"),
          occPen = Some(false),
          disguisedRemuneration = Some(false),
          pay = Some(Pay(Some(34234.15), Some(6782.92), Some(67676), Some("CALENDAR MONTHLY"), Some("2020-04-23"), Some(32), Some(2)))
        )),
        None
      )
    ),
    hmrcExpenses = None,
    customerEmploymentData = Seq(),
    customerExpenses = None
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

  def playSessionCookies(taxYear: Int): String = PlaySessionCookieBaker.bakeSessionCookie(Map(
    SessionValues.TAX_YEAR -> taxYear.toString,
    SessionKeys.sessionId -> sessionId,
    SessionValues.CLIENT_NINO -> "AA123456A",
    SessionValues.CLIENT_MTDITID -> "1234567890"
  ))
}
