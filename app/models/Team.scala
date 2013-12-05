package models

import play.api.Play.current
import slick.driver.H2Driver.simple._
import play.api.db.DB
import globals.Global
import play.api.libs.json._
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsObject
import play.api.libs.json.JsNumber

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

  def update: Boolean = {
    if (Team.database.withSession {
      implicit db: Session =>
        Team.where(_.id === id).update(this) == 1
    }) {
      Global.displayUpdater ! this
      true
    } else {
      false
    }
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

  def forInsert = id.? ~ name ~ currentSprintId.? <>(apply _, unapply _) returning id

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

  implicit val jsonWrites = new Writes[Team] {
    override def writes(team: Team): JsValue = JsObject(
      team.id.map("id" -> JsNumber(_)).toSeq ++
        Seq(
          "name" -> JsString(team.name)
        ) ++
        team.currentSprintId.map("currentSprintId" -> JsNumber(_)).toSeq)
  }

  def jsonReads(teamId: Option[Long]): Reads[Team] = new Reads[Team] {
    override def reads(json: JsValue): JsResult[Team] =
      JsSuccess(Team(
        teamId,
        (json \ "name").as[String],
        (json \ "currentSprintId").asOpt[Long]))
  }
}