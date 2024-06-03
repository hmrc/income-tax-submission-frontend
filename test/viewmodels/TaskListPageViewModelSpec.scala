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

import models.tasklist.{TaskListSection, TaskListSectionItem, TaskStatus, TaskTitle}
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import play.twirl.api.HtmlFormat
import utils.UnitTest

class TaskListPageViewModelSpec extends UnitTest {

  implicit private val messages: Messages = stubMessages()

  private val underTest: TaskListPageViewModel = TaskListPageViewModel(Some(taskListModel), "")

  private val COMPLETED: String = "completed"
  private val INCOMPLETE: String = "taskList.incomplete"

  ".getTaskList" should {
    "return the task list model as html" in {
      underTest.getTaskList.isInstanceOf[Seq[HtmlFormat.Appendable]] shouldBe true
    }

    "return the task list with no data" in {
      underTest.copy(None).getTaskList shouldBe Seq()
    }
  }

  ".getCompletedText" should {
    "return Completed" in {
      underTest.copy(Some(taskListModelCompleted)).getCompletedText shouldBe COMPLETED
    }

    "return Incomplete" in {
      underTest.getCompletedText shouldBe INCOMPLETE
    }
  }
}
