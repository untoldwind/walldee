package controllers.widgets

import play.api.data.Forms._
import models.widgetConfigs.ClockConfig
import models.{Alarm, DisplayItem, Display}
import play.api.templates.Html
import models.utils.DataDigest

object Clock extends Widget[ClockConfig] {
  val configMapping = mapping(
    "labelFont" -> optional(text),
    "labelSize" -> optional(number)
  )(ClockConfig.apply)(ClockConfig.unapply)

  def render(display: Display, displayItem: DisplayItem): Html = {
    views.html.display.widgets.clock.render(display, displayItem)
  }

  override def etag(display: Display, displayItem: DisplayItem): String = {
    val dataDigest = DataDigest()

    dataDigest.update(displayItem.posx)
    dataDigest.update(displayItem.posy)
    dataDigest.update(displayItem.width)
    dataDigest.update(displayItem.height)
    dataDigest.update(displayItem.styleNum)
    dataDigest.update(displayItem.widgetConfigJson)

    val format = org.joda.time.format.DateTimeFormat.forPattern("HH:mm")
    dataDigest.update(format.print(new org.joda.time.DateTime()))

    dataDigest.base64Digest()
  }
}
