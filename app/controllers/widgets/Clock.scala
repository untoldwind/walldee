package controllers.widgets

import play.api.Play.current
import play.api.data.Forms._
import models.widgetConfigs.ClockConfig
import models.{DisplayItem, Display}
import play.api.templates.Html
import play.api.libs.concurrent.Akka
import globals.Global
import akka.util.duration._
import xml.NodeSeq

object Clock extends Widget[ClockConfig] {
  val configMapping = mapping(
    "labelFont" -> optional(text),
    "labelSize" -> optional(number)
  )(ClockConfig.apply)(ClockConfig.unapply)

  override def renderHtml(display: Display, displayItem: DisplayItem): Html = {
    val next = 60L * 1000L + 50 - (System.currentTimeMillis() % (60L * 1000))
    Akka.system.scheduler.scheduleOnce(next millis, Global.displayUpdater, displayItem)
    views.html.display.widgets.clock.render(display, displayItem)
  }
}
