package actors.monitorProcessors

import models.StatusMonitor
import play.api.libs.ws.Response

object FreestyleProcessor extends MonitorProcessor {
  override def accepts: String = "application/json"

  override def process(statusMonitor: StatusMonitor, response: Response) {}
}
