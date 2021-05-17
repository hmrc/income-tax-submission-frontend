/*
 * Copyright 2021 HM Revenue & Customs
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

import models.mongo.UserData
import org.scalamock.handlers.CallHandler1
import org.scalamock.scalatest.MockFactory
import repositories.IncomeTaxUserDataRepository

import scala.concurrent.Future

trait MockIncomeTaxUserDataRepository extends MockFactory {

  val mockRepository: IncomeTaxUserDataRepository = mock[IncomeTaxUserDataRepository]

  def mockUpdate(response: Boolean = true): CallHandler1[UserData, Future[Boolean]] = {
    (mockRepository.update(_: UserData))
      .expects(*)
      .returns(Future.successful(response))
      .anyNumberOfTimes()
  }
}
