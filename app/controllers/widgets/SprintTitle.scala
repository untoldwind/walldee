package controllers.widgets

import play.api.data.Forms._
import models.widgetConfigs.SprintTitleConfig
import models.{Sprint, DisplayItem, Display}
import play.api.templates.Html
import models.utils.DataDigest

object SprintTitle extends Widget[SprintTitleConfig] {
  val configMapping = mapping(
    "labelFont" -> optional(text),
    "labelSize" -> optional(number)
  )(SprintTitleConfig.apply)(SprintTitleConfig.unapply)

  def render(display: Display, displayItem: DisplayItem): Html = {
    Sprint.findById(display.sprintId).map {
      sprint =>
        views.html.display.widgets.sprintTitle.render(display, displayItem, sprint)
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

    Sprint.findById(display.sprintId).map {
      sprint =>
        dataDigest.update(sprint.title)
    }
    dataDigest.base64Digest()
  }
}
