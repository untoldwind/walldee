package controllers.widgets

import play.api.data.Forms._
import models.widgetConfigs.ClockConfig
import models.{DisplayItem, Display}
import play.api.templates.Html

object Clock {
  val configMapping = mapping(
    "labelFont" -> optional(text),
    "labelSize" -> optional(number)
  )(ClockConfig.apply)(ClockConfig.unapply)

  def render(display: Display, displayItem: DisplayItem): Html = {
    views.html.display.widgets.clock.render(display, displayItem)
  }
}
