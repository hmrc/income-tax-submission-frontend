/*
 * Copyright 2022 HM Revenue & Customs
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

class IndividualAuthErrorControllerISpec extends IntegrationTest with ViewHelpers with Status {

  object ExpectedResults {
    val validTitle: String = "You cannot view this page"
    val pageContent: String = "You need to sign up for Making Tax Digital for Income Tax before you can view this page."
    val linkContent: String = "sign up for Making Tax Digital for Income Tax"
    val linkHref: String = "https://www.gov.uk/guidance/sign-up-your-business-for-making-tax-digital-for-income-tax"

    val validTitleWelsh: String = "Ni allwch fwrw golwg dros y dudalen hon"
    val pageContentWelsh: String = "Mae’n rhaid i chi gofrestru ar gyfer y cynllun Troi Treth yn Ddigidol ar gyfer Treth " +
      "Incwm cyn i chi allu bwrw golwg ar y dudalen hon."
    val linkContentWelsh: String = "gofrestru ar gyfer y cynllun Troi Treth yn Ddigidol ar gyfer Treth Incwm"
  }

  object Selectors {
    val paragraphSelector: String = ".govuk-body"
    val linkSelector: String = paragraphSelector + " > a"
  }

  import ExpectedResults._
  import Selectors._

  val errorPageUrl = s"http://localhost:$port/update-and-submit-income-tax-return/error/you-need-to-sign-up"

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

        titleCheck(validTitle, isWelsh = false)
        welshToggleCheck("English")
        h1Check(validTitle, "xl")
        textOnPageCheck(pageContent, paragraphSelector)
        linkCheck(linkContent, linkSelector, linkHref)
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

        titleCheck(validTitleWelsh, isWelsh = true)
        welshToggleCheck("Welsh")
        h1Check(validTitleWelsh, "xl")
        textOnPageCheck(pageContentWelsh, paragraphSelector)
        linkCheck(linkContentWelsh, linkSelector, linkHref)
      }
    }
  }

}
