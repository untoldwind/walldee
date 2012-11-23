package models

import play.api.db._
import play.api.Play.current

import org.scalaquery.ql.TypeMapper._
import org.scalaquery.ql.extended.{ExtendedTable => Table}

import org.scalaquery.ql.extended.H2Driver.Implicit._

import org.scalaquery.session.{Database, Session}
import org.scalaquery.ql.Query
import java.util.Locale
import globals.Global

case class Display(id: Option[Long],
                   name: String,
                   sprintId: Long,
                   projectId: Option[Long],
                   backgroundColor: String,
                   refreshTime: Int) {

  def this() = this(None, "", 0, None, "#000000", 5)

  def insert = Display.database.withSession {
    implicit db: Session =>
      Display.insert(this)
  }

  def update = {
    Display.database.withSession {
      implicit db: Session =>
        Display.where(_.id === id).update(this)
    }
    Global.displayUpdater ! this
  }

  def delete = {
    Display.database.withSession {
      implicit db: Session =>
        Display.where(_.id === id).delete
    }
    Global.displayUpdater ! this
  }
}

object Display extends Table[Display]("DISPLAY") {
  lazy val database = Database.forDataSource(DB.getDataSource())

  def id = column[Long]("ID", O PrimaryKey, O AutoInc)

  def name = column[String]("NAME", O NotNull)

  def sprintId = column[Long]("SPRINTID", O NotNull)

  def projectId = column[Long]("PROJECTID", O NotNull)

  def backgroundColor = column[String]("BACKGROUNDCOLOR", O NotNull)

  def refreshTime = column[Int]("REFRESHTIME", O NotNull)

  def * = id.? ~ name ~ sprintId ~ projectId.? ~ backgroundColor ~ refreshTime <>((apply _).tupled, unapply _)

  def query = Query(this)

  def findAll: Seq[Display] = database.withSession {
    implicit db: Session =>
      query.orderBy(name.asc).list
  }

  def findAllForSprint(sprintId: Long): Seq[Display] = database.withSession {
    implicit db: Session =>
      query.where(d => d.sprintId === sprintId).orderBy(name.asc).list
  }

  def findAllForProject(projectId: Long): Seq[Display] = database.withSession {
    implicit db: Session =>
      query.where(d => d.projectId === projectId).orderBy(name.asc).list
  }

  def findById(displayId: Long): Option[Display] = database.withSession {
    implicit db: Session =>
      query.where(d => d.id === displayId).firstOption
  }
}
