package models.widgetConfigs

import play.api.libs.json._
import play.api.data.Forms._
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsNumber

case class BuildStatusConfig(labelFont: Option[String] = None,
                             labelSize: Option[Int] = None) extends WidgetConfig

object BuildStatusConfig extends WidgetConfigMapper[BuildStatusConfig] {
  val default = apply()

  implicit val jsonFormat = new Format[BuildStatusConfig] {
    override def reads(json: JsValue): JsResult[BuildStatusConfig] =
      JsSuccess(BuildStatusConfig(
        (json \ "labelFont").asOpt[String],
        (json \ "labelSize").asOpt[Int]))

    override def writes(buildStatusConfig: BuildStatusConfig): JsValue = JsObject(
      buildStatusConfig.labelFont.map("labelFont" -> JsString(_)).toSeq ++
        buildStatusConfig.labelSize.map("labelSize" -> JsNumber(_)).toSeq)
  }

  implicit val formMapping = mapping(
    "labelFont" -> optional(text),
    "labelSize" -> optional(number)
  )(apply)(unapply)
}
