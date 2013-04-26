package models

import java.sql.Date
import play.api.libs.json.Json
import org.joda.time.{DateTimeConstants, DateMidnight}
import play.api.db._
import play.api.Play.current
import slick.driver.H2Driver.simple._
import org.joda.time.format.DateTimeFormat
import java.util.Locale
import sprints.SprintCounter
import globals.Global

case class Sprint(id: Option[Long],
                  teamId: Long,
                  title: String,
                  num: Int,
                  sprintStart: Date,
                  sprintEnd: Date,
                  languageTag: String,
                  countersJson: String) {

  def this(teamId: Long) = this(None, teamId, "", 0,
    new Date(System.currentTimeMillis()), new Date(System.currentTimeMillis()), "de-DE", "[]")

  def counters = Json.fromJson[Seq[SprintCounter]](Json.parse(countersJson)).getOrElse(Seq.empty)

  def locale = Locale.forLanguageTag(languageTag)

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

  lazy val dayLabels = {
    var result = Seq.newBuilder[String]
    var formatter = DateTimeFormat.forPattern("E").withLocale(locale)
    var current = new DateMidnight(sprintStart.getTime)
    val end = new DateMidnight(sprintEnd.getTime)
    var numberOfDays = 0

    while (current.compareTo(end) <= 0) {
      if (current.getDayOfWeek != DateTimeConstants.SATURDAY && current.getDayOfWeek != DateTimeConstants.SUNDAY) {
        result += formatter.print(current)
      }
      current = current.plusDays(1)
    }
    result.result()
  }

  def insert = {
    Sprint.database.withSession {
      implicit db: Session =>
        Sprint.insert(this)
    }
  }

  def update = {
    Sprint.database.withSession {
      implicit db: Session =>
        Sprint.where(_.id === id).update(this)
    }
    Global.displayUpdater ! this
  }

  def delete = {
    Sprint.database.withTransaction {
      implicit db: Session =>
        Story.where(s => s.sprintId === id).delete
        DayCount.where(d => d.sprintId === id).delete
        Sprint.where(_.id === id).delete
    }
    Global.displayUpdater ! this
  }
}

object Sprint extends Table[Sprint]("SPRINT") {
  lazy val database = Database.forDataSource(DB.getDataSource())

  def id = column[Long]("ID", O PrimaryKey, O AutoInc)

  def teamId = column[Long]("TEAMID")

  def title = column[String]("TITLE", O NotNull)

  def num = column[Int]("NUM", O NotNull)

  def sprintStart = column[Date]("SPRINTSTART", O NotNull)

  def sprintEnd = column[Date]("SPRINTEND", O NotNull)

  def languageTag = column[String]("LANGUAGETAG", O NotNull)

  def countersJson = column[String]("COUNTERSJSON", O NotNull)

  def * = id.? ~ teamId ~ title ~ num ~ sprintStart ~ sprintEnd ~ languageTag ~ countersJson <>((apply _).tupled, unapply _)

  def formApply(id: Option[Long],
                teamId: Long,
                title: String,
                num: Int,
                sprintStart: Date,
                sprintEnd: Date,
                languageTag: String,
                counters: List[SprintCounter]) =
    Sprint(id, teamId, title, num, sprintStart, sprintEnd, languageTag, Json.stringify(Json.toJson(counters)))

  def formUnapply(sprint: Sprint) =
    Some(sprint.id,
      sprint.teamId,
      sprint.title,
      sprint.num,
      sprint.sprintStart,
      sprint.sprintEnd,
      sprint.languageTag,
      sprint.counters.toList)

  def query = Query(this)

  def findAll: Seq[Sprint] = database.withSession {
    implicit db: Session =>
      query.sortBy(s => s.num.asc).list
  }

  def findAllForTeam(teamId: Long) = database.withSession {
    implicit db: Session =>
      query.where(s => s.teamId === teamId).sortBy(s => s.num.asc).list
  }

  def findById(sprintId: Long): Option[Sprint] = database.withSession {
    implicit db: Session =>
      query.where(s => s.id === sprintId).firstOption
  }
}