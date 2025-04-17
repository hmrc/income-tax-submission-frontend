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

package viewmodels

import models.tasklist.SectionTitle._
import models.tasklist.SectionTitleKeys._
import models.tasklist.TaskStatus._
import models.tasklist.TaskItemTitleKeys._
import models.tasklist.{TaskListModel, TaskStatus, TaskTitle}
import models.tasklist.TaskTitle._
import play.api.Logging
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.html.components._
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import views.html.templates.helpers.Heading2

import javax.inject.Inject

case class TaskListPageViewModel @Inject()(taskListData: Option[TaskListModel], prefix: String)
                                          (implicit messages: Messages) extends Logging {

  private val ZERO: Int = 0
  private val completionInfoKey: String = "taskList.completedText"
  private val completedKey: String = "completed"
  private val incompleteKey: String = "taskList.incomplete"
  private val tagBlue: String = "govuk-tag--blue"
  private val tagLightBlue: String = "govuk-tag--light-blue"
  private val tagWhite: String = "govuk-tag--white"
  private val tagGreen: String = "govuk-tag--green"

  def getTaskList: Seq[HtmlFormat.Appendable] = {
    taskListData match {
      case Some(tasks) =>
        for (section <- tasks.taskList) yield {
          val subHeading: String = new Heading2().apply(sectionTitleKeyOps(section.sectionTitle.toString)).body

          val taskItems: String = new GovukTaskList(new GovukTag)(TaskList(
            section.taskItems.getOrElse(Seq()).map { item =>
              TaskListItem(
                title = TaskListItemTitle(Text(sectionItemOps(item.title))),
                status = itemStatus(item.status),
                href = item.href,
                classes = item.title.toString
              )
            }
          )).body

          HtmlFormat.raw(subHeading ++ taskItems)
        }
      case None => Seq.empty
    }
  }

  def getCompletedText: String = {
    if (isComplete) messages(completedKey) else messages(incompleteKey)
  }

  def getCompletedInfoText: String = {
    messages(completionInfoKey, getCompletedCount, getAllSectionsCount)
  }

  private def isComplete: Boolean =
    if ((getCompletedCount.toInt == getAllSectionsCount.toInt) && getAllSectionsCount.toInt > ZERO) true else false

  private def getCompletedCount: String = {
    taskListData match {
      case Some(data) => checkCompleted(data)
      case None => ZERO.toString
    }
  }

  private def getAllSectionsCount: String = {
    taskListData match {
      case Some(data) => countItems(data)
      case None => ZERO.toString
    }
  }

  private def checkCompleted: TaskListModel => String = { data =>
    val numOfCompletedItems: Seq[Boolean] = for {
      taskItems <- data.taskList.map(_.taskItems)
      sectionItem <- taskItems.getOrElse(Seq())
    } yield {
      sectionItem.status.toString.equals(Completed.toString)
    }
    numOfCompletedItems.count(i => i).toString
  }

  private def countItems: TaskListModel => String = { data =>
    val numOfItems = for {
      taskItems <- data.taskList.map(_.taskItems)
    } yield {
      taskItems.getOrElse(Seq()).size
    }
    numOfItems.sum.toString
  }

  /*
   * TODO: Refactor this.
   *  This is a mapping of the API response to the keys.
   *  The TaskTitle is meant to provide typesafety in pattern matches and there is no benefit gained
   *  by having this in a map with does not leverage this.
   */
  private def sectionItemOps: TaskTitle => String = { taskTitle =>
    Map(
      UkResidenceStatus.toString -> messages(prefix + RESIDENCE_STATUS_KEY),
      FosterCarer.toString -> messages(prefix + FOSTER_CARER_KEY),
      DonationsUsingGiftAid.toString -> messages(prefix + DONATIONS_GIFT_AID_KEY),
      GiftsOfLandOrProperty.toString -> messages(prefix + GIFTS_LAND_PROPERTY_KEY),
      GiftsOfShares.toString -> messages(prefix + GIFTS_SHARES_SECURITIES_KEY),
      GiftsToOverseas.toString -> messages(prefix + GIFTS_TO_OVERSEAS_CHARITIES_KEY),
      PayeEmployment.toString -> messages(prefix + EMPLOYMENT_KEY),
      CIS.toString -> messages(prefix + CIS_KEY),
      ESA.toString -> messages(prefix + ESA_KEY),
      JSA.toString -> messages(prefix + JSA_KEY),
      StatePension.toString -> messages(prefix + STATE_PENSION_KEY),
      OtherUkPensions.toString -> messages(prefix + OTHER_UK_PENSIONS_KEY),
      UnauthorisedPayments.toString -> messages(prefix + UNAUTHORISED_PAYMENTS_KEY),
      LifeInsurance.toString -> messages(prefix + LIFE_INSURANCE_KEY),
      LifeAnnuity.toString -> messages(prefix + LIFE_ANNUITY_KEY),
      CapitalRedemption.toString -> messages(prefix + CAPITAL_REDEMPTION_KEY),
      VoidedISA.toString -> messages(prefix + VOIDED_ISA_KEY),
      PaymentsIntoUk.toString -> messages(prefix + PAYMENTS_INTO_UK_KEY),
      PaymentsIntoOverseas.toString -> messages(prefix + PAYMENTS_INTO_OVERSEAS_KEY),
      AnnualAllowances.toString -> messages(prefix + ANNUAL_ALLOWANCES_KEY),
      OverseasTransfer.toString -> messages(prefix + OVERSEAS_TRANSFER_KEY),
      ShortServiceRefunds.toString -> messages(prefix + SHORT_SERVICE_REFUNDS_KEY),
      IncomeFromOverseas.toString -> messages(prefix + INCOME_FROM_OVERSEAS_KEY),
      BanksAndBuilding.toString -> messages(prefix + BANKS_AND_BUILDING_KEY),
      TrustFundBond.toString -> messages(prefix + TRUST_FUND_BOND_KEY),
      GiltEdged.toString -> messages(prefix + GILT_EDGED_KEY),
      CashDividends.toString -> messages(prefix + CASH_DIVIDENDS_KEY),
      StockDividends.toString -> messages(prefix + STOCK_DIVIDENDS_KEY),
      DividendsFromUnitTrusts.toString -> messages(prefix + DIVIDENDS_FROM_UNIT_TRUSTS_KEY),
      FreeRedeemableShares.toString -> messages(prefix + FREE_REDEEMABLE_SHARES_KEY),
      CloseCompanyLoans.toString -> messages(prefix + CLOSE_COMPANY_LOANS_KEY),
      UkProperty.toString -> messages(prefix + UK_PROPERTY_KEY),
      ForeignProperty.toString -> messages(prefix + FOREIGN_PROPERTY_KEY),
      UkForeignProperty.toString -> messages(prefix + UK_FOREIGN_PROPERTY_KEY)
    )(taskTitle.toString)
  }

  private def sectionTitleKeyOps: String => String = { key =>
    Map(
      AboutYouTitle.toString -> messages(prefix + ABOUT_YOU_TITLE_KEY),
      CharitableDonationsTitle.toString -> messages(prefix + CHARITABLE_DONATIONS_TITLE_KEY),
      EmploymentTitle.toString -> messages(prefix + EMPLOYMENT_TITLE_KEY),
      SelfEmploymentTitle.toString -> messages(prefix + SELF_EMPLOYMENT_TITLE_KEY),
      EsaTitle.toString -> messages(prefix + ESA_TITLE_KEY),
      JsaTitle.toString -> messages(prefix + JSA_TITLE_KEY),
      PensionsTitle.toString -> messages(prefix + PENSIONS_TITLE_KEY),
      InsuranceGainsTitle.toString -> messages(prefix + INSURANCE_GAINS_TITLE_KEY),
      PaymentsIntoPensionsTitle.toString -> messages(prefix + PAYMENTS_INTO_PENSIONS_TITLE_KEY),
      InterestTitle.toString -> messages(prefix + INTEREST_TITLE_KEY),
      DividendsTitle.toString -> messages(prefix + DIVIDENDS_TITLE_KEY),
      UkPropertyTitle.toString -> messages(prefix + UK_PROPERTY_TITLE_KEY),
      ForeignPropertyTitle.toString -> messages(prefix + FOREIGN_PROPERTY_TITLE_KEY),
      UkForeignPropertyTitle.toString -> messages(prefix + UK_FOREIGN_PROPERTY_TITLE_KEY)
    )(key)
  }


  private def itemStatus: TaskStatus => TaskListItemStatus = {
    case status@TaskStatus.NotStarted =>
      TaskListItemStatus(Some(Tag(content = HtmlContent(messages(status.toString)),
        classes = tagBlue)))
    case status@TaskStatus.InProgress =>
      TaskListItemStatus(Some(Tag(content = HtmlContent(messages(status.toString)),
        classes = tagLightBlue)))
    case status@TaskStatus.Completed =>
      TaskListItemStatus(content = HtmlContent(messages(status.toString)),
        classes = tagWhite)
    case status@TaskStatus.CheckNow =>
      TaskListItemStatus(Some(Tag(content = HtmlContent(messages(status.toString)),
        classes = tagBlue)))
    case status@TaskStatus.UnderMaintenance =>
      TaskListItemStatus(content = HtmlContent(messages(status.toString)),
        classes = tagWhite)
  }
}
