package actors.monitorProcessors

import models.StatusMonitor
import play.api.libs.ws.Response

trait MonitorProcessor {
  def apiUrl(url: String) = url

  def process(statusMonitor: StatusMonitor, response: Response)
}
