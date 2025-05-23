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

package models.tasklist

import models.tasklist.TaskTitle._
import org.scalatest.EitherValues
import org.scalatest.Inspectors.forEvery
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsString, JsValue, Json}

class TaskTitleSpec extends AnyFreeSpec with Matchers with EitherValues {

  "TaskTitle" - {

    "TaskTitle" - {
      "serializes and deserializes from a TaskTitle  to a json string value" in {
        forEvery(TaskTitle.values) { taskTitle =>
          val result = Json.toJson(taskTitle)
          result mustBe JsString(taskTitle.entryName)
          result.validate[TaskTitle].asEither.value mustBe taskTitle
        }
      }
    }

    "must parse each element to jsValue successfully" in {
      val underTest: Seq[JsValue] = TaskTitle.values.map(x => Json.toJson(x))
      underTest.isInstanceOf[Seq[JsValue]] mustBe true
    }
  }

}
