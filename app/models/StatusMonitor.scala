package models

import play.api.db._
import play.api.Play.current
import slick.driver.H2Driver.simple._
import java.util.Date
import play.api.libs.json.Json
import models.statusMonitors.{FreestyleConfig, IcingaConfig}
import globals.Global
import models.DateMapper.date2timestamp
import scala.collection.mutable

case class StatusMonitor(id: Option[Long],
                         projectId: Long,
                         name: String,
                         typeNum: Int,
                         url: String,
                         username: Option[String],
                         password: Option[String],
                         active: Boolean,
                         keepHistory: Int,
                         updatePeriod: Int,
                         lastQueried: Option[Date],
                         lastUpdated: Option[Date],
                         configJson: Option[String]) {
  def this(projectId: Long) = this(None, projectId, "", 0, "", None, None, true, 10, 60, None, None, None)

  def config = configJson.map(Json.parse(_))

  def monitorType = StatusMonitorTypes(typeNum)

  def icingaConfig: Option[IcingaConfig] = {
    if (monitorType == StatusMonitorTypes.Icinga)
      config.flatMap(Json.fromJson[IcingaConfig](_).asOpt)
    else
      None
  }

  def freestyleConfig: Option[FreestyleConfig] = {
    if (monitorType == StatusMonitorTypes.Freestyle)
      config.flatMap(Json.fromJson[FreestyleConfig](_).asOpt)
    else
      None
  }

  def insert = {
    StatusMonitor.database.withSession {
      implicit db: Session =>
        StatusMonitor.insert(this)
    }
    Global.displayUpdater ! this
  }

  def update = {
    StatusMonitor.database.withSession {
      implicit db: Session =>
        StatusMonitor.where(_.id === id).update(this)
    }
    Global.displayUpdater ! this
  }

  def updateLastQueried = {
    StatusMonitor.database.withSession {
      implicit db: Session =>
        StatusMonitor.where(_.id === id).map(_.lastQueried).update(new Date)
    }
  }

  def updateLastUpdated = {
    StatusMonitor.database.withSession {
      implicit db: Session =>
        StatusMonitor.where(_.id === id).map(_.lastUpdated).update(new Date)
    }
  }


  def delete = {
    StatusMonitor.database.withSession {
      implicit db: Session =>
        StatusValue.where(_.statusMonitorId === id.get).delete
        StatusMonitor.where(_.id === id).delete
    }
    Global.displayUpdater ! this
  }
}

object StatusMonitor extends Table[StatusMonitor]("STATUSMONITOR") {
  def database = Database.forDataSource(DB.getDataSource())

  def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)

  def projectId = column[Long]("PROJECTID", O.NotNull)

  def name = column[String]("NAME", O.NotNull)

  def typeNum = column[Int]("TYPENUM", O.NotNull)

  def url = column[String]("URL", O.NotNull)

  def username = column[String]("USERNAME")

  def password = column[String]("PASSWORD")

  def active = column[Boolean]("ACTIVE", O.NotNull)

  def keepHistory = column[Int]("KEEPHISTORY", O.NotNull)

  def updatePeriod = column[Int]("UPDATEPERIOD", O.NotNull)

  def lastQueried = column[Date]("LASTQUERIED")

  def lastUpdated = column[Date]("LASTUPDATED")

  def configJson = column[String]("CONFIGJSON")

  def * = id.? ~ projectId ~ name ~ typeNum ~ url ~ username.? ~ password.? ~ active ~ keepHistory ~ updatePeriod ~ lastQueried.? ~ lastUpdated.? ~ configJson.? <>((apply _).tupled, unapply _)

  def formApply(id: Option[Long],
                projectId: Long,
                name: String,
                typeNum: Int,
                url: String,
                username: Option[String],
                password: Option[String],
                active: Boolean,
                keepHistory: Int,
                updatePeriod: Int,
                lastQueried: Option[Date],
                lastUpdated: Option[Date],
                icingaConfig: Option[IcingaConfig],
                freestyleConfig: Option[FreestyleConfig]): StatusMonitor = {
    val config = StatusMonitorTypes(typeNum) match {
      case StatusMonitorTypes.Icinga =>
        Some(Json.toJson(icingaConfig.getOrElse(IcingaConfig())))
      case StatusMonitorTypes.Freestyle =>
        Some(Json.toJson(freestyleConfig.getOrElse(FreestyleConfig())))
      case _ =>
        None
    }
    StatusMonitor(id, projectId, name, typeNum, url, username, password, active, keepHistory, updatePeriod,
      lastQueried, lastUpdated, config.map(Json.stringify(_)))
  }

  def formUnapply(statusMonitor: StatusMonitor) =
    Some(statusMonitor.id,
      statusMonitor.projectId,
      statusMonitor.name,
      statusMonitor.typeNum,
      statusMonitor.url,
      statusMonitor.username,
      statusMonitor.password,
      statusMonitor.active,
      statusMonitor.keepHistory,
      statusMonitor.updatePeriod,
      statusMonitor.lastQueried,
      statusMonitor.lastUpdated,
      statusMonitor.icingaConfig,
      statusMonitor.freestyleConfig)

  def query = Query(this)

  def findAll: Seq[StatusMonitor] = database.withSession {
    implicit db: Session =>
      query.sortBy(s => s.name.asc).list
  }

  def findAllGroupedByType(projectId: Long): Map[StatusMonitorTypes.Type, Seq[StatusMonitor]] = {
    val byTypeBuilders = StatusMonitorTypes.values.toSeq.map {
      statusMonitorType =>
        statusMonitorType -> Seq.newBuilder[StatusMonitor]
    }.toMap

    finaAllForProject(projectId).foreach {
      statusMonitor =>
        byTypeBuilders(statusMonitor.monitorType) += statusMonitor
    }

    byTypeBuilders.mapValues(_.result())
  }

  def findAllActive: Seq[StatusMonitor] = database.withSession {
    implicit db: Session =>
      query.where(s => s.active).sortBy(s => s.name.asc).list
  }

  def findById(statusMonitorId: Long): Option[StatusMonitor] = database.withSession {
    implicit db: Session =>
      query.where(s => s.id === statusMonitorId).firstOption
  }

  def finaAllForProject(projectId: Long) = database.withSession {
    implicit db: Session =>
      query.where(s => s.projectId === projectId).sortBy(s => s.name.asc).list
  }

  def finaAllForProject(projectId: Long, types: Seq[StatusMonitorTypes.Type]): Seq[StatusMonitor] = database.withSession {
    implicit db: Session =>
      query.where(s => s.projectId === projectId && s.active && s.typeNum.inSet(types.map(_.id))).sortBy(s => s.name.asc).list
  }
}
