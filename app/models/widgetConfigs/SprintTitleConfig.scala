package models.widgetConfigs

import play.api.libs.json._
import play.api.libs.json.JsObject

case class SprintTitleConfig(labelSize: Option[Int] = None)

object SprintTitleConfig {

  implicit object SprintTitleConfigFormat extends Format[SprintTitleConfig] {
    override def reads(json: JsValue): SprintTitleConfig =
      SprintTitleConfig(
        (json \ "labelSize").asOpt[Int])

    override def writes(sprintTitleConfig: SprintTitleConfig): JsValue = JsObject(
      sprintTitleConfig.labelSize.map("labelSize" -> JsNumber(_)).toSeq)
  }
}