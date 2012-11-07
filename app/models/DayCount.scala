package models

import json.SprintCounterValue
import play.api.libs.json.Json
import play.api.db._
import play.api.Play.current

import org.scalaquery.ql.TypeMapper._
import org.scalaquery.ql.extended.{ExtendedTable => Table}

import org.scalaquery.ql.extended.H2Driver.Implicit._

import org.scalaquery.session.{Database, Session}
import org.scalaquery.ql.Query

case class DayCount(
  id: Option[Long],
  sprintId: Long,
  dayNum: Int,
  counterValuesJson: String) {

  def this() = this(None, 0, 0, "[]")

  def this(dayNum: Int, sprintId: Long) = this(None, sprintId, dayNum, "[]")

  def counterValues = Json.fromJson[Seq[SprintCounterValue]](Json.parse(counterValuesJson))

  def insert = DayCount.database.withSession {
    implicit db: Session =>
      DayCount.insert(this)
  }

  def update = DayCount.database.withSession {
    implicit db: Session =>
      DayCount.where(_.id === id).update(this)
  }

  def delete = DayCount.database.withSession {
    implicit db: Session =>
      DayCount.where(_.id === id).delete
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

  def findAllForSprint(sprintId: Long) = database.withSession {
    implicit db: Session =>
      query.where(d => d.sprintId === sprintId).orderBy(dayNum.asc).list
  }

  def findById(dayCountId: Long) = database.withSession {
    implicit db: Session =>
      query.where(d => d.id === dayCountId).firstOption
  }
}