package models

import org.squeryl.KeyedEntity
import org.squeryl.PrimitiveTypeMode._

class DayCount(val id: Long,
               val dayNum: Int,
               val counterValues: String,
               val sprintId: Long) extends KeyedEntity[Long] {
  lazy val sprint = WallDeeSchema.sprintToDayCounts.right(this)

  def this() = this(0, 0, "", 0)
}

object DayCount {
  def findAllForSprint(sprintId: Long) = inTransaction {
    from(WallDeeSchema.dayCounts)(d => where(d.sprintId === sprintId) select (d) orderBy (d.dayNum asc)).toList
  }
}