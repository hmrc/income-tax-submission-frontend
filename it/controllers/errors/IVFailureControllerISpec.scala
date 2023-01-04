/*
 * Copyright 2023 HM Revenue & Customs
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

class IVFailureControllerISpec extends IntegrationTest with ViewHelpers with Status {

  object ExpectedResults {
    lazy val pageTitleText = "There’s a problem"
    lazy val pageHeadingText = "There’s a problem"
    lazy val cannotAccessText = "You cannot access this service. This may be because:"
    lazy val tookTooLongText = "you took too long to enter information and the service has timed out"
    lazy val failedToAnswerText = "you have failed to answer enough questions correctly"
    lazy val couldNotMatchText = "we could not match your details to our system"
    lazy val signOutButtonText = "Sign out"
    lazy val signOutUrl = "/update-and-submit-income-tax-return/sign-out?isAgent=false"

    lazy val pageTitleTextWelsh = "Mae problem wedi codi"
    lazy val pageHeadingTextWelsh = "Mae problem wedi codi"
    lazy val cannotAccessTextWelsh = "Ni allwch gael mynediad at y gwasanaeth hwn. Gall hyn fod oherwydd y canlynol:"
    lazy val tookTooLongTextWelsh = "gwnaethoch gymryd gormod o amser i nodi gwybodaeth ac mae’ch cyfle i ddefnyddio’r gwasanaeth wedi dod i ben"
    lazy val failedToAnswerTextWelsh = "rydych wedi methu ag ateb digon o gwestiynau’n gywir"
    lazy val couldNotMatchTextWelsh = "nid oeddem yn gallu paru’ch manylion â’r hyn sydd ar ein system"
    lazy val signOutButtonTextWelsh = "Allgofnodi"
  }

  object Selectors {
    lazy val cannotAccessTextSelector = "#main-content > div > div > div.govuk-body > p"
    lazy val tookTooLongTextSelector = "#main-content > div > div > ul > li:nth-child(1)"
    lazy val failedToAnswerTextSelector = "#main-content > div > div > ul > li:nth-child(2)"
    lazy val couldNotMatchTextSelector = "#main-content > div > div > ul > li:nth-child(3)"
    lazy val signOutFormSelector = "#main-content > div > div > form"
    lazy val signOutButtonSelector = "#continue"
  }

  import ExpectedResults._
  import Selectors._

  val errorPageUrl = s"http://localhost:$port/update-and-submit-income-tax-return/error/we-could-not-confirm-your-details"

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

        welshToggleCheck("English")
        titleCheck(pageTitleText, isWelsh = false)
        h1Check(pageHeadingText, "xl")

        textOnPageCheck(cannotAccessText, cannotAccessTextSelector)
        textOnPageCheck(tookTooLongText, tookTooLongTextSelector)
        textOnPageCheck(failedToAnswerText, failedToAnswerTextSelector)
        textOnPageCheck(couldNotMatchText, couldNotMatchTextSelector)

        formGetLinkCheck(signOutUrl, signOutFormSelector)
        buttonCheck(signOutButtonText, signOutButtonSelector, None)
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

        welshToggleCheck("Welsh")
        titleCheck(pageTitleTextWelsh, isWelsh = true)
        h1Check(pageHeadingTextWelsh, "xl")

        textOnPageCheck(cannotAccessTextWelsh, cannotAccessTextSelector)
        textOnPageCheck(tookTooLongTextWelsh, tookTooLongTextSelector)
        textOnPageCheck(failedToAnswerTextWelsh, failedToAnswerTextSelector)
        textOnPageCheck(couldNotMatchTextWelsh, couldNotMatchTextSelector)

        formGetLinkCheck(signOutUrl, signOutFormSelector)
        buttonCheck(signOutButtonTextWelsh, signOutButtonSelector, None)
      }
    }
  }

}
