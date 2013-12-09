package actors

import akka.actor.{ActorRef, Actor}
import akka.event.slf4j.SLF4JLogging
import models.StatusMonitor
import play.api.libs.concurrent.Execution.Implicits._
import actors.monitorProcessors.MonitorProcessor
import scala.concurrent.duration._
import akka.pattern._
import akka.util.Timeout
import models.statusValues._
import scala.language.postfixOps

class StatusMonitorUpdater(requester: ActorRef) extends Actor with SLF4JLogging {
  implicit val timeout = Timeout(10 seconds)

  import StatusMonitorUpdater._

  def receive = {
    case UpdateAll() =>
      StatusMonitor.findAllActive.foreach {
        statusMonitor =>
          val processor = MonitorProcessor(statusMonitor)

          val request = RequestInfo(
            url = processor.apiUrl,
            method = "GET",
            username = statusMonitor.username,
            password = statusMonitor.password,
            headers = Seq("Accept" -> processor.accepts),
            body = None)

          statusMonitor.updateLastQueried

          val responseFuture = requester ? request
          responseFuture.map {
            case response: RequestSuccess =>
              val (status, json) = processor.process(response)
              processor.updateStatus(status, json)
              statusMonitor.updateLastUpdated
            case failure: RequestFailure =>
              log.error("Request failure", failure.exception)
          }.recover {
            case e =>
              log.error("Exception", e)
          }
      }
    case Test(statusMonitor) =>
      val origin = sender
      val processor = MonitorProcessor(statusMonitor)

      val request = RequestInfo(
        url = processor.apiUrl,
        method = "GET",
        username = statusMonitor.username,
        password = statusMonitor.password,
        headers = Seq("Accept" -> processor.accepts),
        body = None)

      val responseFuture = requester ? request
      responseFuture.map {
        case success: RequestSuccess =>
          val (status, json) = processor.process(success)
          origin ! StatusMonitorTestInfo(request, success, Some(status), Some(json))
        case failure: RequestFailure =>
          origin ! StatusMonitorTestInfo(request, failure, None, None)
      }.recover {
        case e =>
          origin ! StatusMonitorTestInfo(request, RequestFailure(e), None, None)
      }

    case message =>
      log.error("Received invalid message " + message.toString)
  }
}

object StatusMonitorUpdater {

  case class UpdateAll()

  case class Test(statusMonitor: StatusMonitor)

}