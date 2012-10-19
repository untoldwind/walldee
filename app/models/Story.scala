package models

import org.squeryl.KeyedEntity

class Story(
  val id: Long,
  val tag: String,
  val description: String,
  val points: Int,
  val sprintId: Long) extends KeyedEntity[Long] {

  lazy val sprint = WallDeeSchema.sprintToStories.right(this)

  def this() = this(0, "", "", 0, 0)
}
