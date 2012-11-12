package controllers.widgets

import play.api.data.Forms._
import models.widgetConfigs.AlarmsConfig
import models.{Alarm, DisplayItem, Display}
import play.api.templates.Html
import java.security.MessageDigest
import models.utils.DataDigest

object Alarms extends Widget[AlarmsConfig] {
  val configMapping = mapping(
    "labelFont" -> optional(text),
    "labelSize" -> optional(number)
  )(AlarmsConfig.apply)(AlarmsConfig.unapply)

  def render(display: Display, displayItem: DisplayItem): Html = {
    views.html.display.widgets.alarms.render(display, displayItem, Alarm.findAllForToday())
  }

  override def etag(display: Display, displayItem: DisplayItem): String = {
    val dataDigest = DataDigest()

    dataDigest.update(displayItem.posx)
    dataDigest.update(displayItem.posy)
    dataDigest.update(displayItem.width)
    dataDigest.update(displayItem.height)
    dataDigest.update(displayItem.widgetConfigJson)

    Alarm.findAllForToday().foreach {
      alarm =>
        dataDigest.update(alarm.id)
        dataDigest.update(alarm.name)
        dataDigest.update(alarm.repeatDays)
    }
    dataDigest.base64Digest()
  }
}
