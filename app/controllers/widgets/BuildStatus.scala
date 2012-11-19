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

  def render(display: Display, displayItem: DisplayItem): Html = {
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

  override def etag(display: Display, displayItem: DisplayItem): String = {
    val dataDigest = DataDigest()

    dataDigest.update(displayItem.posx)
    dataDigest.update(displayItem.posy)
    dataDigest.update(displayItem.width)
    dataDigest.update(displayItem.height)
    dataDigest.update(displayItem.styleNum)
    dataDigest.update(displayItem.widgetConfigJson)

    dataDigest.update(display.projectId)
    display.projectId.map {
      projectId =>
        StatusMonitor.finaAllForProject(projectId, Seq(StatusMonitorTypes.Jenkins, StatusMonitorTypes.Teamcity)).foreach {
          statusMonitor =>
            dataDigest.update(statusMonitor.id)
            dataDigest.update(statusMonitor.active)
            StatusValue.findLastForStatusMonitor(statusMonitor.id.get).foreach {
              statusValue =>
                dataDigest.update(statusValue.id)
                dataDigest.update(statusValue.statusNum)
            }
        }
    }

    dataDigest.base64Digest()
  }
}
