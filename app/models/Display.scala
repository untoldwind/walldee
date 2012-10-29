package models

import org.squeryl.KeyedEntity
import org.squeryl.PrimitiveTypeMode._

class Display(val id: Long,
              var name: String,
              var sprintId: Long,
              var backgroundColor: String) extends KeyedEntity[Long] {
  lazy val displayItems = WallDeeSchema.displayToDisplayItems.left(this)

  lazy val sprint = WallDeeSchema.sprintToDisplays.right(this)

  def this() = this(0, "", 0, "#000000")

  def save = inTransaction {
    WallDeeSchema.displays.insertOrUpdate(this)
  }

  def delete = inTransaction {
    if (isPersisted) {
      displayItems.deleteAll
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