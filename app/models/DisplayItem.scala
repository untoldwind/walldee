package models

import play.api.libs.json.Json
import widgetConfigs._
import play.api.db._
import play.api.Play.current
import scala.slick.driver.H2Driver.simple._
import scala.Some
import globals.Global

case class DisplayItem(
                        id: Option[Long],
                        displayId: Long,
                        posx: Int,
                        posy: Int,
                        width: Int,
                        height: Int,
                        styleNum: Int,
                        widgetNum: Int,
                        widgetConfigJson: String) {

  def this() = this(None, 0, 0, 0, 0, 0, 0, 0, "{}")

  def widget: DisplayWidgets.Type = DisplayWidgets(widgetNum)

  def style: DisplayStyles.Type = DisplayStyles(styleNum)

  def widgetConfig = Json.parse(widgetConfigJson)

  def burndownChartConfig = {
    if (widget == DisplayWidgets.BurndownChart) {
      Json.fromJson[BurndownChartConfig](widgetConfig).asOpt
    } else {
      None
    }
  }

  def sprintTitleConfig = {
    if (widget == DisplayWidgets.SprintTitle) {
      Json.fromJson[SprintTitleConfig](widgetConfig).asOpt
    } else {
      None
    }
  }

  def clockConfig = {
    if (widget == DisplayWidgets.Clock) {
      Json.fromJson[ClockConfig](widgetConfig).asOpt
    } else {
      None
    }
  }

  def alarmsConfig = {
    if (widget == DisplayWidgets.Alarms) {
      Json.fromJson[AlarmsConfig](widgetConfig).asOpt
    } else {
      None
    }
  }

  def iframeConfig = {
    if (widget == DisplayWidgets.IFrame) {
      Json.fromJson[IFrameConfig](widgetConfig).asOpt
    } else {
      None
    }
  }

  def buildStatusConfig = {
    if (widget == DisplayWidgets.BuildStatus) {
      Json.fromJson[BuildStatusConfig](widgetConfig).asOpt
    } else {
      None
    }
  }

  def hostStatusConfig = {
    if (widget == DisplayWidgets.HostStatus) {
      Json.fromJson[HostStatusConfig](widgetConfig).asOpt
    } else {
      None
    }
  }

  def metricsConfig = {
    if (widget == DisplayWidgets.Metrics) {
      Json.fromJson[MetricsConfig](widgetConfig).asOpt
    } else {
      None
    }
  }

  def insert = {
    val insertedId = DisplayItem.database.withSession {
      implicit db: Session =>
        DisplayItem.insert(this)
        Query(DisplayItem.seqID).first
    }
    val result = DisplayItem(Some(insertedId), displayId, posx, posy, width, height, styleNum, widgetNum, widgetConfigJson)
    Global.displayUpdater ! result
    result
  }

  def update = {
    DisplayItem.database.withSession {
      implicit db: Session =>
        DisplayItem.where(_.id === id).update(this)
    }
    Global.displayUpdater ! this
  }

  def delete = {
    DisplayItem.database.withSession {
      implicit db: Session =>
        DisplayItem.where(_.id === id).delete
    }
    Global.displayUpdater ! this
  }
}

object DisplayItem extends Table[DisplayItem]("DISPLAYITEM") {
  lazy val seqID = SimpleFunction.nullary[Long]("identity")

  lazy val database = Database.forDataSource(DB.getDataSource())

  def id = column[Long]("ID", O PrimaryKey, O AutoInc)

  def displayId = column[Long]("DISPLAYID", O NotNull)

  def posx = column[Int]("POSX", O NotNull)

  def posy = column[Int]("POSY", O NotNull)

  def width = column[Int]("WIDTH", O NotNull)

  def height = column[Int]("HEIGHT", O NotNull)

  def styleNum = column[Int]("STYLENUM", O NotNull)

  def widgetNum = column[Int]("WIDGETNUM", O NotNull)

  def widgetConfigJson = column[String]("WIDGETCONFIGJSON", O NotNull)

  def * = id.? ~ displayId ~ posx ~ posy ~ width ~ height ~ styleNum ~ widgetNum ~ widgetConfigJson <>((apply _).tupled, unapply _)

  def formApply(id: Option[Long],
                displayId: Long,
                posx: Int,
                posy: Int,
                width: Int,
                height: Int,
                styleNum: Int,
                widgetNum: Int,
                burndownChartConfig: Option[BurndownChartConfig],
                sprintTitleConfig: Option[SprintTitleConfig],
                clockConfig: Option[ClockConfig],
                alarmsConfig: Option[AlarmsConfig],
                iframeConfig: Option[IFrameConfig],
                buildStatusConfig: Option[BuildStatusConfig],
                hostStatusConfig: Option[HostStatusConfig],
                metricsConfig: Option[MetricsConfig]): DisplayItem = {

    val widgetConfig = DisplayWidgets(widgetNum) match {
      case DisplayWidgets.BurndownChart => Json.toJson(burndownChartConfig.getOrElse(BurndownChartConfig()))
      case DisplayWidgets.SprintTitle => Json.toJson(sprintTitleConfig.getOrElse(SprintTitleConfig()))
      case DisplayWidgets.Clock => Json.toJson(clockConfig.getOrElse(ClockConfig()))
      case DisplayWidgets.Alarms => Json.toJson(alarmsConfig.getOrElse(AlarmsConfig()))
      case DisplayWidgets.IFrame => Json.toJson(iframeConfig.getOrElse(IFrameConfig()))
      case DisplayWidgets.BuildStatus => Json.toJson(buildStatusConfig.getOrElse(BuildStatusConfig()))
      case DisplayWidgets.HostStatus => Json.toJson(hostStatusConfig.getOrElse(HostStatusConfig()))
      case DisplayWidgets.Metrics => Json.toJson(metricsConfig.getOrElse(MetricsConfig()))
    }

    DisplayItem(id, displayId, posx, posy, width, height, styleNum, widgetNum, Json.stringify(widgetConfig))
  }

  def formUnapply(displayItem: DisplayItem) =
    Some(
      displayItem.id,
      displayItem.displayId,
      displayItem.posx,
      displayItem.posy,
      displayItem.width,
      displayItem.height,
      displayItem.styleNum,
      displayItem.widgetNum,
      displayItem.burndownChartConfig,
      displayItem.sprintTitleConfig,
      displayItem.clockConfig,
      displayItem.alarmsConfig,
      displayItem.iframeConfig,
      displayItem.buildStatusConfig,
      displayItem.hostStatusConfig,
      displayItem.metricsConfig)

  def query = Query(this)

  def findAllForDisplay(displayId: Long): Seq[DisplayItem] = database.withSession {
    implicit db: Session =>
      query.where(d => d.displayId === displayId).sortBy(d => d.id.asc).list
  }

  def findById(displayItemId: Long): Option[DisplayItem] = database.withSession {
    implicit db: Session =>
      query.where(d => d.id === displayItemId).firstOption
  }

}