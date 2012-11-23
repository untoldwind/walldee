package models

import play.api.db._
import play.api.Play.current

import org.scalaquery.ql.TypeMapper._
import org.scalaquery.ql.extended.{ExtendedTable => Table}

import org.scalaquery.ql.extended.H2Driver.Implicit._

import org.scalaquery.session.{Database, Session}
import org.scalaquery.ql.Query
import play.api.db.DB
import globals.Global

case class Project(id: Option[Long],
                   name: String) {

  def this() = this(None, "")

  def insert = {
    Project.database.withSession {
      implicit db: Session =>
        Project.insert(this)
    }
  }

  def update = {
    Project.database.withSession {
      implicit db: Session =>
        Project.where(_.id === id).update(this)
    }
    Global.displayUpdater ! this
  }

  def delete = {
    Project.database.withTransaction {
      implicit db: Session =>
        Project.where(_.id === id).delete
    }
    Global.displayUpdater ! this
  }
}

object Project extends Table[Project]("PROJECT") {
  def database = Database.forDataSource(DB.getDataSource())

  def id = column[Long]("ID", O PrimaryKey, O AutoInc)

  def name = column[String]("NAME", O NotNull)

  def * = id.? ~ name <>((apply _).tupled, (unapply _))

  def query = Query(this)

  def findAll: Seq[Project] = database.withSession {
    implicit db: Session =>
      query.orderBy(name.asc).list
  }

  def findById(projectId: Long): Option[Project] = database.withSession {
    implicit db: Session =>
      query.where(p => p.id === projectId).firstOption
  }
}