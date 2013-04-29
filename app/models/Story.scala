package models

import play.api.db.DB
import play.api.Play.current
import slick.driver.H2Driver.simple._
import globals.Global

case class Story(
  id: Option[Long],
  sprintId: Long,
  tag: String,
  description: String,
  points: Int) {

  def this() = this(None, 0, "", "",  0)

  def insert = {
    Story.database.withSession {
      implicit db: Session =>
        Story.insert(this)
    }
    Global.displayUpdater ! this
  }

  def update = {
    Story.database.withSession {
      implicit db: Session =>
        Story.where(_.id === id).update(this)
    }
    Global.displayUpdater ! this
  }

  def delete = {
    Story.database.withSession {
      implicit db: Session =>
        Story.where(_.id === id).delete
    }
    Global.displayUpdater ! this
  }
}

object Story extends Table[Story]("STORY") {
  lazy val database = Database.forDataSource(DB.getDataSource())

  def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)

  def sprintId = column[Long]("SPRINTID", O.NotNull)

  def tag = column[String]("TAG", O.NotNull)

  def description = column[String]("DESCRIPTION", O.NotNull)

  def points = column[Int]("POINTS", O.NotNull)

  def * = id.? ~ sprintId ~ tag ~ description ~ points <> ((apply _).tupled, unapply _)

  def query = Query(this)

  def findAllForSprint(sprintId: Long) = database.withSession {
    implicit db:Session =>
      query.where(s => s.sprintId === sprintId).sortBy(s => s.tag.asc).list
  }
}