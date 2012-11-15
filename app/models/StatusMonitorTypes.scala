package models

object StatusMonitorTypes extends Enumeration {
  type Type = Value
  val Jenkins, Teamcity, Sonar = Value
}
