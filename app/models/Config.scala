package models

import config.BackupConfig
import play.api.db._
import play.api.Play.current
import slick.driver.H2Driver.simple._
import play.api.libs.json.{JsValue, Json}

case class Config(id: Int,
                  valueJson: String) {
  def this(configType: ConfigTypes.Type, configValue: JsValue) = this(configType.id, Json.stringify(configValue))

  def configType = ConfigTypes(id)

  def configValue = Json.parse(valueJson)

  def backupConfig = {
    if (configType == ConfigTypes.Backup)
      Json.fromJson[BackupConfig](configValue).asOpt
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

  def id = column[Int]("ID", O.PrimaryKey)

  def valueJson = column[String]("VALUEJSON", O.NotNull)

  def * = id ~ valueJson <>((apply _).tupled, unapply _)

  def query = Query(this)

  def findByType(configType: ConfigTypes.Type): Option[Config] = database.withSession {
    implicit db: Session =>
      query.where(c => c.id === configType.id).firstOption
  }
}
