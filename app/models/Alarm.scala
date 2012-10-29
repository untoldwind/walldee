package models

import org.squeryl.PrimitiveTypeMode._
import org.squeryl.KeyedEntity
import java.util.Date
import org.joda.time.DateMidnight
import java.sql.Timestamp

class Alarm(val id: Long,
            var name: String,
            var nextDate: Timestamp,
            var repeatDays: Option[Int]) extends KeyedEntity[Long] {

  def this() = this(0, "", new Timestamp(System.currentTimeMillis()), None)

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

  def findAllForToday(): Seq[Alarm] = inTransaction {
    val today = DateMidnight.now
    val tomorrow = today.plusDays(1)

    from(WallDeeSchema.alarms)(a => where(a.nextDate >= new Timestamp(today.getMillis) and a.nextDate < new Timestamp(tomorrow.getMillis)) select(a) orderBy (a.nextDate asc)).toList
  }

  def findById(alarmId: Long) = inTransaction {
    WallDeeSchema.alarms.lookup(alarmId)
  }
}
