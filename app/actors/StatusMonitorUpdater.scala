package actors

import akka.actor.{ActorRef, Actor}
import akka.event.slf4j.SLF4JLogging
import models.StatusMonitor
import actors.StatusMonitorUpdater.UpdateAll
import play.api.libs.ws.WS
import com.ning.http.client.Realm.AuthScheme

class StatusMonitorUpdater extends Actor with SLF4JLogging {
  def receive = {
    case UpdateAll() =>
      StatusMonitor.findAllActive.foreach {
        statusMonitor =>
          val wsRequest = if (statusMonitor.username.isDefined && statusMonitor.password.isDefined)
            WS.url(statusMonitor.url).withAuth(statusMonitor.username.get, statusMonitor.password.get, AuthScheme.BASIC)
          else
            WS.url(statusMonitor.url)

          wsRequest.get().map {
            response =>
              monitorProcessors.processor(statusMonitor.monitorType).process(statusMonitor, response)
          }
      }
    case message =>
      log.error("Received invalid message {0}", message.toString)
  }
}

object StatusMonitorUpdater {

  case class UpdateAll()

}