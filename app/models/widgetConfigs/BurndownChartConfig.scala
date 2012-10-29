package models.widgetConfigs

import play.api.libs.json._
import play.api.libs.json.JsObject
import play.api.libs.json.JsString

case class BurndownChartConfig(chartBackground: Option[String] = None,
                               plotBackground: Option[String] = None,
                               titleSize: Option[Int] = None,
                               tickSize: Option[Int] = None,
                               labelSize: Option[Int] = None,
                               lineWidth: Option[Int] = None)

object BurndownChartConfig {

  implicit object BurndownChartConfigFormat extends Format[BurndownChartConfig] {
    override def reads(json: JsValue): BurndownChartConfig =
      BurndownChartConfig(
        (json \ "chartBackground").asOpt[String],
        (json \ "plotBackground").asOpt[String],
        (json \ "titleSize").asOpt[Int],
        (json \ "tickSize").asOpt[Int],
        (json \ "labelSize").asOpt[Int],
        (json \ "lineWidth").asOpt[Int])

    override def writes(burndownChartConfig: BurndownChartConfig): JsValue = JsObject(
      burndownChartConfig.chartBackground.map("chartBackground" -> JsString(_)).toSeq ++
        burndownChartConfig.chartBackground.map("plotBackground" -> JsString(_)).toSeq ++
        burndownChartConfig.titleSize.map("titleSize" -> JsNumber(_)).toSeq ++
        burndownChartConfig.tickSize.map("tickSize" -> JsNumber(_)).toSeq ++
        burndownChartConfig.labelSize.map("labelSize" -> JsNumber(_)).toSeq ++
        burndownChartConfig.lineWidth.map("lineWidth" -> JsNumber(_)))
  }

}
