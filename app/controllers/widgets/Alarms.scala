package controllers.widgets

import play.api.Play.current
import play.api.data.Forms._
import models.widgetConfigs.AlarmsConfig
import models.{Alarm, DisplayItem, Display}
import play.api.templates.Html
import org.joda.time.DateTime
import play.api.libs.concurrent.Akka
import globals.Global
import xml.NodeSeq
import org.joda.time.format.ISODateTimeFormat
import play.api.cache.Cache
import models.utils.{AtomState, DataDigest}
import play.api.mvc.RequestHeader
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits._

object Alarms extends Widget[AlarmsConfig] {
  override def renderHtml(display: Display, displayItem: DisplayItem): Html = {
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

  override def renderAtom(display: Display, displayItem: DisplayItem)
                         (implicit request: RequestHeader): (NodeSeq, Long) = {
    val alertPeriod = displayItem.alarmsConfig.flatMap(_.alertPeriod).getOrElse(5) * 60L * 1000L
    val alarms = findAllPendingForToday(alertPeriod)
    val html = renderHtml(display, displayItem)
    val dateFormat = ISODateTimeFormat.dateTime().withZoneUTC()
    val lastUpdate = atomLastUpdate(display, displayItem, html)

    (<entry>
      <title>
        {alarms.headOption.map(nextAlarm => "Next Alarm: %s".format(nextAlarm._1.name)).getOrElse("No more alarms for today")}
      </title>
      <id>
        {controllers.routes.DisplayItems.show(display.id.get, displayItem.id.get).absoluteURL()}
      </id>
      <link href={controllers.routes.DisplayItems.show(display.id.get, displayItem.id.get).absoluteURL()}></link>
      <updated>
        {dateFormat.print(lastUpdate)}
      </updated>
      <content type="html">
        {html}
      </content>
    </entry>, lastUpdate)
  }

  private def findAllPendingForToday(alertPeriod: Long): Seq[(Alarm, Boolean)] = {

    val alarms = Alarm.findAllForToday()
    val now = System.currentTimeMillis()

    alarms.filter(_.nextDate.getTime < now - alertPeriod).foreach {
      alarm =>
        alarm.repeatDays.map {
          repeatDays =>
            val nextDate = new DateTime(alarm.nextDate.getTime).plusDays(repeatDays)
            val newAlarm = Alarm(alarm.id, alarm.name, nextDate.toDate, alarm.durationMins, Some(repeatDays))
            newAlarm.update
        }
    }
    alarms.filter(_.nextDate.getTime >= now - alertPeriod).map {
      alarm =>
        (alarm, alarm.nextDate.getTime < now + alertPeriod)
    }
  }

  private def atomLastUpdate(display: Display, displayItem: DisplayItem, html: Html): Long = {
    val key = "Alarm-%d-%d".format(display.id.get, displayItem.id.get)
    val etag = DataDigest.etag(html)
    var state = Cache.getOrElse(key) {
      AtomState(etag, System.currentTimeMillis())
    }
    if (state.etag != etag) {
      state = AtomState(etag, System.currentTimeMillis())
      Cache.set(key, state)
    }
    state.lastUpdate
  }
}
