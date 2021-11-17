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

package controllers.errors

import itUtils.{IntegrationTest, ViewHelpers}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.http.HeaderNames
import play.api.http.Status.UNAUTHORIZED
import play.api.libs.ws.WSResponse

class AgentAuthErrorControllerISpec extends IntegrationTest with ViewHelpers {

  object ExpectedResults {
    val heading: String = "There’s a problem"
    val title = "There’s a problem"
    val youCan: String = "You cannot view this client’s information. Your client needs to authorise you as their agent " +
      "(opens in new tab) before you can sign in to this service."
    val tryAnother = "Try another client’s details"
    val tryAnotherExpectedHref = "http://localhost:9081/report-quarterly/income-and-expenses/view/agents/client-utr"
    val authoriseAsAnAgentLink = "https://www.gov.uk/guidance/client-authorisation-an-overview"
    val authoriseYouAsText = "authorise you as their agent (opens in new tab)"

    val headingWelsh: String = "Mae problem wedi codi"
    val titleWelsh = "Mae problem wedi codi"
    val youCanWelsh: String = "Ni allwch fwrw golwg dros wybodaeth y cleient hwn. Mae angen i’ch cleient eich awdurdodi " +
      "i weithredu ar ei ran (yn agor tab newydd) cyn y gallwch fewngofnodi i’r gwasanaeth hwn."
    val tryAnotherWelsh = "Rhowch gynnig ar fanylion cleient arall"
    val authoriseYouAsTextWelsh = "eich awdurdodi i weithredu ar ei ran (yn agor tab newydd)"
  }
  
  object Selectors {
    val youCan = "#main-content > div > div > p:nth-child(2)"
    val tryAnother = "#main-content > div > div > a"
    val authoriseAsAnAgentLinkSelector = "#client_auth_link"
  }

  val url = s"http://localhost:$port/update-and-submit-income-tax-return/error/you-need-client-authorisation"

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
        textOnPageCheck(ExpectedResults.youCan, Selectors.youCan)
        linkCheck(ExpectedResults.authoriseYouAsText, Selectors.authoriseAsAnAgentLinkSelector, ExpectedResults.authoriseAsAnAgentLink)
        buttonCheck(ExpectedResults.tryAnother, Selectors.tryAnother, Some(ExpectedResults.tryAnotherExpectedHref))
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
        textOnPageCheck(ExpectedResults.youCanWelsh, Selectors.youCan)
        linkCheck(ExpectedResults.authoriseYouAsTextWelsh, Selectors.authoriseAsAnAgentLinkSelector, ExpectedResults.authoriseAsAnAgentLink)
        buttonCheck(ExpectedResults.tryAnotherWelsh, Selectors.tryAnother, Some(ExpectedResults.tryAnotherExpectedHref))
      }
    }
  }

}
