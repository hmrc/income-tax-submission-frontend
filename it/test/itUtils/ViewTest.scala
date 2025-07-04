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

package itUtils

import org.jsoup.nodes.{Document, Element}
import org.scalatest.Assertion
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers

trait ViewTest extends IntegrationTest {

  implicit lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit lazy val messages: Messages = messagesApi.preferred(fakeRequest)
  lazy val welshMessages: Messages = messagesApi.preferred(Seq(Lang("cy")))
  implicit lazy val mockMessagesControllerComponents: MessagesControllerComponents = Helpers.stubMessagesControllerComponents()

  val serviceName = "Update and submit an Income Tax Return"
  val govUkExtension = "GOV.UK"

  val serviceNameWelsh = "Diweddaru a chyflwyno Ffurflen Dreth Incwm"

  type IntString = Int => String

  def elementText(selector: String)(implicit document: Document): String = {
    document.select(selector).text()
  }

  def element(selector: String)(implicit document: Document): Element = {
    val elements = document.select(selector)

    if (elements.size() == 0) {
      fail(s"No elements exist with the selector '$selector'")
    }

    elements.first()
  }

  def elementExist(selector: String)(implicit document: Document): Boolean = {
    !document.select(selector).isEmpty
  }

  def titleCheck(title: String, isWelsh: Boolean)(implicit document: Document): Unit = {
    if (isWelsh) {
      s"has a title of $title" in {
        document.title() shouldBe s"$title - $serviceNameWelsh - $govUkExtension"
      }
    } else {
      s"has a title of $title" in {
        document.title() shouldBe s"$title - $serviceName - $govUkExtension"
      }
    }
  }

  def h1Check(header: String, size: String = "xl")(implicit document: Document): Unit = {
    s"have a page heading of '$header'" in {
      val heading = document.select(s"h1.govuk-heading-$size").first.ownText
      val caption = document.select(s"h1 > span.govuk-caption-${size}").text

      s"$heading $caption".trim shouldBe header
    }
  }

  def textOnPageCheck(text: String, selector: String)(implicit document: Document): Unit = {
    s"have text on the screen of '$text'" in {
      document.select(selector).text() shouldBe text
    }
  }

  def formGetLinkCheck(text: String, selector: String)(implicit document: Document): Unit = {
    s"have a form with an GET action of '$text'" in {
      document.select(selector).attr("action") shouldBe text
      document.select(selector).attr("method") shouldBe "GET"
    }
  }

  def formPostLinkCheck(actionUrl: String, selector: String)(implicit document: Document): Unit = {
    s"have a form with an POST action of '$actionUrl'" in {
      document.select(selector).attr("action") shouldBe actionUrl
      document.select(selector).attr("method") shouldBe "POST"
    }
  }

  def buttonCheck(text: String, selector: String, href: Option[String] = None)(implicit document: Document): Unit = {
    s"have a $text button" which {
      s"has the text '$text'" in {
        document.select(selector).text() shouldBe text
      }

      s"has a class of govuk-button" in {
        document.select(selector).attr("class") should include("govuk-button")
      }

      if (href.isDefined) {
        s"has a href to '${href.get}'" in {
          document.select(selector).attr("href") shouldBe href.get
        }
      }
    }
  }

  def linkCheck(text: String, selector: String, href: String)(implicit document: Document): Unit = {
    s"have a $text link" which {
      s"has the text '$text'" in {
        document.select(selector).text() shouldBe text
      }
      s"has a href to '$href'" in {
        document.select(selector).attr("href") shouldBe href
      }
    }
  }

  def assertCaption(text: String, selector: String = ".govuk-caption-l")(implicit document: Document): Assertion = {
    elementText(selector) shouldBe text
  }

  def checkMessagesAreUnique(msgFile: Map[String, String], exclusionKeys: Set[String] = Set()): Unit = {
    val msgKeys = msgFile.keys
      .filter(keys => !exclusionKeys.contains(keys))
      .toSet

    val allMsgVals = msgFile.view.filterKeys(msgKeys)
      .values
      .toList

    val uniqueMsgVals = allMsgVals.distinct

    allMsgVals.diff(uniqueMsgVals) shouldBe List.empty
  }

  def welshToggleCheck(activeLanguage: String)(implicit document: Document): Unit = {
    val otherLanguage = if (activeLanguage == "English") "Welsh" else "English"

    def selector = Map("English" -> 0, "Welsh" -> 1)

    def linkLanguage = Map("English" -> "English", "Welsh" -> "Cymraeg")

    def linkText = Map("English" -> "Change the language to English English",
      "Welsh" -> "Newid yr iaith ir Gymraeg Cymraeg")

    s"have the language toggle already set to $activeLanguage" which {
      s"has the text '$activeLanguage" in {
        document.select(".hmrc-language-select__list-item").get(selector(activeLanguage)).text() shouldBe linkLanguage(activeLanguage)
      }
    }
    s"has a link to change the language to $otherLanguage" which {
      s"has the text '${linkText(otherLanguage)}" in {
        document.select(".hmrc-language-select__list-item").get(selector(otherLanguage)).text() shouldBe linkText(otherLanguage)
      }
      s"has a link to change the language" in {
        document.select(".hmrc-language-select__list-item > a").attr("href") shouldBe
          s"/update-and-submit-income-tax-return/language/${linkLanguage(otherLanguage).toLowerCase}"
      }
    }
  }

}
