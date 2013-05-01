package actors

import akka.actor.Actor
import akka.event.slf4j.SLF4JLogging
import models.StatusMonitor
import actors.StatusMonitorUpdater.UpdateAll
import play.api.libs.ws.WS
import com.ning.http.client.Realm.AuthScheme
import play.api.libs.concurrent.Execution.Implicits._
import actors.monitorProcessors.MonitorProcessor

class StatusMonitorUpdater extends Actor with SLF4JLogging {
  def receive = {
    case UpdateAll() =>
      StatusMonitor.findAllActive.foreach {
        statusMonitor =>
          val processor = MonitorProcessor(statusMonitor.monitorType)
          val url = processor.apiUrl(statusMonitor.url)

          val wsRequest = if (statusMonitor.username.isDefined && statusMonitor.password.isDefined)
            WS.url(url).withAuth(statusMonitor.username.get, statusMonitor.password.get, AuthScheme.BASIC)
          else
            WS.url(url)

          statusMonitor.updateLastQueried

          wsRequest.withHeaders("Accept" -> processor.accepts).get().map {
            response =>
              processor.process(statusMonitor, response)
              statusMonitor.updateLastUpdated
          }.recover {
            case e =>
              log.error("Exception", e)
          }
      }
    case message =>
      log.error("Received invalid message " + message.toString)
  }
}

object StatusMonitorUpdater {

  case class UpdateAll()

}