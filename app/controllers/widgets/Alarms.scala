package controllers.widgets

import play.api.data.Forms._
import models.widgetConfigs.AlarmsConfig

object Alarms {
  val configMapping = mapping(
    "labelFont" -> optional(text),
    "labelSize" -> optional(number)
  )(AlarmsConfig.apply)(AlarmsConfig.unapply)

}
