package models

import config.BackupConfig
import play.api.db._
import play.api.Play.current

import org.scalaquery.ql.TypeMapper._
import org.scalaquery.ql.extended.{ExtendedTable => Table}

import org.scalaquery.ql.extended.H2Driver.Implicit._

import org.scalaquery.session.{Database, Session}
import org.scalaquery.ql.Query
import play.api.libs.json.{JsValue, Json}

case class Config(id: Int,
                  valueJson: String) {
  def this(configType: ConfigTypes.Type, configValue: JsValue) = this(configType.id, Json.stringify(configValue))

  def configType = ConfigTypes(id)

  def configValue = Json.parse(valueJson)

  def backupConfig = {
    if (configType == ConfigTypes.Backup)
      Some(Json.fromJson[BackupConfig](configValue))
    else
      None
  }

  def insert = Config.database.withSession {
    implicit db: Session =>
      Config.insert(this)
  }

  def update = Config.database.withSession {
    implicit db: Session =>
      Config.where(_.id === id).update(this)
  }

  def delete = Config.database.withSession {
    implicit db: Session =>
      Config.where(_.id === id).delete
  }

}

object Config extends Table[Config]("CONFIG") {
  lazy val database = Database.forDataSource(DB.getDataSource())

  def id = column[Int]("ID", O PrimaryKey)

  def valueJson = column[String]("VALUEJSON", O NotNull)

  def * = id ~ valueJson <>((apply _).tupled, unapply _)

  def query = Query(this)

  def findByType(configType: ConfigTypes.Type): Option[Config] = database.withSession {
    implicit db: Session =>
      query.where(c => c.id === configType.id).firstOption
  }
}
