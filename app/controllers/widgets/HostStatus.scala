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
        StatusMonitor.finaAllForProject(projectId, Seq(StatusMonitorTypes.Icinga)).foreach {
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
