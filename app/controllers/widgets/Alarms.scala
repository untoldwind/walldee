package controllers.widgets

import play.api.Play.current
import play.api.data.Forms._
import models.widgetConfigs.AlarmsConfig
import models.{Alarm, DisplayItem, Display}
import play.api.templates.Html
import org.joda.time.DateTime
import play.api.libs.concurrent.Akka
import akka.util.duration._
import globals.Global

object Alarms extends Widget[AlarmsConfig] {
  val configMapping = mapping(
    "labelFont" -> optional(text),
    "labelSize" -> optional(number),
    "descriptionFont" -> optional(text),
    "descriptionSize" -> optional(number),
    "alertPeriod" -> optional(number)
  )(AlarmsConfig.apply)(AlarmsConfig.unapply)

  def renderHtml(display: Display, displayItem: DisplayItem): Html = {
    val alertPeriod = displayItem.alarmsConfig.flatMap(_.alertPeriod).getOrElse(5) * 60L * 1000L
    val alarms = findAllPendingForToday(alertPeriod)
    val now = System.currentTimeMillis()
    val min = if (!alarms.isEmpty)
      alarms.map(_._1.nextDate.getTime).min - alertPeriod
    else
      (now + 24L * 3600L * 1000L) % (24L * 3600L * 1000L)

    if (min < now)
      Akka.system.scheduler.scheduleOnce(1 minute, Global.displayUpdater, displayItem)
    else
      Akka.system.scheduler.scheduleOnce((min - now + 50) millis, Global.displayUpdater, displayItem)

    views.html.display.widgets.alarms.render(display, displayItem, alarms)
  }

  private def findAllPendingForToday(alertPeriod: Long): Seq[(Alarm, Boolean)] = {

    val alarms = Alarm.findAllForToday()
    val now = System.currentTimeMillis()

    alarms.filter(_.nextDate.getTime < now - alertPeriod).foreach {
      alarm =>
        alarm.repeatDays.map {
          repeatDays =>
            val nextDate = new DateTime(alarm.nextDate.getTime).plusDays(repeatDays)
            val newAlarm = Alarm(alarm.id, alarm.name, nextDate.toDate, Some(repeatDays))
            newAlarm.update
        }
    }
    alarms.filter(_.nextDate.getTime >= now - alertPeriod).map {
      alarm =>
        (alarm, alarm.nextDate.getTime < now + alertPeriod)
    }
  }
}
