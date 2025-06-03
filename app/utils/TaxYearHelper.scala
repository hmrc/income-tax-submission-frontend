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

package utils

import com.google.inject.Singleton

import java.time.{Clock, LocalDate}
import javax.inject.Inject

@Singleton
class TaxYearHelper @Inject()(clock: Clock) {

  def taxYear: Int = {
    val now = LocalDate.now(clock)

    //noinspection ScalaStyle
    val taxYearCutoffDate: LocalDate = LocalDate.of(now.getYear, 4, 5)

    if (now.isAfter(taxYearCutoffDate)) now.getYear + 1 else now.getYear
  }

}
