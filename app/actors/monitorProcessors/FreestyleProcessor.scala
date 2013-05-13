package actors.monitorProcessors

import models.{StatusTypes, StatusMonitor}
import models.statusMonitors.FreestyleTypes
import models.statusValues.FreestyleStatus
import play.api.libs.json._
import play.libs.XML
import play.api.Logger.logger
import org.w3c.dom._
import play.api.libs.ws.Response
import play.api.libs.json.JsString
import play.api.libs.json.JsNumber
import scala.collection.mutable
import play.api.libs.ws.Response
import play.api.libs.json.JsString
import play.api.libs.json.JsNumber

object FreestyleProcessor extends MonitorProcessor {
  override def accepts: String = "application/json"

  override def process(statusMonitor: StatusMonitor, response: Response) {
    val statusOpt: Option[FreestyleStatus] = statusMonitor.freestyleConfig.map(_.freestyleType).getOrElse(FreestyleTypes.Regex) match {
      case FreestyleTypes.Regex => None
      case FreestyleTypes.Json =>
        FreestyleJsonProcessor.processJson(statusMonitor.freestyleConfig.flatMap(_.selector), response.json)
      case FreestyleTypes.Xml =>
        parserXML(response.body).flatMap {
          document =>
            FreestyleXmlProcessor.processXml(statusMonitor.freestyleConfig.flatMap(_.selector), document)
        }
      case FreestyleTypes.Html => None
    }
    statusOpt.map {
      status =>
        updateStatus(statusMonitor, StatusTypes.Ok, Json.toJson(status))
    }.getOrElse {
      updateStatus(statusMonitor, StatusTypes.Failure, JsObject(Seq.empty))
    }
  }

  private def parserXML(xml: String): Option[Document] = {
    try {
      Some(XML.fromString(xml))
    } catch {
      case e: Throwable =>
        logger.error("Exception", e)
        None
    }
  }
}
