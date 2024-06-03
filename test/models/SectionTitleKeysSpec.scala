/*
 * Copyright 2023 HM Revenue & Customs
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

import models.tasklist.SectionTitleKeys
import models.tasklist.SectionTitleKeys._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class SectionTitleKeysSpec extends AnyFreeSpec with Matchers {

  "SectionTitle" - {

    "must contain the correct values" in {
      SectionTitleKeys.values mustEqual Seq(
        AboutYouTitleKey,
        CharitableDonationsTitleKey,
        EmploymentTitleKey,
        SelfEmploymentTitleKey,
        EsaTitleKey,
        JsaTitleKey,
        PensionsTitleKey,
        PaymentsIntoPensionsTitleKey,
        InterestTitleKey,
        DividendsTitleKey
      )
    }
  }
}

