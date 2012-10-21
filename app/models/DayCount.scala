package models

import json.{SprintCounterValue, SprintCounter}
import org.squeryl.KeyedEntity
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.annotations.Transient
import play.api.libs.json.Json

class DayCount(val id: Long,
               var dayNum: Int,
               var sprintId: Long,
               var counterValuesJson: String) extends KeyedEntity[Long] {
  lazy val sprint = WallDeeSchema.sprintToDayCounts.right(this)

  def this() = this(0, 0, 0, "[]")

  def this(dayNum:Int, sprintId:Long) = this(0, dayNum, sprintId, "[]")

  @Transient
  def counterValues = Json.fromJson[Seq[SprintCounterValue]](Json.parse(counterValuesJson))

  def counterValues_=(counterValues: Seq[SprintCounterValue]) = {
    counterValuesJson = Json.stringify(Json.toJson(counterValues))
  }

  def save = inTransaction {
    WallDeeSchema.dayCounts.insertOrUpdate(this)
  }

  def delete = inTransaction {
    if (isPersisted) {
      WallDeeSchema.dayCounts.delete(id)
    }
  }
}

object DayCount {
  def findAllForSprint(sprintId: Long) = inTransaction {
    from(WallDeeSchema.dayCounts)(d => where(d.sprintId === sprintId) select (d) orderBy (d.dayNum asc)).toList
  }
}