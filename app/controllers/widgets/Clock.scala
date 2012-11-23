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

  def renderHtml(display: Display, displayItem: DisplayItem): Html = {
    views.html.display.widgets.clock.render(display, displayItem)
  }
}
