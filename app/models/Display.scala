package models

import org.squeryl.KeyedEntity
import org.squeryl.PrimitiveTypeMode._

class Display(val id: Long,
              var name: String,
              var sprintId: Long) extends KeyedEntity[Long] {
  lazy val sprint = WallDeeSchema.sprintToDisplays.right(this)

  def this() = this(0, "", 0)

  def save = inTransaction {
    WallDeeSchema.displays.insertOrUpdate(this)
  }

  def delete = inTransaction {
    if (isPersisted) {
      WallDeeSchema.displays.delete(id)
    }
  }
}

object Display {
  def findAll(): Seq[Display] = inTransaction {
    from(WallDeeSchema.displays)(d => select(d) orderBy (d.name desc)).toList
  }

  def findById(displayId: Long) = inTransaction {
    WallDeeSchema.displays.lookup(displayId)
  }

}