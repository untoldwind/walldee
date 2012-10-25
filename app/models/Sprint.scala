package models

import json.SprintCounter
import org.squeryl.KeyedEntity
import org.squeryl.PrimitiveTypeMode._
import java.util.Date
import org.squeryl.annotations.Transient
import play.api.libs.json.Json
import org.joda.time.{DateTimeConstants, DateMidnight, LocalDate}

class Sprint(val id: Long,
             var title: String,
             var num: Int,
             var sprintStart: Date,
             var sprintEnd: Date,
             var countersJson: String) extends KeyedEntity[Long] {

  lazy val stories = WallDeeSchema.sprintToStories.left(this)

  def this() = this(0, "", 0, new Date(System.currentTimeMillis()), new Date(System.currentTimeMillis()), "[]")

  @Transient
  def counters = Json.fromJson[Seq[SprintCounter]](Json.parse(countersJson))

  def counters_=(counters: Seq[SprintCounter]) = {
    countersJson = Json.stringify(Json.toJson(counters))
  }

  @Transient
  lazy val numberOfDays = {
    var current = new DateMidnight(sprintStart.getTime)
    val end = new DateMidnight(sprintEnd.getTime)
    var numberOfDays = 0

    while (current.compareTo(end) < 0) {
      if (current.getDayOfWeek != DateTimeConstants.SATURDAY && current.getDayOfWeek != DateTimeConstants.SUNDAY)
        numberOfDays += 1
      current = current.plusDays(1)
    }
    numberOfDays
  }

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