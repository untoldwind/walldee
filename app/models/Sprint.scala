package models

import org.squeryl.KeyedEntity
import org.squeryl.PrimitiveTypeMode._

class Sprint(val id: Long,
             var title: String,
             var num: Int) extends KeyedEntity[Long] {

  lazy val stories = WallDeeSchema.sprintToStories.left(this)

  def this() = this(0, "", 0)

  def this(title:String, num:Int) = this(0, title, num)

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