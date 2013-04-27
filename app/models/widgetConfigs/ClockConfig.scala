package models.widgetConfigs

import play.api.libs.json._
import play.api.data.Forms._
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsNumber

case class ClockConfig(labelFont: Option[String] = None,
                       labelSize: Option[Int] = None) extends WidgetConfig

object ClockConfig extends WidgetConfigMapper[ClockConfig] {
  val default = apply()

  implicit val jsonFormat = new Format[ClockConfig] {
    override def reads(json: JsValue): JsResult[ClockConfig] =
      JsSuccess(ClockConfig(
        (json \ "labelFont").asOpt[String],
        (json \ "labelSize").asOpt[Int]))

    override def writes(clockConfig: ClockConfig): JsValue = JsObject(
      clockConfig.labelFont.map("labelFont" -> JsString(_)).toSeq ++
        clockConfig.labelSize.map("labelSize" -> JsNumber(_)).toSeq)
  }

  implicit val formMapping = mapping(
    "labelFont" -> optional(text),
    "labelSize" -> optional(number)
  )(apply)(unapply)

}
