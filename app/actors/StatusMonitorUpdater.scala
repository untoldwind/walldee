package actors

import akka.actor.{ActorRef, Actor}
import akka.event.slf4j.SLF4JLogging
import models.StatusMonitor
import actors.StatusMonitorUpdater.UpdateAll

class StatusMonitorUpdater(val statusMonitorCrawler: ActorRef) extends Actor with SLF4JLogging {
  def receive = {
    case UpdateAll() =>
      StatusMonitor.findAllActive.foreach {
        statusMonitor =>
          statusMonitorCrawler ! statusMonitor
      }
    case message =>
      log.error("Received invalid message {0}", message.toString)
  }
}

object StatusMonitorUpdater {

  case class UpdateAll()

}