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

import play.api.mvc.{Request, WrappedRequest}

case class User[T](mtditid: String,
                   arn: Option[String],
                   nino: String,
                   affinityGroup: String,
                   sessionId: String,
                   isSecondaryAgent: Boolean = false)
                  (implicit request: Request[T]) extends WrappedRequest[T](request) {
  def isAgent: Boolean = arn.nonEmpty
}
