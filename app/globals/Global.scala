package globals

import actors.{DisplayUpdater, Backup, StatusMonitorUpdater}
import akka.actor.Props
import play.api.Play.current
import play.api.libs.concurrent.Akka
import play.api.{Application, GlobalSettings}
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits._
import org.h2.tools.Server
import akka.event.slf4j.SLF4JLogging

object Global extends GlobalSettings with SLF4JLogging {
  lazy val displayUpdater = Akka.system.actorOf(Props(new DisplayUpdater))

  lazy val backup = Akka.system.actorOf(Props(new Backup))

  lazy val statusMonitorUpdater = Akka.system.actorOf(Props(new StatusMonitorUpdater))

  var h2WebServer: Option[Server] = None
  var h2TcpServer: Option[Server] = None

  override def onStart(app: Application) {

    Akka.system.scheduler.schedule(1.minutes, 1.hour, backup, Backup.CheckBackup())

    Akka.system.scheduler.schedule(30.seconds, 30.seconds, statusMonitorUpdater, StatusMonitorUpdater.UpdateAll())

    app.configuration.getConfig("h2.server").foreach {
      h2ServerConfig =>
        if (h2ServerConfig.getBoolean("start").getOrElse(false)) {
          try {
            h2ServerConfig.getInt("web.port").foreach {
              webPort =>
                h2WebServer = Some(Server.createWebServer("-webPort", webPort.toString))

            }
            h2ServerConfig.getInt("tcp.port").foreach {
              tcpPort =>
                h2TcpServer = Some(Server.createTcpServer("-tcpPort", tcpPort.toString))
            }
          } catch {
            case e: Exception =>
              log.error("Failed to start h2 service", e)
          }
        }
    }
    h2WebServer.foreach(_.start())
    h2TcpServer.foreach(_.start())
  }

  override def onStop(app: Application) {
    h2WebServer.foreach(_.stop())
    h2TcpServer.foreach(_.stop())

  }
}
