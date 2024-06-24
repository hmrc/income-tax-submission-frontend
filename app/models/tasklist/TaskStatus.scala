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

import models.{Enumerable, WithName}
import play.api.libs.json.{Json, OWrites, Reads}

trait TaskStatus extends Enumerable.Implicits

object TaskStatus extends TaskStatus {

  case class Completed() extends WithName("completed") with TaskStatus
  object Completed extends ReadsWrites[Completed]

  case class InProgress() extends WithName("inProgress") with TaskStatus
  object InProgress extends ReadsWrites[InProgress]

  case class NotStarted() extends WithName("notStarted") with TaskStatus
  object NotStarted extends ReadsWrites[NotStarted]

  case class CheckNow() extends WithName("checkNow") with TaskStatus
  object CheckNow extends ReadsWrites[CheckNow]

  val values: Seq[TaskStatus] = Seq(
    Completed(), InProgress(), NotStarted(), CheckNow()
  )

  implicit val enumerable: Enumerable[TaskStatus] =
    Enumerable(values.map(v => v.toString -> v): _*)

}