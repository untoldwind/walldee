package controllers.widgets

import models.widgetConfigs.{HostStatusConfig, BuildStatusConfig}
import play.api.data.Forms._
import models._
import play.api.templates.Html

object HostStatus extends Widget[HostStatusConfig]{
  val configMapping = mapping(
    "labelFont" -> optional(text),
    "labelSize" -> optional(number)
  )(HostStatusConfig.apply)(HostStatusConfig.unapply)

  def render(display: Display, displayItem: DisplayItem): Html = {
    display.projectId.map {
      projectId =>
        var statusMonitors = StatusMonitor.finaAllForProject(projectId, Seq(StatusMonitorTypes.Icinga))
        var statusMonitorsWithValues = statusMonitors.map {
          statusMonitor =>
            (statusMonitor,
              StatusValue.findLastForStatusMonitor(statusMonitor.id.get))
        }
        views.html.display.widgets.hostStatus(display, displayItem, statusMonitorsWithValues)
    }.getOrElse(Html(""))
  }
}
