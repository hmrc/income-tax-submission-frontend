/*
 * Copyright 2022 HM Revenue & Customs
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

package forms

import forms.AddSectionsForm.AddSectionsQuestionModel
import models.IncomeSourcesModel
import play.api.data.Form
import utils.UnitTest

class AddSectionsFormSpec extends UnitTest {

  def theForm(): Form[AddSectionsQuestionModel] = {
    AddSectionsForm.addSectionsForm(IncomeSourcesModel())
  }

  "The form" should {

    "correctly validate" when {

      "with one input value" in {
        val expected = AddSectionsQuestionModel(Seq("dividends"), Seq("dividends"))
        val actual = theForm().bind(Map("addSections[0]" -> "dividends")).value

        actual shouldBe Some(expected)
      }

      "with two input values" in {
        val expected = AddSectionsQuestionModel(Seq("cis", "gift-aid"), Seq("cis", "gift-aid"))
        val actual = theForm().bind(Map("addSections[0]" -> "cis", "addSections[1]" -> "gift-aid")).value

        actual shouldBe Some(expected)
      }

      "with multiple values" in {
        val expected = AddSectionsQuestionModel(Seq("dividends", "interest", "employment", "cis", "gift-aid", "pensions"), Seq("dividends", "interest", "employment", "cis", "gift-aid", "pensions"))
        val actual = theForm().bind(Map(
          "addSections[0]" -> "dividends",
          "addSections[1]" -> "interest",
          "addSections[2]" -> "employment",
          "addSections[3]" -> "cis",
          "addSections[4]" -> "gift-aid",
          "addSections[5]" -> "pensions")).value

        actual shouldBe Some(expected)
      }
    }
  }
}
