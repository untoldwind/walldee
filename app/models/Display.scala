package models

import org.squeryl.KeyedEntity

class Display(val id: Long,
              var name: String,
              val sprintId: Long) extends KeyedEntity[Long] {
  lazy val sprint = WallDeeSchema.sprintToDisplays.right(this)

  def this() = this(0, "", 0)
}
