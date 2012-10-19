package models

import org.squeryl.KeyedEntity
import org.squeryl.PrimitiveTypeMode._

class Story(val id: Long,
            var tag: String,
            var description: String,
            var points: Int,
            val sprintId: Long) extends KeyedEntity[Long] {

  lazy val sprint = WallDeeSchema.sprintToStories.right(this)

  def this() = this(0, "", "", 0, 0)

  def save = inTransaction {
    WallDeeSchema.stories.insertOrUpdate(this)
  }

  def delete = inTransaction {
    if (isPersisted) {
      WallDeeSchema.stories.delete(id)
    }
  }
}

object Story {
  def findAllForSprint(sprintId: Long) = inTransaction {
    from(WallDeeSchema.stories)(s => where (s.sprintId === sprintId) select(s) orderBy (s.tag asc)).toList
  }
}