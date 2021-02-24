/*
 * Copyright 2020 HM Revenue & Customs
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

import config.AppConfig
import controllers.predicates.AuthorisedAction
import helpers.WireMockHelper
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.{Application, Environment, Mode}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.MessagesControllerComponents
import services.AuthService
import uk.gov.hmrc.auth.core.ConfidenceLevel
import uk.gov.hmrc.http.HeaderCarrier
import views.html.authErrorPages.AgentAuthErrorPageView

import scala.concurrent.{Await, Awaitable, Future}
import scala.concurrent.duration.Duration

trait IntegrationTest extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with WireMockHelper with BeforeAndAfterAll{

  implicit val emptyHeaderCarrier: HeaderCarrier = HeaderCarrier()

  def await[T](awaitable: Awaitable[T]): T = Await.result(awaitable, Duration.Inf)

  def config: Map[String, String] = Map(
    "auditing.enabled" -> "false",
    "play.filters.csrf.header.bypassHeaders.Csrf-Token" -> "nocheck",
    "microservice.services.income-tax-submission.host" -> wiremockHost,
    "microservice.services.income-tax-submission.port" -> wiremockPort.toString,
    "microservice.services.auth.host" -> wiremockHost,
    "microservice.services.auth.port" -> wiremockPort.toString
  )

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .in(Environment.simple(mode = Mode.Dev))
    .configure(config)
    .build

  lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  override def beforeAll(): Unit = {
    super.beforeAll()
    startWiremock()
  }

  override def afterAll(): Unit = {
    stopWiremock()
    super.afterAll()
  }

  lazy val agentAuthErrorPage: AgentAuthErrorPageView = app.injector.instanceOf[AgentAuthErrorPageView]
  lazy val mcc: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]

  val defaultAcceptedConfidenceLevels = Seq(
    ConfidenceLevel.L200,
    ConfidenceLevel.L300,
    ConfidenceLevel.L500
  )

  def authService(stubbedRetrieval: Future[_], acceptedConfidenceLevel: Seq[ConfidenceLevel]) = new AuthService(
    new MockAuthConnector(stubbedRetrieval, acceptedConfidenceLevel)
  )

  def authAction(stubbedRetrieval: Future[_], acceptedConfidenceLevel: Seq[ConfidenceLevel] = Seq.empty[ConfidenceLevel]) = new AuthorisedAction(
    appConfig,
    agentAuthErrorPage
  )(
    authService(stubbedRetrieval, if(acceptedConfidenceLevel.nonEmpty) {
      acceptedConfidenceLevel
    } else {
      defaultAcceptedConfidenceLevels
    }),
    mcc
  )

}
