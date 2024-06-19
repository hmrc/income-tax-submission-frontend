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

package models.tasklist.taskItemTitles

import models.WithName
import models.tasklist.TaskTitle
import play.api.libs.json.{Json, OWrites, Reads}

object UkInsuranceGainsTitles {

  case class LifeInsurance() extends WithName("LifeInsuranceTitle") with TaskTitle
  object LifeInsurance {
    implicit val nonStrictReads: Reads[LifeInsurance] = Reads.pure(LifeInsurance())
    implicit val writes: OWrites[LifeInsurance] = OWrites[LifeInsurance](_ => Json.obj())
  }

  case class LifeAnnuity() extends WithName("LifeAnnuityTitle") with TaskTitle
  object LifeAnnuity {
    implicit val nonStrictReads: Reads[LifeAnnuity] = Reads.pure(LifeAnnuity())
    implicit val writes: OWrites[LifeAnnuity] = OWrites[LifeAnnuity](_ => Json.obj())
  }

  case class CapitalRedemption() extends WithName("CapitalRedemptionTitle") with TaskTitle
  object CapitalRedemption {
    implicit val nonStrictReads: Reads[CapitalRedemption] = Reads.pure(CapitalRedemption())
    implicit val writes: OWrites[CapitalRedemption] = OWrites[CapitalRedemption](_ => Json.obj())
  }

  case class VoidedISA() extends WithName("VoidedISATitle") with TaskTitle
  object VoidedISA {
    implicit val nonStrictReads: Reads[VoidedISA] = Reads.pure(VoidedISA())
    implicit val writes: OWrites[VoidedISA] = OWrites[VoidedISA](_ => Json.obj())
  }

}
