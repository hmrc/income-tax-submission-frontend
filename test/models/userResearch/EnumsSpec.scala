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

package models.userResearch

import utils.UnitTest

class EnumsSpec extends UnitTest {

  "credential strength values" should {
    
    "be correct" in {
      
      CS_Strong.value shouldBe "strong"
      CS_Weak.value shouldBe "weak"
      CS_None.value shouldBe "none"
      
    }
    
  }

  "confidence level values" should {
    
    "be correct" in {
      
      L250.value shouldBe 250
      L200.value shouldBe 200
      L50.value shouldBe 50
      
    }
    
  }

  "affinity group values" should {
    
    "be correct" in {
      
      AG_Individual.value shouldBe "Individual"
      AG_Agent.value shouldBe "Agent"
      
    }
    
  }
  
}
