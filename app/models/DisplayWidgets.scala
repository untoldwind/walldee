package models

object DisplayWidgets extends Enumeration {
  type Type = Value
  val BurndownChart, SprintTitle, Clock, Alarms = Value
}
