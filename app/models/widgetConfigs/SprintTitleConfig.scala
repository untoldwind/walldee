package models.widgetConfigs

import play.api.libs.json._
import play.api.data.Forms._
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsNumber

case class SprintTitleConfig(labelFont: Option[String] = None,
                             labelSize: Option[Int] = None) extends WidgetConfig

object SprintTitleConfig extends WidgetConfigMapper[SprintTitleConfig] {
  val default = apply()

  implicit val jsonFormat = new Format[SprintTitleConfig] {
    override def reads(json: JsValue): JsResult[SprintTitleConfig] =
      JsSuccess(SprintTitleConfig(
        (json \ "labelFont").asOpt[String],
        (json \ "labelSize").asOpt[Int]))

    override def writes(sprintTitleConfig: SprintTitleConfig): JsValue = JsObject(
      sprintTitleConfig.labelFont.map("labelFont" -> JsString(_)).toSeq ++
        sprintTitleConfig.labelSize.map("labelSize" -> JsNumber(_)).toSeq)
  }

  implicit val formMapping = mapping(
    "labelFont" -> optional(text),
    "labelSize" -> optional(number)
  )(apply)(unapply)
}