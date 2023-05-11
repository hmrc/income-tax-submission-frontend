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

import models.cis.AllCISDeductions
import models.employment.AllEmploymentData
import models.gains.{InsurancePoliciesModel, LifeInsuranceModel}
import models.pensions.Pensions
import models.statebenefits.AllStateBenefitsData
import utils.UnitTest

import scala.reflect.runtime.{universe => reflect}

class OverviewTailoringModelSpec extends UnitTest {

  case class IncomeSourceSetup(sourceName: String, functionName: String) {
    val tailoringList: Seq[String] = Seq(sourceName)
  }

  private val emptyIncomeSources: IncomeSourcesModel = IncomeSourcesModel()
  private val populatedIncomeSources: IncomeSourcesModel = IncomeSourcesModel(
    Some(List()),
    Some(DividendsModel()),
    Some(Seq(InterestModel("Mah Swamp", "1234567890", Some(500), Some(500)))),
    Some(GiftAidModel()),
    Some(AllEmploymentData(Seq(), None, Seq(), None)),
    Some(AllCISDeductions(None, None)),
    Some(Pensions(None, None, None, None, None)),
    Some(InsurancePoliciesModel("2020-01-04T05:01:01Z", Seq(LifeInsuranceModel(None,None,500,None,None,None,None)) ,None,None,None,None)),
    Some(AllStateBenefitsData(None, None)),
    Some(SavingsIncomeDataModel(None, None, None)),
    Some(StockDividendsModel(Some("2020-01-04T05:01:01Z"), None, None, Some(Dividend(Some("ref"), Some(123.45))), None, None))
  )

  val sources = Seq(
    IncomeSourceSetup("dividends", "hasDividends"),
    IncomeSourceSetup("interest", "hasInterest"),
    IncomeSourceSetup("gift-aid", "hasGiftAid"),
    IncomeSourceSetup("employment", "hasEmployment"),
    IncomeSourceSetup("cis", "hasCis"),
    IncomeSourceSetup("gains", "hasGains"),
    IncomeSourceSetup("pensions", "hasPensions"),
    IncomeSourceSetup("state-benefits", "hasStateBenefits"),
    IncomeSourceSetup("stock-dividends", "hasStockDividends")
  )

  "the number of sources match the number of income source fields" in {
    sources.:+(("interest-savings", "hasInterest-savings")).length shouldBe classOf[IncomeSourcesModel].getConstructors.head.getParameterCount - 1
  }

  sources.map { data =>

    lazy val methodSymbol: reflect.MethodSymbol = reflect.typeOf[OverviewTailoringModel].decl(reflect.TermName(data.functionName)).asMethod
    lazy val mirror = reflect.runtimeMirror(classOf[OverviewTailoringModel].getClassLoader)

    def method(model: OverviewTailoringModel): Boolean = mirror.reflect(model).reflectMethod(methodSymbol).apply().asInstanceOf[Boolean]

    lazy val modelWithoutSources = OverviewTailoringModel(data.tailoringList, emptyIncomeSources)
    lazy val modelWithoutTailoringList = OverviewTailoringModel(Seq.empty[String], populatedIncomeSources)
    lazy val modelWithNeither = OverviewTailoringModel(Seq.empty[String], emptyIncomeSources)

    s"calling .${data.functionName}" should {

      "return true" when {

        s"'${data.sourceName}' is in the tailoring list, and there is no ${data.sourceName} income source data" in {
          method(modelWithoutSources) shouldBe true
        }

        s"'${data.sourceName}' is not in the tailoring list, but there is ${data.sourceName} income source data" in {
          method(modelWithoutTailoringList) shouldBe true
        }

      }

      "return false" when {

        s"'${data.sourceName}' is not in the tailoring list and there is no data in the income sources" in {
          method(modelWithNeither) shouldBe false
        }

      }

    }
  }

}
