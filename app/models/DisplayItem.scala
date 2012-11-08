package models

import play.api.libs.json.Json
import widgetConfigs._
import play.api.db._
import play.api.Play.current

import scala.slick.driver.H2Driver.simple._

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
      Some(Json.fromJson[BurndownChartConfig](widgetConfig).getOrElse(BurndownChartConfig()))
    } else {
      None
    }
  }

  def sprintTitleConfig = {
    if (widget == DisplayWidgets.SprintTitle) {
      Some(Json.fromJson[SprintTitleConfig](widgetConfig).getOrElse(SprintTitleConfig()))
    } else {
      None
    }
  }

  def clockConfig = {
    if (widget == DisplayWidgets.Clock) {
      Some(Json.fromJson[ClockConfig](widgetConfig).getOrElse(ClockConfig()))
    } else {
      None
    }
  }

  def alarmsConfig = {
    if (widget == DisplayWidgets.Alarms) {
      Some(Json.fromJson[AlarmsConfig](widgetConfig).getOrElse(AlarmsConfig()))
    } else {
      None
    }
  }

  def iframeConfig = {
    if (widget == DisplayWidgets.IFrame) {
      Some(Json.fromJson[IFrameConfig](widgetConfig).getOrElse(IFrameConfig()))
    } else {
      None
    }
  }

  def insert = DisplayItem.database.withSession {
    implicit db: Session =>
      DisplayItem.insert(this)
  }

  def update = DisplayItem.database.withSession {
    implicit db: Session =>
      DisplayItem.where(_.id === id).update(this)
  }

  def delete = DisplayItem.database.withSession {
    implicit db: Session =>
      DisplayItem.where(_.id === id).delete
  }
}

object DisplayItem extends Table[DisplayItem]("DISPLAYITEM") {
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
                iframeConfig: Option[IFrameConfig]): DisplayItem = {

    val widgetConfig = DisplayWidgets(widgetNum) match {
      case DisplayWidgets.BurndownChart => Json.toJson(burndownChartConfig.getOrElse(BurndownChartConfig()))
      case DisplayWidgets.SprintTitle => Json.toJson(sprintTitleConfig.getOrElse(SprintTitleConfig()))
      case DisplayWidgets.Clock => Json.toJson(clockConfig.getOrElse(ClockConfig()))
      case DisplayWidgets.Alarms => Json.toJson(alarmsConfig.getOrElse(AlarmsConfig()))
      case DisplayWidgets.IFrame => Json.toJson(iframeConfig.getOrElse(IFrameConfig()))
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
      displayItem.iframeConfig)

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