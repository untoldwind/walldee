package actors.monitorProcessors

import models.{StatusMonitorTypes, StatusValue, StatusTypes, StatusMonitor}
import play.api.libs.ws.Response
import play.api.libs.json.{Json, JsValue}
import models.statusValues.ResponseInfo

trait MonitorProcessor {
  def statusMonitor: StatusMonitor

  def apiUrl = statusMonitor.url

  def process(response: ResponseInfo): (StatusTypes.Type, JsValue)

  def accepts: String = "application/json"

  def updateStatus(status: StatusTypes.Type, json: JsValue) {
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
  def apply(statusMonitor: StatusMonitor): MonitorProcessor = statusMonitor.monitorType match {
    case StatusMonitorTypes.Jenkins => new JenkinsProcessor(statusMonitor)
    case StatusMonitorTypes.Teamcity => new TeamcityProcessor(statusMonitor)
    case StatusMonitorTypes.Sonar => new SonarProcessor(statusMonitor)
    case StatusMonitorTypes.Icinga => new IcingaProcessor(statusMonitor)
    case StatusMonitorTypes.Freestyle => new FreestyleProcessor(statusMonitor)
  }

}