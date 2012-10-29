package controllers.widgets

import play.api.data.Forms._
import models.widgetConfigs.AlarmsConfig
import models.{Alarm, DisplayItem, Display}
import play.api.templates.Html

object Alarms {
  val configMapping = mapping(
    "labelFont" -> optional(text),
    "labelSize" -> optional(number)
  )(AlarmsConfig.apply)(AlarmsConfig.unapply)

  def render(display: Display, displayItem: DisplayItem): Html = {
    views.html.display.widgets.alarms.render(display, displayItem, Alarm.findAllForToday())
  }
}
