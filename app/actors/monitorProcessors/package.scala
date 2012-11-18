package actors

import models.StatusMonitorTypes

package object monitorProcessors {
  def processor(statusMonitorType: StatusMonitorTypes.Type): MonitorProcessor = statusMonitorType match {
    case StatusMonitorTypes.Jenkins => JenkinsProcessor
    case StatusMonitorTypes.Teamcity => TeamcityProcessor
    case StatusMonitorTypes.Sonar => SonarProcessor
    case StatusMonitorTypes.Icinga => IcingaProcessor
  }
}
