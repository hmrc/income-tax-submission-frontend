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

trait StatusTag extends Enumerable.Implicits

object StatusTag {

  case object Completed extends WithName("completed") with StatusTag

  case object InProgress extends WithName("inProgress") with StatusTag

  case object NotStarted extends WithName("notStarted") with StatusTag

  case object CheckNow extends WithName("checkNow") with StatusTag

  val values: Seq[StatusTag] = Seq(
    Completed, InProgress, NotStarted, CheckNow
  )

  implicit val enumerable: Enumerable[StatusTag] =
    Enumerable(values.map(v => v.toString -> v): _*)

}