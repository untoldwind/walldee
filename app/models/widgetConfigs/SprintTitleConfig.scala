package models.widgetConfigs

import play.api.libs.json._
import play.api.libs.json.JsObject

case class SprintTitleConfig(labelFont: Option[String] = None,
                             labelSize: Option[Int] = None)

object SprintTitleConfig {

  implicit object SprintTitleConfigFormat extends Format[SprintTitleConfig] {
    override def reads(json: JsValue): JsResult[SprintTitleConfig] =
      JsSuccess(SprintTitleConfig(
        (json \ "labelFont").asOpt[String],
        (json \ "labelSize").asOpt[Int]))

    override def writes(sprintTitleConfig: SprintTitleConfig): JsValue = JsObject(
      sprintTitleConfig.labelFont.map("labelFont" -> JsString(_)).toSeq ++
      sprintTitleConfig.labelSize.map("labelSize" -> JsNumber(_)).toSeq)
  }
}