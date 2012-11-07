package models

import org.squeryl.Schema
import org.squeryl.PrimitiveTypeMode._

object WallDeeSchema extends Schema {
  val sprints = table[Sprint]

}
