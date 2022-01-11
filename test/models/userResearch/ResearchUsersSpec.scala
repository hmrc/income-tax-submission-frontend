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

package models.userResearch

import utils.UnitTest

class ResearchUsersSpec extends UnitTest {
  
  val testEnrolment: ResearchUser = ResearchUser("TEST_ID", 2021, "AA123456A", CS_Strong, L250, AG_Individual, Seq(Enrolment("HMRC-MTD-IT", Seq(Identifier("MTDITID", "1234567890")))), Seq(DelegatedEnrolment("HMRC-MTD-IT", Seq(Identifier("MTDITID", "1234567890")), "mtd-auth")))
  
  ".getUserCredentials" should {
    
    "return the user that matches the user id" in {
      
      ResearchUsers.getUserCredentials("TEST_ID") shouldBe Some(testEnrolment)
      
    }
    
    "return None if the user id cannot be found" in {
      ResearchUsers.getUserCredentials("non-existent-id") shouldBe None
    }
    
  }
  
}
