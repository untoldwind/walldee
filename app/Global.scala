import actors.{StatusMonitorCrawler, StatusMonitorUpdater}
import akka.actor.Props
import play.api.Play.current
import play.api.libs.concurrent.Akka
import play.api.{Application, GlobalSettings}

import akka.util.duration._

object Global extends GlobalSettings {
  override def onStart(app: Application) {
    val statusMonitorCrawler = Akka.system.actorOf(Props(new StatusMonitorCrawler))
    val statusMonitorUpdater = Akka.system.actorOf(Props(new StatusMonitorUpdater(statusMonitorCrawler)))

    Akka.system.scheduler.schedule(30 seconds, 1 minute, statusMonitorUpdater, StatusMonitorUpdater.UpdateAll())
  }
}
