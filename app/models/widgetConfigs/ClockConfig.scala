package models.widgetConfigs

import play.api.libs.json._
import play.api.libs.json.JsObject
import play.api.libs.json.JsNumber

case class ClockConfig(labelFont:Option[String] = None,
                       labelSize:Option[Int] = None)

object ClockConfig {
  implicit object ClockConfigFormat extends Format[ClockConfig] {
    override def reads(json: JsValue): ClockConfig =
      ClockConfig(
        (json \ "labelFont").asOpt[String],
        (json \ "labelSize").asOpt[Int])

    override def writes(clockConfig: ClockConfig): JsValue = JsObject(
      clockConfig.labelFont.map("labelFont" -> JsString(_)).toSeq ++
      clockConfig.labelSize.map("labelSize" -> JsNumber(_)).toSeq)
  }
}
