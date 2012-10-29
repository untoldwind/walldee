package controllers.widgets

import play.api.data.Forms._
import models.widgetConfigs.ClockConfig

object Clock {
  val configMapping = mapping(
    "labelFont" -> optional(text),
    "labelSize" -> optional(number)
  )(ClockConfig.apply)(ClockConfig.unapply)
}
