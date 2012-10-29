package models.widgetConfigs

import play.api.libs.json._
import play.api.libs.json.JsObject
import play.api.libs.json.JsString

case class AlarmsConfig(labelFont: Option[String] = None,
                       labelSize: Option[Int] = None)

object AlarmsConfig {

  implicit object AlarmConfigFormat extends Format[AlarmsConfig] {
    override def reads(json: JsValue): AlarmsConfig =
      AlarmsConfig(
        (json \ "labelFont").asOpt[String],
        (json \ "labelSize").asOpt[Int])

    override def writes(alarmsConfig: AlarmsConfig): JsValue = JsObject(
      alarmsConfig.labelFont.map("labelFont" -> JsString(_)).toSeq ++
        alarmsConfig.labelSize.map("labelSize" -> JsNumber(_)).toSeq)
  }

}
