package models

import play.api.db._
import play.api.Play.current

import org.scalaquery.ql.TypeMapper._
import org.scalaquery.ql.extended.{ExtendedTable => Table}

import org.scalaquery.ql.extended.H2Driver.Implicit._

import org.scalaquery.session.{Database, Session}
import org.scalaquery.ql.Query
import java.util.Date
import models.DateMapper.date2timestamp

case class StatusMonitor(id: Option[Long],
                         name: String,
                         typeNum: Int,
                         url: String,
                         username: Option[String],
                         password: Option[String],
                         active: Boolean,
                         keepHistory: Int,
                         updatePeriod: Int,
                         lastQueried: Option[Date],
                         lastUpdated: Option[Date]) {
  def this() = this(None, "", 0, "", None, None, true, 10, 60, None, None)

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

  def username = column[String]("USERNAME")

  def password = column[String]("PASSWORD")

  def active = column[Boolean]("ACTIVE", O NotNull)

  def keepHistory = column[Int]("KEEPHISTORY", O NotNull)

  def updatePeriod = column[Int]("UPDATEPERIOD", O NotNull)

  def lastQueried = column[Date]("LASTQUERIED")

  def lastUpdated = column[Date]("LASTUPDATED")

  def * = id.? ~ name ~ typeNum ~ url ~ username.? ~ password.? ~ active ~ keepHistory ~ updatePeriod ~ lastQueried.? ~ lastUpdated.? <>((apply _).tupled, unapply _)

  def query = Query(this)

  def findAll: Seq[StatusMonitor] = database.withSession {
    implicit db: Session =>
      query.orderBy(name.asc).list
  }

  def findAllActive: Seq[StatusMonitor] = database.withSession {
    implicit db: Session =>
      query.where(s => s.active).orderBy(name.asc).list
  }

  def findById(statusMonitorId: Long): Option[StatusMonitor] = database.withSession {
    implicit db: Session =>
      query.where(s => s.id === statusMonitorId).firstOption
  }
}
