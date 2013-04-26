package models

import play.api.libs.json.Json
import play.api.db._
import play.api.Play.current
import slick.driver.H2Driver.simple._
import sprints.SprintCounterValue
import globals.Global

case class DayCount(
                     id: Option[Long],
                     sprintId: Long,
                     dayNum: Int,
                     counterValuesJson: String) {

  def this() = this(None, 0, 0, "[]")

  def this(dayNum: Int, sprintId: Long) = this(None, sprintId, dayNum, "[]")

  def counterValues = Json.fromJson[Seq[SprintCounterValue]](Json.parse(counterValuesJson)).getOrElse(Seq.empty)

  def insert = {
    DayCount.database.withSession {
      implicit db: Session =>
        DayCount.insert(this)
    }
    Global.displayUpdater ! this
  }

  def update = {
    DayCount.database.withSession {
      implicit db: Session =>
        DayCount.where(_.id === id).update(this)
    }
    Global.displayUpdater ! this
  }

  def delete = {
    DayCount.database.withSession {
      implicit db: Session =>
        DayCount.where(_.id === id).delete
    }
    Global.displayUpdater ! this
  }

}

object DayCount extends Table[DayCount]("DAYCOUNT") {
  lazy val database = Database.forDataSource(DB.getDataSource())

  def id = column[Long]("ID", O PrimaryKey, O AutoInc)

  def sprintId = column[Long]("SPRINTID", O NotNull)

  def dayNum = column[Int]("DAYNUM", O NotNull)

  def counterValuesJson = column[String]("COUNTERVALUESJSON", O NotNull)

  def * = id.? ~ sprintId ~ dayNum ~ counterValuesJson <>((apply _).tupled, unapply _)

  def query = Query(this)

  def formApply(
                 id: Option[Long],
                 sprintId: Long,
                 dayNum: Int,
                 counterValues: List[SprintCounterValue]) =
    DayCount(id, sprintId, dayNum, Json.stringify(Json.toJson(counterValues)))

  def formUnapply(dayCount: DayCount) =
    Some(dayCount.id, dayCount.sprintId, dayCount.dayNum, dayCount.counterValues.toList)

  def findAllForSprint(sprintId: Long): Seq[DayCount] = database.withSession {
    implicit db: Session =>
      query.where(d => d.sprintId === sprintId).sortBy(d => d.dayNum.asc).list
  }

  def findById(dayCountId: Long): Option[DayCount] = database.withSession {
    implicit db: Session =>
      query.where(d => d.id === dayCountId).firstOption
  }
}