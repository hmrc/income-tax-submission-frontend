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

package config

import itUtils.ViewTest
import play.api.i18n.MessagesApi

import scala.io.Source

class MessagesSpec extends ViewTest {

  lazy val allLanguages: Map[String, Map[String, String]] = app.injector.instanceOf[MessagesApi].messages

  private val exclusionKeys: Set[String] = Set(
    "global.error.badRequest400.title",
    "global.error.badRequest400.heading",
    "global.error.badRequest400.message",
    "global.error.pageNotFound404.title",
    "global.error.pageNotFound404.heading",
    "global.error.pageNotFound404.message",
    "global.error.InternalServerError500.title",
    "global.error.InternalServerError500.heading",
    "global.error.InternalServerError500.message",
    "global.error.fallbackClientError4xx.title",
    "global.error.fallbackClientError4xx.heading",
    "global.error.fallbackClientError4xx.message",
    "betaBar.banner.message.1",
    "betaBar.banner.message.2",
    "betaBar.banner.message.3",
    "footer.welshHelp.text",
    "footer.accessibility.text",
    "footer.contact.url",
    "footer.welshHelp.url",
    "footer.govukHelp.url",
    "footer.privacy.url",
    "footer.termsConditions.url",
    "footer.govukHelp.text",
    "phase.banner.before",
    "phase.banner.after",
    "footer.cookies.text",
    "footer.privacy.text",
    "footer.termsConditions.text",
    "phase.banner.link",
    "footer.contact.text",
    "header.govuk.url",
    "footer.cookies.url",
    "language.day.plural",
    "language.day.singular",
    "back.text",
    "error.summary.title",
    "taskList.agent.aboutYouTitle",
    "taskList.agent.charitableDonationsTitle",
    "taskList.agent.employmentTitle",
    "taskList.agent.selfEmploymentTitle",
    "taskList.agent.esaTitle",
    "taskList.agent.jsaTitle",
    "taskList.agent.pensionsTitle",
    "taskList.agent.paymentsIntoPensionsTitle",
    "taskList.agent.interestTitle",
    "taskList.agent.dividendsTitle",
    "taskList.agent.ukResidenceStatusTask",
    "taskList.agent.fosterCarerTask",
    "taskList.agent.donationsUsingGiftAidTask",
    "taskList.agent.giftsOfLandOrPropertyTask",
    "taskList.agent.giftsOfSharesOrSecuritiesTask",
    "taskList.agent.giftsToOverseasCharitiesTask",
    "taskList.agent.employmentTask",
    "taskList.agent.cisTask",
    "taskList.agent.esaTask",
    "taskList.agent.jsaTask",
    "taskList.agent.statePensionTask",
    "taskList.agent.otherUkPensionsTask",
    "taskList.agent.unauthorisedPaymentsTask",
    "taskList.agent.shortServiceRefundsTask",
    "taskList.agent.incomeFromOverseasTask",
    "taskList.agent.lifeInsuranceTask",
    "taskList.agent.lifeAnnuityTask",
    "taskList.agent.capitalRedemptionTask",
    "taskList.agent.voidedIsaTask",
    "taskList.agent.paymentsIntoUkPensionsTask",
    "taskList.agent.paymentsIntoOverseasPensionsTask",
    "taskList.agent.annualAllowancesTask",
    "taskList.agent.overseasTransferTask",
    "taskList.agent.banksAndBuildingTask",
    "taskList.agent.trustFundBondTask",
    "taskList.agent.giltEdgedTask",
    "taskList.agent.cashDividendsTask",
    "taskList.agent.stockDividendsTask",
    "taskList.agent.dividendsUnitTrustsInvestmentCompaniesTask",
    "taskList.agent.freeOrRedeemableSharesTask",
    "taskList.agent.closeCompanyLoansWrittenOffReleasedTask",
    "common.cis",
    "common.pensions",
    "error.no-valid-income.p2.bullet1",
    "notStarted",
    "taskList.agent.insuranceGainsTitle",
    "common.employmentSL"
  )

  val betaMessagesInBothHMRCLibraryAndMessages = 3


  val defaults: Map[String, String] = allLanguages("en")
  val welsh: Map[String, String] = allLanguages("cy")

  "the messages file must have welsh translations" should {
    "check all keys in the default file other than those in the exclusion list has a corresponding translation" in {

      defaults.keys.foreach(
        key =>
          if (!exclusionKeys.contains(key)) {
            welsh.keys should contain(key)
          }
      )
    }
  }

  "the files" should {
    "have no duplicate keys" in {
      val bufferedSource = Source.fromFile("conf/messages")
      val keys = (for (line <- bufferedSource.getLines()) yield {
        line.split("=").head
      }).toList
      val exceptionList  = List("taskList.income")
      val listOfMessages = keys.collect { case s if s.trim.nonEmpty => s.trim } diff exceptionList
      val uniqueListOfMessages = listOfMessages.distinct

      listOfMessages.diff(uniqueListOfMessages) shouldBe List.empty

      bufferedSource.close
    }
  }

  "the default file" should {
    "have no duplicate messages(values) if all keys are unique" in {
      checkMessagesAreUnique(defaults, exclusionKeys)
    }
  }

  "the welsh file" should {
    "have no duplicate messages.cy(values) if all keys are unique" in {
      checkMessagesAreUnique(welsh, exclusionKeys)
    }
  }
}
