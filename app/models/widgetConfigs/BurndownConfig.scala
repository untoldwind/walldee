package models.widgetConfigs

import play.api.libs.json._
import play.api.libs.json.JsObject
import play.api.libs.json.JsString

case class BurndownConfig(chartBackground: Option[String] = None,
                               plotBackground: Option[String] = None,
                               titleSize: Option[Int] = None,
                               tickSize: Option[Int] = None,
                               labelSize: Option[Int] = None,
                               lineWidth: Option[Int] = None)

object BurndownConfig {

  implicit object BurndownChartConfigFormat extends Format[BurndownConfig] {
    override def reads(json: JsValue): BurndownConfig =
      BurndownConfig(
        (json \ "chartBackground").asOpt[String],
        (json \ "plotBackground").asOpt[String],
        (json \ "titleSize").asOpt[Int],
        (json \ "tickSize").asOpt[Int],
        (json \ "labelSize").asOpt[Int],
        (json \ "lineWidth").asOpt[Int])

    override def writes(burndownConfig: BurndownConfig): JsValue = JsObject(
      burndownConfig.chartBackground.map("chartBackground" -> JsString(_)).toSeq ++
        burndownConfig.chartBackground.map("plotBackground" -> JsString(_)).toSeq ++
        burndownConfig.titleSize.map("titleSize" -> JsNumber(_)).toSeq ++
        burndownConfig.tickSize.map("tickSize" -> JsNumber(_)).toSeq ++
        burndownConfig.labelSize.map("labelSize" -> JsNumber(_)).toSeq ++
        burndownConfig.lineWidth.map("lineWidth" -> JsNumber(_)))
  }

}
