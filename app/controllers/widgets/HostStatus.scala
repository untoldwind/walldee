package controllers.widgets

import play.api.Play.current
import models.widgetConfigs.HostStatusConfig
import play.api.data.Forms._
import models._
import play.api.templates.Html
import util.matching.Regex
import utils.{AtomState, DataDigest}
import xml.NodeSeq
import play.api.mvc.RequestHeader
import play.api.cache.Cache
import org.joda.time.format.ISODateTimeFormat

object HostStatus extends Widget[HostStatusConfig] {
  override def renderHtml(display: Display, displayItem: DisplayItem): Html = {
    val projectIdOpt = displayItem.projectId.map(Some(_)).getOrElse(display.projectId)
    projectIdOpt.map {
      projectId =>
        val statusMonitorsWithValues = getStatusMonitorsWithValues(projectId)
        views.html.display.widgets.hostStatus(display, displayItem, projectId, statusMonitorsWithValues)
    }.getOrElse(Html(""))
  }

  override def renderAtom(display: Display, displayItem: DisplayItem)
                         (implicit request: RequestHeader): (NodeSeq, Long) = {
    val projectIdOpt = displayItem.projectId.map(Some(_)).getOrElse(display.projectId)
    projectIdOpt.map {
      projectId =>
        val statusMonitorsWithValues = getStatusMonitorsWithValues(projectId)
        val html = renderHtml(display, displayItem)
        val dateFormat = ISODateTimeFormat.dateTime().withZoneUTC()
        val lastUpdate = atomLastUpdate(display, displayItem, html)
        val title = "Host status: " + statusMonitorsWithValues.map {
          case (statusMonitor, values) =>
            statusMonitor.name
        }.mkString(", ")

        (<entry>
          <title>
            {title}
          </title>
          <id>
            {controllers.routes.DisplayItems.show(display.id.get, displayItem.id.get).absoluteURL()}
          </id>
          <link href={controllers.routes.DisplayItems.show(display.id.get, displayItem.id.get).absoluteURL()}></link>
          <updated>
            {dateFormat.print(lastUpdate)}
          </updated>
          <content type="html">
            {html}
          </content>
        </entry>, 0L)
    }.getOrElse((NodeSeq.Empty, 0L))
  }

  private def getStatusMonitorsWithValues(projectId: Long): Seq[(StatusMonitor, Option[StatusValue])] = {
    val statusMonitors = StatusMonitor.finaAllForProject(projectId, Seq(StatusMonitorTypes.Icinga))
    statusMonitors.map {
      statusMonitor =>
        (statusMonitor,
          StatusValue.findLastForStatusMonitor(statusMonitor.id.get))
    }
  }

  private def atomLastUpdate(display: Display, displayItem: DisplayItem, html: Html): Long = {
    val key = "HostStatus-%d-%d".format(display.id.get, displayItem.id.get)
    val etag = DataDigest.etag(html)
    var state = Cache.getOrElse(key) {
      AtomState(etag, System.currentTimeMillis())
    }
    if (state.etag != etag) {
      state = AtomState(etag, System.currentTimeMillis())
      Cache.set(key, state)
    }
    state.lastUpdate
  }
}
