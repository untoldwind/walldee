package models

import play.api.db._
import play.api.Play.current

import org.scalaquery.ql.TypeMapper._
import org.scalaquery.ql.extended.{ExtendedTable => Table}

import org.scalaquery.ql.extended.H2Driver.Implicit._

import org.scalaquery.session.{Database, Session}
import org.scalaquery.ql.Query

case class Story(
  id: Option[Long],
  sprintId: Long,
  tag: String,
  description: String,
  points: Int) {

  def this() = this(None, 0, "", "",  0)

  def insert = Story.database.withSession {
    implicit db: Session =>
      Story.insert(this)
  }

  def update = Story.database.withSession {
    implicit db: Session =>
      Story.where(_.id === id).update(this)
  }

  def delete = Story.database.withSession {
    implicit db: Session =>
      Story.where(_.id === id).delete
  }
}

object Story extends Table[Story]("STORY") {
  lazy val database = Database.forDataSource(DB.getDataSource())

  def id = column[Long]("ID", O PrimaryKey, O AutoInc)

  def sprintId = column[Long]("SPRINTID", O NotNull)

  def tag = column[String]("TAG", O NotNull)

  def description = column[String]("DESCRIPTION", O NotNull)

  def points = column[Int]("POINTS", O NotNull)

  def * = id.? ~ sprintId ~ tag ~ description ~ points <> ((apply _).tupled, unapply _)

  def query = Query(this)

  def findAllForSprint(sprintId: Long) = database.withSession {
    implicit db:Session =>
      query.where(s => s.sprintId === sprintId).orderBy(tag.asc).list
  }
}