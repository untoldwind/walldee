package controllers.widgets

import models.widgetConfigs.{HostStatusConfig, BuildStatusConfig}
import play.api.data.Forms._
import models._
import play.api.templates.Html
import util.matching.Regex
import utils.DataDigest

object HostStatus extends Widget[HostStatusConfig] {
  val regexMapping = text.transform[Regex](
    str => str.r,
    regex => regex.toString
  )

  val configMapping = mapping(
    "titleFont" -> optional(text),
    "titleSize" -> optional(number),
    "labelFont" -> optional(text),
    "labelSize" -> optional(number),
    "columns" -> optional(number),
    "hostNamePattern" -> optional(regexMapping)
  )(HostStatusConfig.apply)(HostStatusConfig.unapply)

  def renderHtml(display: Display, displayItem: DisplayItem): Html = {
    val projectIdOpt = displayItem.projectId.map(Some(_)).getOrElse(display.projectId)
    projectIdOpt.map {
      projectId =>
        var statusMonitors = StatusMonitor.finaAllForProject(projectId, Seq(StatusMonitorTypes.Icinga))
        var statusMonitorsWithValues = statusMonitors.map {
          statusMonitor =>
            (statusMonitor,
              StatusValue.findLastForStatusMonitor(statusMonitor.id.get))
        }
        views.html.display.widgets.hostStatus(display, displayItem, projectId, statusMonitorsWithValues)
    }.getOrElse(Html(""))
  }
}
