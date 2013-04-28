package models

import models.widgetConfigs._
import play.api.libs.json.Json

object DisplayWidgets extends Enumeration {
  type Type = Value
  val Burndown = Widget(BurndownConfig)
  val SprintTitle = Widget(SprintTitleConfig)
  val Clock = Widget(ClockConfig)
  val Alarms = Widget(AlarmsConfig)
  val IFrame = Widget(IFrameConfig)
  val BuildStatus = Widget(BuildStatusConfig)
  val HostStatus = Widget(HostStatusConfig)
  val Metrics = Widget(MetricsConfig)

  implicit def valueToWidget(x: Value): Widget[WidgetConfig] = x.asInstanceOf[Widget[WidgetConfig]]

  case class Widget[T <: WidgetConfig](configMappger: WidgetConfigMapper[T]) extends Val(nextId, null) {

    def configToJson(widgetConfig: T) = {
      val jsValue = Json.toJson(widgetConfig)(configMappger.jsonFormat)
      Json.stringify(jsValue)
    }

    def jsonToConfig(json: String): T = {
      val jsValue = Json.parse(json)
      Json.fromJson[T](jsValue)(configMappger.jsonFormat).getOrElse(configMappger.default)
    }
  }
}
