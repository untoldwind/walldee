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

  case class Widget[T <: WidgetConfig](configMappger: WidgetConfigMapper[T]) extends Val(nextId, null)

  def configToJson(displayWidget: Type, widgetConfig: WidgetConfig) = {
    val jsValue = Json.toJson(widgetConfig)(displayWidget.configMappger.jsonFormat)
    Json.stringify(jsValue)
  }

  def jsonToConfig(displayWidget: Type, json: String): Option[WidgetConfig] = {
    val jsValue = Json.parse(json)
    Json.fromJson[WidgetConfig](jsValue)(displayWidget.configMappger.jsonFormat).asOpt
  }
}
