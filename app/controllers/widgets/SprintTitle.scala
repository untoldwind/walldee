package controllers.widgets

import play.api.data.Forms._
import models.widgetConfigs.SprintTitleConfig

object SprintTitle {
  val configMapping = mapping(
    "labelSize" -> optional(number)
  )(SprintTitleConfig.apply)(SprintTitleConfig.unapply)
}
