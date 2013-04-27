package models.widgetConfigs

import play.api.libs.json._
import play.api.data.Forms._
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsNumber

case class BurndownConfig(chartBackground: Option[String] = None,
                          plotBackground: Option[String] = None,
                          titleSize: Option[Int] = None,
                          tickSize: Option[Int] = None,
                          labelSize: Option[Int] = None,
                          lineWidth: Option[Int] = None) extends WidgetConfig

object BurndownConfig extends WidgetConfigMapper[BurndownConfig] {
  val default = apply()

  implicit val jsonFormat = new Format[BurndownConfig] {
    override def reads(json: JsValue): JsResult[BurndownConfig] =
      JsSuccess(BurndownConfig(
        (json \ "chartBackground").asOpt[String],
        (json \ "plotBackground").asOpt[String],
        (json \ "titleSize").asOpt[Int],
        (json \ "tickSize").asOpt[Int],
        (json \ "labelSize").asOpt[Int],
        (json \ "lineWidth").asOpt[Int]))

    override def writes(burndownConfig: BurndownConfig): JsValue = JsObject(
      burndownConfig.chartBackground.map("chartBackground" -> JsString(_)).toSeq ++
        burndownConfig.chartBackground.map("plotBackground" -> JsString(_)).toSeq ++
        burndownConfig.titleSize.map("titleSize" -> JsNumber(_)).toSeq ++
        burndownConfig.tickSize.map("tickSize" -> JsNumber(_)).toSeq ++
        burndownConfig.labelSize.map("labelSize" -> JsNumber(_)).toSeq ++
        burndownConfig.lineWidth.map("lineWidth" -> JsNumber(_)))
  }

  implicit val formMapping = mapping(
    "chartBackground" -> optional(text),
    "plotBackground" -> optional(text),
    "titleSize" -> optional(number),
    "tickSize" -> optional(number),
    "labelSize" -> optional(number),
    "lineWidth" -> optional(number)
  )(apply)(unapply)

}
