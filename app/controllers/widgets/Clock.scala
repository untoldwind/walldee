package controllers.widgets

import play.api.Play.current
import play.api.data.Forms._
import models.widgetConfigs.ClockConfig
import models.{DisplayItem, Display}
import play.api.templates.Html
import globals.Global
import scala.concurrent.duration._
import play.libs.Akka

object Clock extends Widget[ClockConfig] {
  implicit val executor = Akka.system.dispatcher

  val configMapping = mapping(
    "labelFont" -> optional(text),
    "labelSize" -> optional(number)
  )(ClockConfig.apply)(ClockConfig.unapply)

  def renderHtml(display: Display, displayItem: DisplayItem): Html = {
    val next = 60L * 1000L + 50 - (System.currentTimeMillis() % (60L * 1000))
    Akka.system.scheduler.scheduleOnce(next millis, Global.displayUpdater, displayItem)
    views.html.display.widgets.clock.render(display, displayItem)
  }
}
