package actors.monitorProcessors

import models.StatusMonitor
import play.api.libs.ws.Response

object TeamcityProcessor extends MonitorProcessor{
  def process(statusMonitor: StatusMonitor, response: Response) {}
}
