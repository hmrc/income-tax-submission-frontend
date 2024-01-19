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

package controllers.predicates

import common.SessionValues.{TAX_YEAR, VALID_TAX_YEARS}
import config.{AppConfig, ErrorHandler}
import models.User
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Results.Redirect
import play.api.mvc._
import services.ValidTaxYearListService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TaxYearAction @Inject()(taxYear: Int, missingTaxYearReset: Boolean)(
  implicit val appConfig: AppConfig,
  implicit val executionContext: ExecutionContext,
  val messagesApi: MessagesApi,
  validTaxYearListService: ValidTaxYearListService,
  errorHandler: ErrorHandler
) extends ActionRefiner[User, User] with I18nSupport {

  lazy val logger: Logger = Logger.apply(this.getClass)

  override def refine[A](request: User[A]): Future[Either[Result, User[A]]] = {
    implicit val implicitUser: User[A] = request

    implicit lazy val headerCarrier: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request,request.session)

    def taxYearListCheck(sessionExists: Boolean, validTaxYears: Seq[Int]) = {
      if (!appConfig.taxYearErrorFeature || validTaxYears.contains(taxYear)) {
        val sameTaxYear = request.session.get(TAX_YEAR).exists(_.toInt == taxYear)
        if (sessionExists) {
          if (sameTaxYear || !missingTaxYearReset) {
            Right(request)
          } else {
            logger.info("[TaxYearAction][refine] Tax year provided is different than that in session. Redirecting to Start Page.")
            val newSessionData = Seq(TAX_YEAR -> taxYear.toString, VALID_TAX_YEARS -> validTaxYears.mkString(","))
            Left(Redirect(controllers.routes.StartPageController.show(taxYear).url).addingToSession(newSessionData: _*))
          }
        } else {
          val newSessionData = Seq(TAX_YEAR -> taxYear.toString, VALID_TAX_YEARS -> validTaxYears.mkString(","))
          Left(Redirect(controllers.routes.StartPageController.show(taxYear).url).addingToSession(newSessionData: _*))
        }
      } else {
        logger.info(s"Invalid tax year, adding default tax year to session")
        val newSessionData = if(sessionExists){
          Seq(TAX_YEAR -> taxYear.toString)
        } else {
          Seq(TAX_YEAR -> taxYear.toString, VALID_TAX_YEARS -> validTaxYears.mkString(","))
        }
        Left(Redirect(controllers.routes.TaxYearErrorController.show).addingToSession(newSessionData: _*)(request))
      }
    }

    val validClientTaxYears = request.session.get(VALID_TAX_YEARS)

    if (validClientTaxYears.isEmpty){
      validTaxYearListService.getValidTaxYearList(implicitUser.nino, implicitUser.mtditid).map {
        case Left(error) => Left(errorHandler.handleError(error.status))
        case Right(validTaxYears) =>
          taxYearListCheck(sessionExists = false, validTaxYears.taxYears)
      }
    } else {
      Future.successful(
        taxYearListCheck(sessionExists = true, validClientTaxYears.get.split(",").toSeq.map(_.toInt))
      )
    }
  }
}

object TaxYearAction {
  def taxYearAction(taxYear: Int, missingTaxYearReset: Boolean = true)(implicit appConfig: AppConfig,
                                                                       ec: ExecutionContext,
                                                                       messages: MessagesApi,
                                                                       validTaxYearListService: ValidTaxYearListService,
                                                                       errorHandler: ErrorHandler
  ): TaxYearAction =
    new TaxYearAction(taxYear, missingTaxYearReset)
}
