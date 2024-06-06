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

package models

import models.tasklist.SectionTitle.AboutYouTitle
import models.tasklist._
import play.api.libs.json.{JsObject, Json}
import utils.UnitTest

class TaskListSectionModelSpec extends UnitTest {


  val taskListSectionTitleModel: SectionTitle = AboutYouTitle()
  val jsonTaskListSectionTitleModel: JsObject = Json.obj(
    "content" -> "About you"
  )

  val taskListItemModel: Seq[TaskListSectionItem] = Seq(TaskListSectionItem(
    TaskTitle("UK residence status"),
    TaskStatus.Completed(),
    Some(""))
  )

  val jsonTaskListItemModel: Seq[JsObject] = Seq(Json.obj(
    "title" -> Json.obj(
      "content" -> "UK residence status"
    ),
    "status" -> "completed",
    "href" -> Some("")
  ))

  val model: TaskListSection = TaskListSection(taskListSectionTitleModel, Some(taskListItemModel))
  val jsonModel: JsObject = Json.obj(
    "sectionTitle" -> taskListSectionTitleModel.toString,
    "taskItems" -> jsonTaskListItemModel
  )

  "TaskListSectionModel" should {

    "parse to Json" in {
      Json.toJson(model) shouldBe jsonModel
    }

    "parse from Json" in {
      jsonModel.as[TaskListSection]
    }
  }
}
