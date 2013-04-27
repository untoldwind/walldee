package models.widgetConfigs

import play.api.libs.json.Format
import play.api.data.Mapping

trait WidgetConfig

trait WidgetConfigMapper[T <: WidgetConfig] {
  def default: T

  def jsonFormat: Format[T]

  def formMapping: Mapping[T]
}