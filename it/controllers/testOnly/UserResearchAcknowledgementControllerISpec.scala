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

package controllers.testOnly

import itUtils.{IntegrationTest, ViewHelpers}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class UserResearchAcknowledgementControllerISpec extends IntegrationTest with ViewHelpers {

  private lazy val url: String = s"http://localhost:$port/update-and-submit-income-tax-return/test-only/user-research-acknowledgement"
  
  object ExpectedResults {
    val title = "Making Tax Digital for Income Tax"
    val p1 = "Making Tax Digital for Income Tax is the new way of reporting your earnings to HMRC. Instead of completing a Self Assessment return, you’ll use software of your choice (like Rhino, Xero or Quickbooks) to keep digital records and send Income Tax updates."
    val p2 = "You have to start using the new service from 6 April 2024, if your annual income from self employment and/or property is over £10,000."
    val p3 = "Once you start using Making Tax Digital for Income Tax, you will need to send the following updates each tax year:"
    
    val list1Heading = "Four quarterly updates:"
    val list1Text = "These are summaries of your business income and expenses, for each income source. You must send these updates every 3 months, using software."
    
    val list2Heading = "End of period statements:"
    val list2Text = "Your software will produce these statements and you’ll submit them for each income source at the end of the year. It’s your chance to make any accounting adjustments, claim any reliefs and confirm that your information is correct."
    
    val list3Heading = "One final declaration:"
    val list3Text = "This is just to confirm that all your information for the whole year is true - to the best of your knowledge. This completes your tax return."
    
    val continue = "Continue"
  }
  
  object Selectors {
    val p1 = "#main-content > div > div > p:nth-child(2)"
    val p2 = "#main-content > div > div > p:nth-child(3)"
    val p3 = "#main-content > div > div > p:nth-child(4)"
    
    val list1Heading = "#main-content > div > div > h2:nth-child(5)"
    val list1Text = "#main-content > div > div > ul:nth-child(6)"
    
    val list2Heading = "#main-content > div > div > h2:nth-child(7)"
    val list2Text = "#main-content > div > div > ul:nth-child(8)"
    
    val list3Heading = "#main-content > div > div > h2:nth-child(9)"
    val list3Text = "#main-content > div > div > ul:nth-child(10)"
  }
  
  ".show" should {
    
    "render the page" which {
      import ExpectedResults._

      val result = await(wsClient.url(url).get())
      implicit val document: () => Document = () => Jsoup.parse(result.body)

      titleCheck(title, isWelsh = false)
      textOnPageCheck(p1, Selectors.p1)
      textOnPageCheck(p2, Selectors.p2)
      textOnPageCheck(p3, Selectors.p3)

      textOnPageCheck(list1Heading, Selectors.list1Heading)
      textOnPageCheck(list1Text, Selectors.list1Text)

      textOnPageCheck(list2Heading, Selectors.list2Heading)
      textOnPageCheck(list2Text, Selectors.list2Text)

      textOnPageCheck(list3Heading, Selectors.list3Heading)
      textOnPageCheck(list3Text, Selectors.list3Text)

      buttonCheck(continue)
      
    }
  }
  
}
