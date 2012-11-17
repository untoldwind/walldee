package actors.monitorProcessors

import models.StatusMonitor
import play.api.libs.ws.Response

trait MonitorProcessor {
  def process(statusMonitor:StatusMonitor, response:Response)
}
