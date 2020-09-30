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

package views

import config.FrontendAppConfig
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Messages, MessagesApi}
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.OverviewPageView

class OverviewPageViewSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  val individualHeading = "Your Income Tax Return"
  val agentHeading = "Your client's Income Tax Return"
  val agentTask1Heading = "Tailor your client's return"
  val individualTask1Heading = "Tailor your return"
  val task2Heading = "Provide updates"
  val task3Heading = "View estimates"
  val task4Heading = "Submit return"


  implicit val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]

  implicit lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  implicit lazy val mockMessages: Messages = messagesApi.preferred(FakeRequest())

  val overviewPageView: OverviewPageView = app.injector.instanceOf[OverviewPageView]

  class Setup(isAgent : Boolean) {
    val page : HtmlFormat.Appendable = overviewPageView(isAgent)(FakeRequest(), implicitly, appConfig)
    val document: Document = Jsoup.parse(page.body)
  }

  "Business Start Date" must {
    "have the correct individual heading with caption" in new Setup(false) {
      document.getElementsByTag("h1").text shouldBe individualHeading
      document.select(s"""[class=govuk-caption-xl]""").text shouldBe "2020 to 2021 Income Tax"
    }
    "have the correct agent heading with caption" in new Setup(true) {
      document.getElementsByTag("h1").text shouldBe agentHeading
      document.select(s"""[class=govuk-caption-xl]""").text shouldBe "2020 to 2021 Income Tax"
    }
    "have a task list with individual content" in new Setup(false) {
      val taskList: String = document.select(s"""[class=app-task-list]""").text
      taskList.contains(individualTask1Heading) shouldBe true
      taskList.contains(task2Heading) shouldBe true
      taskList.contains(task3Heading) shouldBe true
      taskList.contains(task4Heading) shouldBe true
    }
    "have a task list with agent content" in new Setup(true) {
      val taskList: String = document.select(s"""[class=app-task-list]""").text
      taskList.contains(agentTask1Heading) shouldBe true
      taskList.contains(task2Heading) shouldBe true
      taskList.contains(task3Heading) shouldBe true
      taskList.contains(task4Heading) shouldBe true
    }
  }
}

