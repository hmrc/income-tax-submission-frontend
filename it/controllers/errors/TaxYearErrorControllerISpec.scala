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

class TaxYearErrorControllerISpec extends IntegrationTest with ViewHelpers with Status {


  object ExpectedResults {
    val pageTitleText = "Page not found"
    val pageHeadingText = "Page not found"
    val youCanOnlyText = "You can only enter information for the 2021 to 2022 tax year."
    val checkThatYouveText = "Check that you’ve entered the correct web address."
    val ifTheWebsiteText: String = "If the website address is correct or you selected a link or button, " +
      "you can use Self Assessment: general enquiries (opens in new tab) to speak to someone about your income tax."
    val linkText = "Self Assessment: general enquiries (opens in new tab)"
    val linkHref = "https://www.gov.uk/government/organisations/hm-revenue-customs/contact/self-assessment"
  }

  object Selectors {
    val link = "#govuk-income-tax-link"
    val youCanOnlySelector = "#main-content > div > div > div.govuk-body > p:nth-child(1)"
    val checkThatYouveSelector = "#main-content > div > div > div.govuk-body > p:nth-child(2)"
    val ifTheWebsiteSelector = "#main-content > div > div > div.govuk-body > p:nth-child(3)"
  }

  import ExpectedResults._
  import Selectors._

  val errorPageUrl = s"http://localhost:$port/income-through-software/return/error/wrong-tax-year"

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
        linkCheck(linkText, link, linkHref)
        textOnPageCheck(youCanOnlyText, youCanOnlySelector)
        textOnPageCheck(checkThatYouveText, checkThatYouveSelector)
        textOnPageCheck(ifTheWebsiteText, ifTheWebsiteSelector)
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
          result.status shouldBe 200
        }

        titleCheck(pageTitleText)
        welshToggleCheck("Welsh")
        h1Check(pageHeadingText, "xl")
        linkCheck(linkText, link, linkHref)
        textOnPageCheck(youCanOnlyText, youCanOnlySelector)
        textOnPageCheck(checkThatYouveText, checkThatYouveSelector)
        textOnPageCheck(ifTheWebsiteText, ifTheWebsiteSelector)
      }
    }
  }

}
