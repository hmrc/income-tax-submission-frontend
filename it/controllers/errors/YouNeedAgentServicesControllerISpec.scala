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
import play.api.http.{HeaderNames, Status}
import play.api.libs.ws.WSResponse

class YouNeedAgentServicesControllerISpec extends IntegrationTest with ViewHelpers with Status {

  object ExpectedResults {
    lazy val pageHeadingText = "You cannot view this page"
    lazy val pageTitleText = "You cannot view this page"
    lazy val youNeedText = "You need to"
    lazy val createAnAgentText = "create an agent services account"
    lazy val beforeYouCanText = "before you can view this page."
    lazy val createAnAgentLink = "https://www.gov.uk/guidance/get-an-hmrc-agent-services-account"
  }

  object Selectors {
    val p1Selector = "#main-content > div > div > p"
    val createAnAgentLinkSelector = "#create_agent_services_link"
  }

  import ExpectedResults._
  import Selectors._

  val errorPageUrl = s"http://localhost:$port/income-through-software/return/error/you-need-agent-services-account"

  "an user calling GET" when {
    "language is set to ENGLISH" should {
      "return a page" which {
        lazy val result: WSResponse = {
          authoriseIndividual()
          await(wsClient.url(errorPageUrl).get())
        }

        implicit def document: () => Document = () => Jsoup.parse(result.body)

        "returns status of UNAUTHORIZED(401)" in {
          result.status shouldBe UNAUTHORIZED
        }

        welshToggleCheck("English")
        titleCheck(pageTitleText)
        h1Check(pageHeadingText, "xl")
        textOnPageCheck(s"$youNeedText $createAnAgentText $beforeYouCanText", p1Selector)
        linkCheck(createAnAgentText, createAnAgentLinkSelector, createAnAgentLink)
      }
    }

    "language is set to WELSH" should {
      "return a page" which {
        lazy val result: WSResponse = {
          authoriseIndividual()
          await(wsClient.url(errorPageUrl).withHttpHeaders(HeaderNames.ACCEPT_LANGUAGE -> "cy").get())
        }

        implicit def document: () => Document = () => Jsoup.parse(result.body)

        "returns status of UNAUTHORIZED(401)" in {
          result.status shouldBe UNAUTHORIZED
        }

        welshToggleCheck("Welsh")
        titleCheck(pageTitleText)
        h1Check(pageHeadingText, "xl")
        textOnPageCheck(s"$youNeedText $createAnAgentText $beforeYouCanText", p1Selector)
        linkCheck(createAnAgentText, createAnAgentLinkSelector, createAnAgentLink)
      }
    }
  }

}
