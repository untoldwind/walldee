package controllers.widgets

import models.widgetConfigs.{SprintTitleConfig, BuildStatusConfig}
import play.api.data.Forms._
import models._
import play.api.templates.Html
import utils.DataDigest

object BuildStatus extends Widget[BuildStatusConfig] {
  val configMapping = mapping(
    "labelFont" -> optional(text),
    "labelSize" -> optional(number)
  )(BuildStatusConfig.apply)(BuildStatusConfig.unapply)

  def renderHtml(display: Display, displayItem: DisplayItem): Html = {
    display.projectId.map {
      projectId =>
        var statusMonitors = StatusMonitor.finaAllForProject(projectId, Seq(StatusMonitorTypes.Jenkins, StatusMonitorTypes.Teamcity))
        var statusMonitorsWithStatus = statusMonitors.map {
          statusMonitor =>
            (statusMonitor,
              StatusValue.findLastForStatusMonitor(statusMonitor.id.get).map(_.status).getOrElse(StatusTypes.Unknown))
        }
        views.html.display.widgets.buildStatus(display, displayItem, statusMonitorsWithStatus)
    }.getOrElse(Html(""))
  }
}
