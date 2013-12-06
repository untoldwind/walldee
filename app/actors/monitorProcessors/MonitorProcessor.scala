package actors.monitorProcessors

import models.{StatusMonitorTypes, StatusValue, StatusTypes, StatusMonitor}
import play.api.libs.ws.Response
import play.api.libs.json.{Json, JsValue}
import models.statusValues.ResponseInfo

trait MonitorProcessor {
  def apiUrl(url: String) = url

  def process(statusMonitor: StatusMonitor, response: ResponseInfo)

  def accepts: String = "application/json"

  def updateStatus(statusMonitor: StatusMonitor, status: StatusTypes.Type, json: JsValue) {
    val lastStatusValue = StatusValue.findLastForStatusMonitor(statusMonitor.id.get)

    if (lastStatusValue.isEmpty || lastStatusValue.get.status != status || lastStatusValue.get.statusValues != json) {
      val nextStatusValue = new StatusValue(statusMonitor.id.get, status, json)

      nextStatusValue.insert

      val statusValues = StatusValue.findAllForStatusMonitor(statusMonitor.id.get)

      if (statusValues.length > statusMonitor.keepHistory) {
        statusValues.slice(statusMonitor.keepHistory, statusValues.length).foreach {
          statusValue =>
            statusValue.delete
        }
      }
    }
  }
}

object MonitorProcessor {
  def apply(statusMonitorType: StatusMonitorTypes.Type): MonitorProcessor = statusMonitorType match {
    case StatusMonitorTypes.Jenkins => JenkinsProcessor
    case StatusMonitorTypes.Teamcity => TeamcityProcessor
    case StatusMonitorTypes.Sonar => SonarProcessor
    case StatusMonitorTypes.Icinga => IcingaProcessor
    case StatusMonitorTypes.Freestyle => FreestyleProcessor
  }

}