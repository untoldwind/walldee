package models

import org.squeryl.PrimitiveTypeMode._
import org.squeryl.KeyedEntity
import java.util.Date

class Alarm(val id: Long,
            var name: String,
            var nextDate: Date,
            var repeatDays: Option[Int]) extends KeyedEntity[Long] {
  def save = inTransaction {
    WallDeeSchema.alarms.insertOrUpdate(this)
  }

  def delete = inTransaction {
    if (isPersisted) {
      WallDeeSchema.alarms.delete(id)
    }
  }
}

object Alarm {
  def findAll(): Seq[Alarm] = inTransaction {
    from(WallDeeSchema.alarms)(a => select(a) orderBy (a.nextDate asc)).toList
  }
}
