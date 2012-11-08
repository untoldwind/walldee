import actors.StatusMonitorUpdater
import akka.actor.Props
import play.api.Play.current
import play.api.libs.concurrent.Akka
import play.api.{Application, GlobalSettings}

import scala.concurrent.duration._

object Global extends GlobalSettings {
  override def onStart(app: Application) {
    val statusMonitorUpdater = Akka.system.actorOf(Props(new StatusMonitorUpdater))

    val system = Akka.system

    import system.dispatcher

    Akka.system.scheduler.schedule(30 seconds, 30 seconds, statusMonitorUpdater, StatusMonitorUpdater.UpdateAll())
  }
}
