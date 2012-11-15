package actors

import akka.actor.Actor
import akka.event.slf4j.SLF4JLogging
import models.StatusMonitor

class StatusMonitorCrawler extends Actor with SLF4JLogging {
  def receive = {
    case statusMonitor: StatusMonitor =>
      println(statusMonitor.toString)
    case message =>
      log.error("Received invalid message {0}", message.toString)
  }
}
