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

import common.SessionValues
import config.AppConfig
import models.User
import play.api.Logging
import play.api.libs.json.{Json, Reads}
import play.api.mvc.Results.Redirect
import play.api.mvc.{Request, Result}
import uk.gov.hmrc.http.{HeaderCarrier, SessionKeys}

import scala.concurrent.Future

trait SessionDataHelper extends Logging {

  val appConfig: AppConfig

  def retrieveTaxYearList(implicit user: User[_]): Seq[Int] = {
    user.session.get(SessionValues.VALID_TAX_YEARS).getOrElse("").split(",").toSeq.map(_.toInt)
  }

  def firstClientTaxYear(implicit user: User[_]): Int = retrieveTaxYearList.head
  def latestClientTaxYear(implicit user: User[_]): Int = retrieveTaxYearList.last

  def singleValidTaxYear(implicit user: User[_]): Boolean = firstClientTaxYear == latestClientTaxYear

  def getSessionData[T](key: String)(implicit request: Request[_], reads: Reads[T]): Option[T] = {
    request.session.get(key).flatMap { stringValue =>
      Json.parse(stringValue).asOpt[T]
    }
  }

  def withSessionId[A](block: String => Future[Result])
                      (implicit request: Request[A], hc: HeaderCarrier): Future[Result] =
    hc.sessionId.map(_.value).orElse(request.headers.get(SessionKeys.sessionId)).fold {
      logger.info("[SessionHelper][withSessionId] No session ID was found for the request. Redirecting user to login")
      Future.successful(Redirect(appConfig.signInUrl))
    }(block)
}
