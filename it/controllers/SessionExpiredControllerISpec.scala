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

package controllers

import itUtils.{IntegrationTest, ViewHelpers}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.http.{HeaderNames, Status}
import play.api.libs.ws.WSResponse

class SessionExpiredControllerISpec extends IntegrationTest with ViewHelpers with Status {

  object ExpectedResults {
    val taxYear = 2022
    val pageTitleText = "For your security, we signed you out"
    val pageHeadingText = "For your security, we signed you out"
    val p1Text = "We did not save your answers."
    val buttonText = "Sign in"
  }

  object Selectors {
    val p1 = "#main-content > div > div > div:nth-child(2) > p:nth-child(1)"
    val continueButtonSelector = "#continue"
  }

  import ExpectedResults._
  import Selectors._

  val errorPageUrl = s"http://localhost:$port/income-through-software/return/timeout"

  "an user calling GET" when {
    "language is set to ENGLISH" should {
      "return a page" which {
        lazy val result: WSResponse = {
          authoriseIndividual()
          await(wsClient.url(errorPageUrl).get())
        }

        implicit def document: () => Document = () => Jsoup.parse(result.body)

        "returns status of OK(200)" in {
          result.status shouldBe OK
        }

        titleCheck(pageTitleText)
        welshToggleCheck("English")
        h1Check(pageHeadingText, "xl")
        textOnPageCheck(p1Text, p1)
        buttonCheck(buttonText, continueButtonSelector)
      }
    }

    "language is set to WELSH" should {
      "return a page" which {
        lazy val result: WSResponse = {
          authoriseIndividual()
          await(wsClient.url(errorPageUrl).withHttpHeaders(HeaderNames.ACCEPT_LANGUAGE -> "cy").get())
        }

        implicit def document: () => Document = () => Jsoup.parse(result.body)

        "returns status of OK(200)" in {
          result.status shouldBe OK
        }

        titleCheck(pageTitleText)
        welshToggleCheck("Welsh")
        h1Check(pageHeadingText, "xl")
        textOnPageCheck(p1Text, p1)
        buttonCheck(buttonText, continueButtonSelector)
      }
    }
  }

}
