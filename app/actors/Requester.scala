package actors

import akka.actor.Actor
import akka.event.slf4j.SLF4JLogging
import models.statusValues.{RequestFailure, RequestSuccess, RequestInfo}
import play.api.libs.ws.WS
import com.ning.http.client.Realm.AuthScheme
import scala.collection.JavaConversions._
import play.api.libs.concurrent.Execution.Implicits._

class Requester extends Actor with SLF4JLogging {
  def receive = {
    case RequestInfo(url, method, usernameOpt, passwordOpt, headers, bodyOpt) =>
      val origin = sender
      val wsRequest = (
        for (username <- usernameOpt; password <- passwordOpt)
        yield WS.url(url).withAuth(username, password, AuthScheme.BASIC).withHeaders(headers: _*)
        ).getOrElse(WS.url(url).withHeaders(headers: _*))

      method match {
        case "GET" =>
          wsRequest.get().map {
            response =>
              val headers = response.ahcResponse.getHeaders.entrySet().toSeq.flatMap {
                entry =>
                  entry.getValue.map {
                    value =>
                      entry.getKey -> value
                  }
              }
              origin ! RequestSuccess(statusCode = response.status,
                statusText = response.statusText,
                headers = headers,
                body = response.body)
          }.recover {
            case e =>
              origin ! RequestFailure(e)
          }
      }
    case message =>
      log.error("Received invalid message " + message.toString)
  }
}

