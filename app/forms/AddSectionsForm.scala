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

package forms

import common.IncomeSources._
import models.IncomeSourcesModel
import play.api.data.Forms._
import play.api.data._

object AddSectionsForm {

  case class AddSectionsQuestionModel(addSections: Seq[String], addSectionsRaw: Seq[String]) {
  }

  object AddSectionsQuestionModel {
    //scalastyle:off
    def apply(addSections: Seq[String], incomeSources: IncomeSourcesModel): AddSectionsQuestionModel = {
      var newTailoring: Seq[String] = addSections
      if (incomeSources.interest.exists(accounts => accounts.exists(_.hasAmounts)) && !newTailoring.contains(INTEREST)) newTailoring = newTailoring :+ INTEREST
      if (incomeSources.dividends.isDefined && !newTailoring.contains(DIVIDENDS)) newTailoring = newTailoring :+ DIVIDENDS
      if (incomeSources.giftAid.isDefined && !newTailoring.contains(GIFT_AID)) newTailoring = newTailoring :+ GIFT_AID
      if (incomeSources.employment.isDefined && !newTailoring.contains(EMPLOYMENT)) newTailoring = newTailoring :+ EMPLOYMENT
      if (incomeSources.cis.isDefined && !newTailoring.contains(CIS)) newTailoring = newTailoring :+ CIS
      if (incomeSources.pensions.isDefined && !newTailoring.contains(PENSIONS)) newTailoring = newTailoring :+ PENSIONS
      AddSectionsQuestionModel(newTailoring, addSections)
    }
    //scalastyle:on
  }

  val addSections = "addSections"

  def addSectionsForm(sourcesModel: IncomeSourcesModel): Form[AddSectionsQuestionModel] = Form[AddSectionsQuestionModel](
    mapping(
      addSections -> seq(text),
      addSections -> seq(text)
    )((a1, a2) => AddSectionsQuestionModel.apply(a1, sourcesModel))(AddSectionsQuestionModel.unapply)
  )

}
