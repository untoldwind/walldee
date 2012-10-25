package models.widgetConfigs

import play.api.libs.json._
import models.json.SprintCounter
import play.api.libs.json.JsObject
import play.api.libs.json.JsString

case class BurndownChartConfig(chartBackground: Option[String],
                               plotBackground: Option[String],
                               fontSize: Option[Int])

object BurndownChartConfig {

  implicit object BurndownChartConfigFormat extends Format[BurndownChartConfig] {
    override def reads(json: JsValue): BurndownChartConfig =
      BurndownChartConfig(
        (json \ "chartBackground").asOpt[String],
        (json \ "plotBackground").asOpt[String],
        (json \ "fontSize").asOpt[Int])

    override def writes(burndownChartConfig: BurndownChartConfig): JsValue = JsObject(
      burndownChartConfig.chartBackground.map("chartBackground" -> JsString(_)).toSeq ++
        burndownChartConfig.chartBackground.map("plotBackground" -> JsString(_)).toSeq ++
        burndownChartConfig.fontSize.map("fontSize" -> JsNumber(_)).toSeq)
  }

}
