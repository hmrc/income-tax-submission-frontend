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
import models.tasklist.{StatusTag, TaskListModel}
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
  private val tagWhite: String = "govuk-tag--white"
  private val tagGreen: String = "govuk-tag--green"

  def getTaskList: Seq[HtmlFormat.Appendable] = {
    taskListData match {
      case Some(tasks) =>
        for (section <- tasks.taskList) yield {
          val subHeading: String = new Heading2().apply(sectionTitleKeyOps(section.sectionTitle)).body

          val taskItems: String = new GovukTaskList(new GovukTag)(TaskList(
            section.taskItems.getOrElse(Seq()).map { item =>
              TaskListItem(
                title = TaskListItemTitle(Text(sectionItemOps(item.title.content))),
                status = itemStatus(item.status.content),
                href = item.href
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
      sectionItem.status.content.equals(StatusTag.Completed.toString)
    }
    numOfCompletedItems.count(_.equals(true)).toString
  }

  private def countItems: TaskListModel => String = { data =>
    val numOfItems = for {
      taskItems <- data.taskList.map(_.taskItems)
    } yield {
      taskItems.getOrElse(Seq()).size
    }
    numOfItems.sum.toString
  }

  private def sectionItemOps: String => String = { title =>
    messages(prefix + title)
  }

  private def sectionTitleKeyOps: String => String = { key =>
      Map(
        AboutYouTitle.toString -> messages(prefix + AboutYouTitleKey.toString),
        CharitableDonationsTitle.toString -> messages(prefix + CharitableDonationsTitleKey.toString),
        EmploymentTitle.toString -> messages(prefix + EmploymentTitleKey.toString),
        SelfEmploymentTitle.toString -> messages(prefix + SelfEmploymentTitleKey.toString),
        EsaTitle.toString -> messages(prefix + EsaTitleKey.toString),
        JsaTitle.toString -> messages(prefix + JsaTitleKey.toString),
        PensionsTitle.toString -> messages(prefix + PensionsTitleKey.toString),
        PaymentsIntoPensionsTitle.toString -> messages(prefix + PaymentsIntoPensionsTitleKey.toString),
        InterestTitle.toString -> messages(prefix + InterestTitleKey.toString),
        DividendsTitle.toString -> messages(prefix + DividendsTitleKey.toString)
      ).applyOrElse(key, Map(key -> ""))
  }


  private def itemStatus: String => TaskListItemStatus = {
    case status@StatusTag.NotStarted.toString =>
      TaskListItemStatus(Some(Tag(content = HtmlContent(messages(status)),
        classes = tagBlue)))
    case status@StatusTag.InProgress.toString =>
      TaskListItemStatus(Some(Tag(content = HtmlContent(messages(status)),
        classes = tagGreen)))
    case status@StatusTag.Completed.toString =>
      TaskListItemStatus(content = HtmlContent(messages(status)),
        classes = tagWhite)
    case status@StatusTag.CheckNow.toString =>
      TaskListItemStatus(Some(Tag(content = HtmlContent(messages(status)),
        classes = tagBlue)))
    case _ => TaskListItemStatus(Some(Tag(content = HtmlContent(messages(StatusTag.NotStarted.toString)),
      classes = tagBlue)))
  }
}
