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

package views.helpers

import itUtils.ViewTest
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.templates.helpers.Heading

class HeadingISpec extends ViewTest {

  lazy val heading: Heading = app.injector.instanceOf[Heading]

  "Heading template" should {

    "show the caption before the heading" in {
      lazy val view = heading(heading = "heading", caption = "caption")

      implicit def document: Document = Jsoup.parse(view.body)

      val headingAndCaption = elementText(s"h1.govuk-heading-xl").trim

      headingAndCaption.startsWith("caption") shouldBe true
      headingAndCaption.endsWith("heading") shouldBe true
    }

    "show only the heading when no caption is provided" in {
      lazy val view = heading(heading = "heading")

      implicit def document: Document = Jsoup.parse(view.body)

      val headingAndCaption = elementText(s"h1.govuk-heading-xl").trim

      headingAndCaption shouldBe "heading"
    }

    "add the extra classes in the h1" in {
      lazy val view = heading(heading = "heading", caption = "caption", extraClasses = "extra-class")

      implicit def document: Document = Jsoup.parse(view.body)

      val headingAndCaption = document.select(s"h1.govuk-heading-xl")

      headingAndCaption.hasClass("extra-class") shouldBe true
    }
  }
}
