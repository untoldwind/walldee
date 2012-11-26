package models

import play.api.db._
import play.api.Play.current

import org.scalaquery.ql.TypeMapper._
import org.scalaquery.ql.extended.{ExtendedTable => Table}

import org.scalaquery.ql.extended.H2Driver.Implicit._

import org.scalaquery.session.{Database, Session}
import java.sql.Timestamp
import org.joda.time.DateMidnight
import java.util.Date
import models.DateMapper.date2timestamp
import org.scalaquery.ql.Query
import globals.Global

case class Alarm(id: Option[Long], name: String, nextDate: Date, durationMins:Int, repeatDays: Option[Int]) {
  def this() = this(None, "", new Timestamp(System.currentTimeMillis()), 15, None)

  def insert = {
    Alarm.database.withSession {
      implicit db: Session =>
        Alarm.insert(this)
    }
    Global.displayUpdater ! this
  }

  def update = {
    Alarm.database.withSession {
      implicit db: Session =>
        Alarm.where(_.id === id).update(this)
    }
    Global.displayUpdater ! this
  }

  def delete = {
    Alarm.database.withSession {
      implicit db: Session =>
        Alarm.where(_.id === id).delete
    }
    Global.displayUpdater ! this
  }
}

object Alarm extends Table[Alarm]("ALARM") {
  lazy val database = Database.forDataSource(DB.getDataSource())

  def id = column[Long]("ID", O PrimaryKey, O AutoInc)

  def name = column[String]("NAME", O NotNull)

  def nextDate = column[Date]("NEXTDATE", O NotNull)

  def durationMins = column[Int]("DURATIONMINS", O NotNull)

  def repeatDays = column[Int]("REPEATDAYS")

  def * = id.? ~ name ~ nextDate ~ durationMins ~ repeatDays.? <>((apply _).tupled, unapply _)

  def query = Query(this)

  def findAll: Seq[Alarm] = database.withSession {
    implicit db: Session =>
      query.orderBy(nextDate.asc).list
  }

  def findAllForToday(): Seq[Alarm] = database.withSession {
    implicit db: Session =>
      val today = DateMidnight.now
      val tomorrow = today.plusDays(1)

      query.where(a => a.nextDate >= today.toDate && a.nextDate < tomorrow.toDate)
        .orderBy(nextDate.asc).list
  }

  def findById(alarmId: Long): Option[Alarm] = database.withSession {
    implicit db: Session =>
      query.where(a => a.id === alarmId).firstOption
  }
}
