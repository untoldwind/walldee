package models.widgetConfigs

import play.api.libs.json._
import play.api.libs.json.JsObject
import play.api.libs.json.JsString

case class HostStatusConfig(labelFont: Option[String] = None,
                            labelSize: Option[Int] = None)

object HostStatusConfig {

  implicit object HostStatusConfigFormat extends Format[HostStatusConfig] {
    override def reads(json: JsValue): HostStatusConfig =
      HostStatusConfig(
        (json \ "labelFont").asOpt[String],
        (json \ "labelSize").asOpt[Int])

    override def writes(hostStatusConfig: HostStatusConfig): JsValue = JsObject(
      hostStatusConfig.labelFont.map("labelFont" -> JsString(_)).toSeq ++
        hostStatusConfig.labelSize.map("labelSize" -> JsNumber(_)).toSeq)
  }

}
