package models

import play.api.db._
import play.api.Play.current
import slick.driver.H2Driver.simple._
import java.util.Date
import play.api.libs.json._
import models.statusValues.{FreestyleStatus, MetricStatus, BuildStatus, HostsStatus}
import globals.Global
import models.DateMapper.date2timestamp
import play.api.libs.json.JsObject

case class StatusValue(id: Option[Long],
                       statusMonitorId: Long,
                       statusNum: Int,
                       retrievedAt: Date,
                       valuesJson: String) {

  def this(statusMonitorId: Long, status: StatusTypes.Type, json: JsValue) =
    this(None, statusMonitorId, status.id, new Date(), Json.stringify(json))

  def status = StatusTypes(statusNum)

  def statusValues = Json.parse(valuesJson)

  def buildStatus = {
    if (status != StatusTypes.Unknown) {
      Json.fromJson[BuildStatus](statusValues).asOpt
    } else {
      None
    }
  }

  def hostsStatus = {
    if (status != StatusTypes.Unknown) {
      Json.fromJson[HostsStatus](statusValues).asOpt
    } else {
      None
    }
  }

  def metricStatus = {
    if (status != StatusTypes.Unknown) {
      Json.fromJson[MetricStatus](statusValues).asOpt
    } else {
      None
    }
  }

  def freestyleStatus = {
    if (status != StatusTypes.Unknown) {
      Json.fromJson[FreestyleStatus](statusValues).asOpt
    } else {
      None
    }
  }

  def insert = {
    StatusValue.database.withSession {
      implicit db: Session =>
        StatusValue.insert(this)
    }
    Global.displayUpdater ! this
  }

  def update = {
    StatusValue.database.withSession {
      implicit db: Session =>
        StatusValue.where(_.id === id).update(this)
    }
    Global.displayUpdater ! this
  }

  def delete = {
    StatusValue.database.withSession {
      implicit db: Session =>
        StatusValue.where(_.id === id).delete
    }
    Global.displayUpdater ! this
  }
}

object StatusValue extends Table[StatusValue]("STATUSVALUE") {
  def database = Database.forDataSource(DB.getDataSource())

  def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)

  def statusMonitorId = column[Long]("STATUSMONITORID", O.NotNull)

  def statusNum = column[Int]("STATUSNUM", O.NotNull)

  def retrievedAt = column[Date]("RETRIEVEDAT", O.NotNull)

  def valuesJson = column[String]("VALUESJSON", O.NotNull)

  def * = id.? ~ statusMonitorId ~ statusNum ~ retrievedAt ~ valuesJson <>((apply _).tupled, unapply _)

  def query = Query(this)

  def findAllForStatusMonitor(statusMonitorId: Long): Seq[StatusValue] = database.withSession {
    implicit db: Session =>
      query.where(s => s.statusMonitorId === statusMonitorId).sortBy(s => s.id.desc).list
  }

  def findLastForStatusMonitor(statusMonitorId: Long): Option[StatusValue] = database.withSession {
    implicit db: Session =>
      query.where(s => s.statusMonitorId === statusMonitorId).sortBy(s => s.id.desc).firstOption
  }

  implicit val jsonWrites = new Writes[StatusValue] {
    def writes(statusValue: StatusValue) = JsObject(
      statusValue.id.map("id" -> JsNumber(_)).toSeq ++
        Seq(
          "statusMonitorId" -> JsNumber(statusValue.statusMonitorId),
          "statusNum" -> JsNumber(statusValue.statusNum),
          "retrievedAt" -> JsNumber(statusValue.retrievedAt.getTime),
          "values" -> statusValue.statusValues
        )
    )
  }
}