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

package utils

import config.AppConfig
import org.jsoup.nodes.{Document, Element}
import org.scalatest.Assertion
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.MessagesControllerComponents
import play.api.test.{FakeRequest, Helpers}

trait ViewTest extends UnitTest with GuiceOneAppPerSuite {

  implicit lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit lazy val messages: Messages = messagesApi.preferred(FakeRequest())
  implicit lazy val mockMessagesControllerComponents: MessagesControllerComponents = Helpers.stubMessagesControllerComponents()
  implicit lazy val mockConfig: AppConfig = app.injector.instanceOf[AppConfig]

  val serviceName = "Update and submit an Income Tax Return"
  val govUkExtension = "Gov.UK"

  type IntString = Int => String

  def elementText(selector: String)(implicit document: Document): String = {
    document.select(selector).text()
  }

  def element(selector: String)(implicit document: Document): Element = {
    val elements = document.select(selector)

    if(elements.size() == 0) {
      fail(s"No elements exist with the selector '$selector'")
    }

    elements.first()
  }

  def elementExist(selector: String)(implicit document: Document): Boolean = {
    !document.select(selector).isEmpty
  }

  def assertTitle(title: String)(implicit document: Document): Assertion = {
    elementText("title") shouldBe title
  }

  def assertH1(text: String)(implicit document: Document): Assertion = {
    elementText("h1") shouldBe text
  }

  def assertCaption(text: String, selector: String = ".govuk-caption-l")(implicit document: Document): Assertion = {
    elementText(selector) shouldBe text
  }

}
