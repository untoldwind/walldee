import actors.{Backup, StatusMonitorUpdater}
import akka.actor.Props
import play.api.Play.current
import play.api.libs.concurrent.Akka
import play.api.{Application, GlobalSettings}

import akka.util.duration._

object Global extends GlobalSettings {
  override def onStart(app: Application) {
    val backup = Akka.system.actorOf(Props(new Backup))

    Akka.system.scheduler.schedule(1 minutes, 1 hour, backup, Backup.CheckBackup())

    val statusMonitorUpdater = Akka.system.actorOf(Props(new StatusMonitorUpdater))

    Akka.system.scheduler.schedule(30 seconds, 30 seconds, statusMonitorUpdater, StatusMonitorUpdater.UpdateAll())
  }
}
