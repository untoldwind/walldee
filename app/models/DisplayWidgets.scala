package models

object DisplayWidgets extends Enumeration {
  type Type = Value
  val Burndown, SprintTitle, Clock, Alarms, IFrame, BuildStatus, HostStatus, Metrics = Value
}
