package actors.monitorProcessors

import models.{StatusTypes, StatusMonitor}
import models.statusMonitors.FreestyleTypes
import models.statusValues.{ResponseInfo, FreestyleStatus}
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
import org.jsoup.Jsoup
import com.fasterxml.jackson.core.JsonParseException
import akka.event.slf4j.SLF4JLogging

class FreestyleProcessor(var statusMonitor: StatusMonitor) extends MonitorProcessor with SLF4JLogging {
  override def accepts: String = "application/json"

  override def process(response: ResponseInfo) = {
    val statusOpt: Option[FreestyleStatus] = statusMonitor.freestyleConfig.map(_.freestyleType).getOrElse(FreestyleTypes.Regex) match {
      case FreestyleTypes.Regex => None
      case FreestyleTypes.Json =>
        try {
          FreestyleJsonProcessor.processJson(statusMonitor.freestyleConfig.flatMap(_.selector), response.bodyAsJson)
        } catch {
          case e: JsonParseException =>
            log.error("Exception", e)
            None
        }
      case FreestyleTypes.Xml =>
        parserXML(response.body).flatMap {
          document =>
            FreestyleXmlProcessor.processXml(statusMonitor.freestyleConfig.flatMap(_.selector), document)
        }
      case FreestyleTypes.Html =>
        parseHtml(response.body).flatMap {
          document =>
            FreestyleHtmlProcessor.processHtml(statusMonitor.freestyleConfig.flatMap(_.selector), document)
        }
    }
    statusOpt.map {
      status =>
        (StatusTypes.Ok, Json.toJson(status))
    }.getOrElse {
      (StatusTypes.Failure, JsObject(Seq.empty))
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

  private def parseHtml(html: String): Option[org.jsoup.nodes.Document] = {
    try {
      Some(Jsoup.parse(html))
    } catch {
      case e: Throwable =>
        logger.error("Exception", e)
        None
    }
  }
}
