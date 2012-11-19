package models.widgetConfigs

import play.api.libs.json._
import play.api.libs.json.JsObject
import play.api.libs.json.JsString

case class BuildStatusConfig(labelFont: Option[String] = None,
                             labelSize: Option[Int] = None)

object BuildStatusConfig {

  implicit object BuildStatusConfigFormat extends Format[BuildStatusConfig] {
    override def reads(json: JsValue): BuildStatusConfig =
      BuildStatusConfig(
        (json \ "labelFont").asOpt[String],
        (json \ "labelSize").asOpt[Int])

    override def writes(buildStatusConfig: BuildStatusConfig): JsValue = JsObject(
      buildStatusConfig.labelFont.map("labelFont" -> JsString(_)).toSeq ++
        buildStatusConfig.labelSize.map("labelSize" -> JsNumber(_)).toSeq)
  }

}
