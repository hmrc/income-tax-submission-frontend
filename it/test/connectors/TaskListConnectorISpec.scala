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

package connectors

import itUtils.IntegrationTest
import models.tasklist._
import models.{APIErrorBodyModel, APIErrorModel, APIErrorsBodyModel}
import play.api.libs.json.Json
import play.mvc.Http.Status._

class TaskListConnectorISpec extends IntegrationTest {

  private lazy val connector: TaskListConnector = app.injector.instanceOf[TaskListConnector]

  private val taskListSectionTitle = SectionTitle.AboutYouTitle
  private val taskListItemModel = Seq(TaskListSectionItem(TaskTitle.UkResidenceStatus, TaskStatus.Completed, Some("")))

  val tasklistUrl = s"/income-tax-submission-service/income-tax/nino/$nino/sources/task-list/$taxYearEOY"

  ".TaskListConnector" should {
    "return a TaskListSectionModel" in {
        val expectedResult = TaskListModel(Seq(TaskListSection(taskListSectionTitle, Some(taskListItemModel))))

        stubGet(tasklistUrl, OK, Json.toJson(expectedResult).toString())

        val result = await(connector.getTaskList(nino, taxYearEOY))

        result shouldBe Right(Some(expectedResult))
    }
    "non json is returned" in {
      stubGet(tasklistUrl, INTERNAL_SERVER_ERROR, "")

      val result = await(connector.getTaskList(nino, taxYearEOY))

      result shouldBe Left(APIErrorModel(INTERNAL_SERVER_ERROR, APIErrorBodyModel.parsingError))
    }

    "API Returns multiple errors" in {
      val expectedResult = APIErrorModel(INTERNAL_SERVER_ERROR, APIErrorsBodyModel(Seq(
        APIErrorBodyModel("INVALID_IDTYPE", "ID is invalid"),
        APIErrorBodyModel("INVALID_IDTYPE_2", "ID 2 is invalid"))))

      val responseBody = Json.obj(
        "failures" -> Json.arr(
          Json.obj("code" -> "INVALID_IDTYPE",
            "reason" -> "ID is invalid"),
          Json.obj("code" -> "INVALID_IDTYPE_2",
            "reason" -> "ID 2 is invalid")
        )
      )
      stubGet(tasklistUrl, BAD_REQUEST, responseBody.toString())

      val result = await(connector.getTaskList(nino, taxYearEOY))

      result shouldBe Left(expectedResult)
    }

    "return a PARSING_ERROR" in {
      val invalidJson = Json.obj(
        "sectionTitle" -> ""
      )

      val expectedResult = APIErrorModel(
        INTERNAL_SERVER_ERROR,
        APIErrorBodyModel(
          "PARSING_ERROR",
          "Error parsing response from API - List((/taskList,List(JsonValidationError(List(error.path.missing),List()))))"
        )
      )

      stubGet(tasklistUrl, OK, invalidJson.toString())
      val result = await(connector.getTaskList(nino, taxYearEOY))

      result shouldBe Left(expectedResult)
    }

    "return a SERVICE_UNAVAILABLE" in {
      val expectedResult = APIErrorModel(SERVICE_UNAVAILABLE, APIErrorBodyModel("SERVICE_UNAVAILABLE", "Service unavailable"))

      stubGet(tasklistUrl, SERVICE_UNAVAILABLE, expectedResult.toJson.toString())
      val result = await(connector.getTaskList(nino, taxYearEOY))

      result shouldBe Left(expectedResult)
    }

    "return a Right(TaskListSectionModel())" in {
      val expectedResult = Right(None)

      stubGet(tasklistUrl, NOT_FOUND, "{}")
      val result = await(connector.getTaskList(nino, taxYearEOY))

      result shouldBe expectedResult
    }
    "return a INTERNAL_SERVER_ERROR" in {
      val expectedResult = APIErrorModel(INTERNAL_SERVER_ERROR, APIErrorBodyModel("INTERNAL_SERVER_ERROR", "Internal server error"))

      stubGet(tasklistUrl, INTERNAL_SERVER_ERROR, expectedResult.toJson.toString())
      val result = await(connector.getTaskList(nino, taxYearEOY))

      result shouldBe Left(expectedResult)
    }
    "return a INTERNAL_SERVER_ERROR when unexpected status 408" in {
      val expectedResult = APIErrorModel(INTERNAL_SERVER_ERROR, APIErrorBodyModel("INTERNAL_SERVER_ERROR", "Internal server error"))

      stubGet(tasklistUrl, REQUEST_TIMEOUT, expectedResult.toJson.toString())
      val result = await(connector.getTaskList(nino, taxYearEOY))

      result shouldBe Left(expectedResult)
    }

  }

}
