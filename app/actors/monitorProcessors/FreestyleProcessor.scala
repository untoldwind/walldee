package actors.monitorProcessors

import models.{StatusTypes, StatusMonitor}
import models.statusMonitors.FreestyleTypes
import models.statusValues.FreestyleStatus
import play.api.libs.json._
import play.libs.XML
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
    statusMonitor.freestyleConfig.map(_.freestyleType).getOrElse(FreestyleTypes.Regex) match {
      case FreestyleTypes.Regex =>
      case FreestyleTypes.Json =>
      case FreestyleTypes.Xml =>
        val statusOpt = FreestyleXmlProcessor.processXml(statusMonitor.freestyleConfig.flatMap(_.selector), XML.fromString(response.body))

        statusOpt.map {
          status =>
            updateStatus(statusMonitor, StatusTypes.Ok, Json.toJson(status))
        }.getOrElse {
          updateStatus(statusMonitor, StatusTypes.Failure, JsObject(Seq.empty))
        }
      case FreestyleTypes.Html =>
    }
  }


}
