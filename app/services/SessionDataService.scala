/*
 * Copyright 2025 HM Revenue & Customs
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

package services

import common.SessionValues
import config.AppConfig
import connectors.SessionDataConnector
import models.errors.MissingAgentClientDetails
import models.session.UserSessionData
import play.api.Logging
import play.api.mvc.Request
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SessionDataService @Inject()(sessionDataConnector: SessionDataConnector,
                                   val config: AppConfig
                                  )(implicit ec: ExecutionContext) extends Logging {

  def getSessionData[A](sessionId: String)
                       (implicit request: Request[A], hc: HeaderCarrier): Future[UserSessionData] =
    getSessionDataFromSessionStore().map {
      case Some(sessionData) => sessionData
      case _ =>
        getFallbackSessionData(sessionId) match {
          case Some(sessionData) => sessionData
          case _ =>
            logger.warn("[SessionDataService][getSessionData] Session Data service and Session Cookie both returned empty data. Throwing exception")
            throw MissingAgentClientDetails("Session Data service and Session Cookie both returned empty data")
        }
    }

  private[services] def getFallbackSessionData[A](sessionId: String)
                                                 (implicit request: Request[A]): Option[UserSessionData] =
    (
      request.session.get(SessionValues.CLIENT_NINO),
      request.session.get(SessionValues.CLIENT_MTDITID)
    ) match {
      case (Some(nino), Some(mtdItId)) => Some(UserSessionData(sessionId, mtdItId, nino))
      case (optNino, optMtdItId) =>
        val missingData = Seq(
          Option.when(optNino.isEmpty)("NINO"),
          Option.when(optMtdItId.isEmpty)("MTDITID")
        ).flatten.mkString(", ")
        logger.warn(s"[SessionDataService][getFallbackSessionData] Could not find $missingData in request session. Returning no data")
        None
    }

  private[services] def getSessionDataFromSessionStore()(implicit hc: HeaderCarrier): Future[Option[UserSessionData]] =
    if (config.sessionCookieServiceEnabled) {
      sessionDataConnector.getSessionData.map {
        case Right(sessionDataOpt) =>
          if(sessionDataOpt.isEmpty) logger.warn("Session cookie service returned empty data. Returning no data")
          sessionDataOpt
        case Left(err) =>
          logger.info(s"[getSessionDataFromSessionStore] Request to retrieve session data failed with error status: ${err.status} and error body: ${err.body}. Returning None")
          None
      }
    } else {
      Future.successful(None)
    }
}
