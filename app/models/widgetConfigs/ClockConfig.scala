package models.widgetConfigs

import play.api.libs.json.{JsNumber, JsObject, JsValue, Format}

case class ClockConfig(labelSize:Option[Int])

object ClockConfig {
  implicit object ClockConfigFormat extends Format[ClockConfig] {
    override def reads(json: JsValue): ClockConfig =
      ClockConfig(
        (json \ "labelSize").asOpt[Int])

    override def writes(clockConfig: ClockConfig): JsValue = JsObject(
      clockConfig.labelSize.map("labelSize" -> JsNumber(_)).toSeq)
  }
}
