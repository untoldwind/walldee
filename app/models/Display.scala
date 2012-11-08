package models

import play.api.db._
import play.api.Play.current

import scala.slick.driver.H2Driver.simple._

case class Display(id: Option[Long],
                   name: String,
                   sprintId: Long,
                   backgroundColor: String,
                   refreshTime: Int) {

  def this() = this(None, "", 0, "#000000", 5)

  def insert = Display.database.withSession {
    implicit db: Session =>
      Display.insert(this)
  }

  def update = Display.database.withSession {
    implicit db: Session =>
      Display.where(_.id === id).update(this)
  }

  def delete = Display.database.withSession {
    implicit db: Session =>
      Display.where(_.id === id).delete
  }
}

object Display extends Table[Display]("DISPLAY") {
  lazy val database = Database.forDataSource(DB.getDataSource())

  def id = column[Long]("ID", O PrimaryKey, O AutoInc)

  def name = column[String]("NAME", O NotNull)

  def sprintId = column[Long]("SPRINTID", O NotNull)

  def backgroundColor = column[String]("BACKGROUNDCOLOR", O NotNull)

  def refreshTime = column[Int]("REFRESHTIME", O NotNull)

  def * = id.? ~ name ~ sprintId ~ backgroundColor ~ refreshTime <>((apply _).tupled, unapply _)

  def query = Query(this)

  def findAll: Seq[Display] = database.withSession {
    implicit db: Session =>
      query.sortBy(d => d.name.asc).list
  }

  def findById(displayId: Long): Option[Display] = database.withSession {
    implicit db: Session =>
      query.where(d => d.id === displayId).firstOption
  }
}
