package controllers.widgets

import play.api.Play.current
import play.api.data.Forms._
import models.widgetConfigs.SprintTitleConfig
import models.{Team, Sprint, DisplayItem, Display}
import play.api.templates.Html
import models.utils.{AtomState, DataDigest}
import xml.NodeSeq
import play.api.mvc.RequestHeader
import play.api.cache.Cache
import org.joda.time.format.ISODateTimeFormat

object SprintTitle extends Widget[SprintTitleConfig] {
  def renderHtml(display: Display, displayItem: DisplayItem): Html = {
    (for {
      sprintId <- getSprintId(display, displayItem)
      sprint <- Sprint.findById(sprintId)
    } yield {
      views.html.display.widgets.sprintTitle.render(display, displayItem, sprint)
    }).getOrElse(Html(""))
  }

  override def renderAtom(display: Display, displayItem: DisplayItem)
                         (implicit request: RequestHeader): (NodeSeq, Long) = {
    (for {
      sprintId <- getSprintId(display, displayItem)
      sprint <- Sprint.findById(sprintId)
    } yield {
      val html = renderHtml(display, displayItem)
      val dateFormat = ISODateTimeFormat.dateTime().withZoneUTC()
      val lastUpdate = atomLastUpdate(display, displayItem, html)
      (<entry>
        <title>
          {"Sprint %d: %s".format(sprint.num, sprint.title)}
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
      </entry>, lastUpdate)
    }).getOrElse((NodeSeq.Empty, 0L))
  }

  private def getSprintId(display: Display, displayItem: DisplayItem): Option[Long] = {
    val teamIdOpt = displayItem.teamId.map(Some(_)).getOrElse(display.teamId)

    for {
      teamId <- teamIdOpt
      team <- Team.findById(teamId)
      sprintId <- team.currentSprintId
    } yield sprintId
  }

  private def atomLastUpdate(display: Display, displayItem: DisplayItem, html: Html): Long = {
    val key = "SprintTitle-%d-%d".format(display.id.get, displayItem.id.get)
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
