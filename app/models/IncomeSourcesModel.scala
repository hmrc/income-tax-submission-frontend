
package models

import play.api.libs.json.{Json, OFormat}

case class IncomeSourcesModel(dividends: Seq[String])

object IncomeSourcesModel {
  implicit val formats: OFormat[IncomeSourcesModel] = Json.format[IncomeSourcesModel]
}


