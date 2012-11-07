package models

import json.SprintCounter
import java.sql.Date
import play.api.libs.json.Json
import org.joda.time.{DateTimeConstants, DateMidnight}
import play.api.db._
import play.api.Play.current

import org.scalaquery.ql.TypeMapper._
import org.scalaquery.ql.extended.{ExtendedTable => Table}

import org.scalaquery.ql.extended.H2Driver.Implicit._

import org.scalaquery.session.{Database, Session}
import org.scalaquery.ql.Query

case class Sprint(
  id: Option[Long],
  title: String,
  num: Int,
  sprintStart: Date,
  sprintEnd: Date,
  countersJson: String) {

  def this() = this(None, "", 0, new Date(System.currentTimeMillis()), new Date(System.currentTimeMillis()), "[]")

  def counters = Json.fromJson[Seq[SprintCounter]](Json.parse(countersJson))

  lazy val numberOfDays = {
    var current = new DateMidnight(sprintStart.getTime)
    val end = new DateMidnight(sprintEnd.getTime)
    var numberOfDays = 0

    while (current.compareTo(end) < 0) {
      if (current.getDayOfWeek != DateTimeConstants.SATURDAY && current.getDayOfWeek != DateTimeConstants.SUNDAY) {
        numberOfDays += 1
      }
      current = current.plusDays(1)
    }
    numberOfDays
  }

  def insert = Sprint.database.withSession {
    implicit db: Session =>
      Sprint.insert(this)
  }

  def update = Sprint.database.withSession {
    implicit db: Session =>
      Sprint.where(_.id === id).update(this)
  }

  def delete = Sprint.database.withTransaction {
    implicit db: Session =>
      Story.where(s => s.sprintId === id).delete
      Sprint.where(_.id === id).delete
  }
}

object Sprint extends Table[Sprint]("SPRINT") {
  lazy val database = Database.forDataSource(DB.getDataSource())

  def id = column[Long]("ID", O PrimaryKey, O AutoInc)

  def title = column[String]("TITLE", O NotNull)

  def num = column[Int]("NUM", O NotNull)

  def sprintStart = column[Date]("SPRINTSTART", O NotNull)

  def sprintEnd = column[Date]("SPRINTEND", O NotNull)

  def countersJson = column[String]("COUNTERSJSON", O NotNull)

  def * = id.? ~ title ~ num ~ sprintStart ~ sprintEnd ~ countersJson <>((apply _).tupled, unapply _)

  def formApply(
    id: Option[Long],
    title: String,
    num: Int,
    sprintStart: Date,
    sprintEnd: Date,
    counters: List[SprintCounter]) =
    Sprint(id, title, num, sprintStart, sprintEnd, Json.stringify(Json.toJson(counters)))

  def formUnapply(sprint: Sprint) =
    Some(sprint.id, sprint.title, sprint.num, sprint.sprintStart, sprint.sprintEnd, sprint.counters.toList)

  def query = Query(this)

  def findAll(): Seq[Sprint] = database.withSession {
    implicit db: Session =>
      query.orderBy(num.asc).list
  }

  def findById(sprintId: Long) = database.withSession {
    implicit db: Session =>
      query.where(s => s.id === sprintId).firstOption
  }
}