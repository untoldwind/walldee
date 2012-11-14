package controllers.widgets

import play.api.data.Forms._
import models.widgetConfigs.AlarmsConfig
import models.{Alarm, DisplayItem, Display}
import play.api.templates.Html
import java.security.MessageDigest
import models.utils.DataDigest
import org.joda.time.DateTime

object Alarms extends Widget[AlarmsConfig] {
  val configMapping = mapping(
    "labelFont" -> optional(text),
    "labelSize" -> optional(number),
    "descriptionFont" -> optional(text),
    "descriptionSize" -> optional(number),
    "alertPeriod" -> optional(number)
  )(AlarmsConfig.apply)(AlarmsConfig.unapply)

  def render(display: Display, displayItem: DisplayItem): Html = {
    views.html.display.widgets.alarms.render(display, displayItem,
      findAllPendingForToday(displayItem.alarmsConfig.flatMap(_.alertPeriod)))
  }

  override def etag(display: Display, displayItem: DisplayItem): String = {
    val dataDigest = DataDigest()

    dataDigest.update(displayItem.posx)
    dataDigest.update(displayItem.posy)
    dataDigest.update(displayItem.width)
    dataDigest.update(displayItem.height)
    dataDigest.update(displayItem.widgetConfigJson)

    findAllPendingForToday(displayItem.alarmsConfig.flatMap(_.alertPeriod)).foreach {
      case (alarm, alert) =>
        dataDigest.update(alert)
        dataDigest.update(alarm.id)
        dataDigest.update(alarm.name)
        dataDigest.update(alarm.repeatDays)
    }
    dataDigest.base64Digest()
  }

  private def findAllPendingForToday(alarmPeriod: Option[Int]): Seq[(Alarm, Boolean)] = {

    val alarms = Alarm.findAllForToday()
    val period = alarmPeriod.getOrElse(5) * 60L * 1000L
    val now = System.currentTimeMillis()

    alarms.filter(_.nextDate.getTime < now - period).foreach {
      alarm =>
        alarm.repeatDays.map {
          repeatDays =>
            val nextDate = new DateTime(alarm.nextDate.getTime).plusDays(repeatDays)
            val newAlarm = Alarm(alarm.id, alarm.name, nextDate.toDate, Some(repeatDays))
            newAlarm.update
        }
    }
    alarms.filter(_.nextDate.getTime >= now - period).map {
      alarm =>
        (alarm, alarm.nextDate.getTime < now + period)
    }
  }
}
