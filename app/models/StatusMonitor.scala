package models

import play.api.db._
import play.api.Play.current

import org.scalaquery.ql.TypeMapper._
import org.scalaquery.ql.extended.{ExtendedTable => Table}

import org.scalaquery.ql.extended.H2Driver.Implicit._

import org.scalaquery.session.{Database, Session}
import org.scalaquery.ql.Query

case class StatusMonitor(id: Option[Long], name: String, typeNum: Int, url: String, active: Boolean, keepHistory: Int) {
  def this() = this(None, "", 0, "", true, 10)

  def monitorType = StatusMonitorTypes(typeNum)

  def insert = StatusMonitor.database.withSession {
    implicit db: Session =>
      StatusMonitor.insert(this)
  }

  def update = StatusMonitor.database.withSession {
    implicit db: Session =>
      StatusMonitor.where(_.id === id).update(this)
  }

  def delete = StatusMonitor.database.withSession {
    implicit db: Session =>
      StatusMonitor.where(_.id === id).delete
  }
}

object StatusMonitor extends Table[StatusMonitor]("STATUSMONITOR") {
  lazy val database = Database.forDataSource(DB.getDataSource())

  def id = column[Long]("ID", O PrimaryKey, O AutoInc)

  def name = column[String]("NAME", O NotNull)

  def typeNum = column[Int]("TYPENUM", O NotNull)

  def url = column[String]("URL", O NotNull)

  def active = column[Boolean]("ACTIVE", O NotNull)

  def keepHistory = column[Int]("KEEPHISTORY", O NotNull)

  def * = id.? ~ name ~ typeNum ~ url ~ active ~ keepHistory <>((apply _).tupled, unapply _)

  def query = Query(this)

  def findAll = database.withSession {
    implicit db: Session =>
      query.orderBy(name.asc).list
  }
}
