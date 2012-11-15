package models

object StatusTypes extends Enumeration {
  type Type = Value
  val Ok, Failure, Unknown = Value
}
