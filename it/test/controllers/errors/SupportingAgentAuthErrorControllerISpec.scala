/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers.errors

import itUtils.{IntegrationTest, ViewHelpers}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.http.HeaderNames
import play.api.http.Status.UNAUTHORIZED
import play.api.libs.ws.WSResponse

class SupportingAgentAuthErrorControllerISpec extends IntegrationTest with ViewHelpers {

  object ExpectedResults {
    lazy val heading = "You are not authorised to use this service"
    lazy val title = "You are not authorised to use this service"
    val suppAgent: String = "You’re a supporting agent for this client. Only your client or their main agent," +
      " if they have one, can access and submit their tax return."
    val accountHomeText = "Go back to account home"
    val accountHomeLink = "http://localhost:9081/report-quarterly/income-and-expenses/view/agents"

    val headingWelsh: String = "Nid ydych wedi’ch awdurdodi i ddefnyddio’r gwasanaeth hwn"
    val titleWelsh = "Nid ydych wedi’ch awdurdodi i ddefnyddio’r gwasanaeth hwn"
    // TODO: change English string to welsh
    val suppAgentWelsh: String = "You’re a supporting agent for this client. Only your client or their main agent," +
      " if they have one, can access and submit their tax return."
    val accountHomeTextWelsh = "Go back to account home"
  }

  object Selectors {
    val suppAgent = "#main-content > div > div > p"
    val backToAccountHomeLinkSelector = "#account_home_link"
  }

  val url = s"http://localhost:$port/update-and-submit-income-tax-return/error/supporting-agent-not-authorised"

  "an agent calling GET" when {
    "language is set to ENGLISH" should {
      "return a page" which {
        lazy val result: WSResponse = {
          authoriseIndividual()
          await(wsClient.url(url).get())
        }

        implicit def document: () => Document = () => Jsoup.parse(result.body)

        "returns status of UNAUTHORIZED(401)" in {
          result.status shouldBe UNAUTHORIZED
        }

        welshToggleCheck("English")
        titleCheck(ExpectedResults.title, isWelsh = false)
        h1Check(ExpectedResults.heading,"xl")
        textOnPageCheck(ExpectedResults.suppAgent, Selectors.suppAgent)
        linkCheck(ExpectedResults.accountHomeText, Selectors.backToAccountHomeLinkSelector, ExpectedResults.accountHomeLink)
      }
    }

    "language is set to WELSH" should {
      "return a page" which {
        lazy val result: WSResponse = {
          authoriseIndividual()
          await(wsClient.url(url).withHttpHeaders(HeaderNames.ACCEPT_LANGUAGE -> "cy").get())
        }

        implicit def document: () => Document = () => Jsoup.parse(result.body)

        "returns status of UNAUTHORIZED(401)" in {
          result.status shouldBe UNAUTHORIZED
        }

        welshToggleCheck("Welsh")
        titleCheck(ExpectedResults.titleWelsh, isWelsh = true)
        h1Check(ExpectedResults.headingWelsh,"xl")
        textOnPageCheck(ExpectedResults.suppAgentWelsh, Selectors.suppAgent)
        linkCheck(ExpectedResults.accountHomeText, Selectors.backToAccountHomeLinkSelector, ExpectedResults.accountHomeLink)
      }
    }
  }

}
