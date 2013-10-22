package models

import play.api.Play.current
import slick.driver.H2Driver.simple._
import play.api.db.DB
import globals.Global

case class Team(id: Option[Long] = None,
                name: String,
                currentSprintId: Option[Long] = None) {

  def this() = this(None, "", None)

  def insert: Long = {
    Team.database.withSession {
      implicit db: Session =>
        Team.forInsert.insert(this)
    }
  }

  def update = {
    Team.database.withSession {
      implicit db: Session =>
        Team.where(_.id === id).update(this)
    }
    Global.displayUpdater ! this
  }

  def delete = {
    Team.database.withTransaction {
      implicit db: Session =>
        Team.where(_.id === id).delete
    }
    Global.displayUpdater ! this
  }
}

object Team extends Table[Team]("TEAM") {
  lazy val database = Database.forDataSource(DB.getDataSource())

  def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)

  def name = column[String]("NAME", O.NotNull)

  def currentSprintId = column[Long]("CURRENTSPRINTID")

  def * = id.? ~ name ~ currentSprintId.? <>((apply _).tupled, (unapply _))

  def forInsert = id.? ~ name ~ currentSprintId.? <> (apply _, unapply _) returning id

  def query = Query(this)

  def findAll: Seq[Team] = database.withSession {
    implicit db: Session =>
      query.sortBy(t => t.name.asc).list
  }

  def findFirstByName(name: String): Option[Team] = database.withSession {
    implicit db: Session =>
      query.where(_.name === name).firstOption
  }

  def findById(teamId: Long): Option[Team] = database.withSession {
    implicit db: Session =>
      query.where(p => p.id === teamId).firstOption
  }
}