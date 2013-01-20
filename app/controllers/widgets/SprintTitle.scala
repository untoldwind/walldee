package controllers.widgets

import play.api.Play.current
import play.api.data.Forms._
import models.widgetConfigs.SprintTitleConfig
import models.{Sprint, DisplayItem, Display}
import play.api.templates.Html
import models.utils.{AtomState, DataDigest}
import xml.NodeSeq
import play.api.mvc.RequestHeader
import play.api.cache.Cache
import org.joda.time.format.ISODateTimeFormat

object SprintTitle extends Widget[SprintTitleConfig] {
  val configMapping = mapping(
    "labelFont" -> optional(text),
    "labelSize" -> optional(number)
  )(SprintTitleConfig.apply)(SprintTitleConfig.unapply)

  def renderHtml(display: Display, displayItem: DisplayItem): Html = {
    Sprint.findById(display.sprintId).map {
      sprint =>
        views.html.display.widgets.sprintTitle.render(display, displayItem, sprint)
    }.getOrElse(Html(""))
  }

  override def renderAtom(display: Display, displayItem: DisplayItem)
                         (implicit request: RequestHeader): (NodeSeq, Long) = {
    Sprint.findById(display.sprintId).map {
      sprint =>
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
    }.getOrElse((NodeSeq.Empty, 0L))
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
