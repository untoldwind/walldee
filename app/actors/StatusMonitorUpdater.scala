package actors

import akka.actor.{ActorRef, Actor}
import akka.event.slf4j.SLF4JLogging
import models.StatusMonitor
import actors.StatusMonitorUpdater.UpdateAll
import play.api.libs.concurrent.Execution.Implicits._
import actors.monitorProcessors.MonitorProcessor
import scala.concurrent.duration._
import akka.pattern._
import akka.util.Timeout
import models.statusValues.{ResponseInfo, RequestInfo}
import scala.language.postfixOps

class StatusMonitorUpdater(requester: ActorRef) extends Actor with SLF4JLogging {
  implicit val timeout = Timeout(10 seconds)

  def receive = {
    case UpdateAll() =>
      StatusMonitor.findAllActive.foreach {
        statusMonitor =>
          val processor = MonitorProcessor(statusMonitor.monitorType)
          val url = processor.apiUrl(statusMonitor.url)

          val request = RequestInfo(
            url = url,
            method = "GET",
            username = statusMonitor.username,
            password = statusMonitor.password,
            headers = Seq("Accept" -> processor.accepts),
            body = None)

          statusMonitor.updateLastQueried

          val responseFuture = requester ? request
          responseFuture.map {
            case response: ResponseInfo =>
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