package actors.monitorProcessors

import models.StatusMonitor
import play.api.libs.ws.Response

object SonarProcessor extends MonitorProcessor {
  def process(statusMonitor: StatusMonitor, response: Response) {}
}
