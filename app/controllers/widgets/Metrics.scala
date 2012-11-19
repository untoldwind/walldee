package controllers.widgets

import models.widgetConfigs.{MetricsItemTypes, MetricsItem, ClockConfig, MetricsConfig}
import play.api.data.Forms._
import models.statusValues.MetricSeverityTypes
import models._
import scala.Some
import play.api.templates.Html

object Metrics extends Widget[MetricsConfig] {
  def itemTypeMapping = number.transform[MetricsItemTypes.Type](
    id => MetricsItemTypes(id),
    itemType => itemType.id
  )

  def severityMapping = mapping(
    "Blocker" -> boolean,
    "Critical" -> boolean,
    "Major" -> boolean,
    "Minor" -> boolean,
    "Info" -> boolean
  ) {
    (blocker, critical, major, minor, info) =>
      MetricSeverityTypes.values
      val severities = Seq.newBuilder[MetricSeverityTypes.Type]
      if (blocker)
        severities += MetricSeverityTypes.Blocker
      if (critical)
        severities += MetricSeverityTypes.Critical
      if (major)
        severities += MetricSeverityTypes.Major
      if (minor)
        severities += MetricSeverityTypes.Minor
      if (info)
        severities += MetricSeverityTypes.Info
      severities.result()
  } {
    severities =>
      Some(
        severities.exists(_ == MetricSeverityTypes.Blocker),
        severities.exists(_ == MetricSeverityTypes.Critical),
        severities.exists(_ == MetricSeverityTypes.Major),
        severities.exists(_ == MetricSeverityTypes.Minor),
        severities.exists(_ == MetricSeverityTypes.Info)
      )
  }

  def metricsItemMapping = mapping(
    "itemType" -> itemTypeMapping,
    "labelSize" -> optional(number),
    "severities" -> severityMapping
  )(MetricsItem.apply)(MetricsItem.unapply)

  def configMapping = mapping(
    "labelFont" -> optional(text),
    "columns" -> optional(number),
    "items" -> seq(metricsItemMapping)
  )(MetricsConfig.apply)(MetricsConfig.unapply)

  def render(display: Display, displayItem: DisplayItem) = {
    display.projectId.map {
      projectId =>
        var statusMonitors = StatusMonitor.finaAllForProject(projectId, Seq(StatusMonitorTypes.Sonar))
        var statusMonitorsWithValues = statusMonitors.map {
          statusMonitor =>
            (statusMonitor,
              StatusValue.findLastForStatusMonitor(statusMonitor.id.get))
        }
        views.html.display.widgets.metrics(display, displayItem, statusMonitorsWithValues)
    }.getOrElse(Html(""))
  }
}
