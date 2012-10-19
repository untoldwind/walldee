package models

import org.squeryl.KeyedEntity
import org.squeryl.PrimitiveTypeMode._
import java.util.Date

class Sprint(val id: Long,
             var title: String,
             var num: Int,
             var sprintStart: Date,
             var sprintEnd: Date,
             var counters: String) extends KeyedEntity[Long] {

  lazy val stories = WallDeeSchema.sprintToStories.left(this)

  def this() = this(0, "", 0, new Date(System.currentTimeMillis()), new Date(System.currentTimeMillis()), "")

  def save = inTransaction {
    WallDeeSchema.sprints.insertOrUpdate(this)
  }

  def delete = inTransaction {
    if (isPersisted) {
      WallDeeSchema.sprints.delete(id)
    }
  }
}

object Sprint {
  def findAll(): Seq[Sprint] = inTransaction {
    from(WallDeeSchema.sprints)(s => select(s) orderBy (s.num desc)).toList
  }

  def findById(sprintId: Long) = inTransaction {
    WallDeeSchema.sprints.lookup(sprintId)
  }
}