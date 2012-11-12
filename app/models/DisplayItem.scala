package models

import play.api.libs.json.Json
import widgetConfigs._
import play.api.db._
import play.api.Play.current

import org.scalaquery.ql.TypeMapper._
import org.scalaquery.ql.extended.{ExtendedTable => Table}

import org.scalaquery.ql.extended.H2Driver.Implicit._

import org.scalaquery.session.{Database, Session}
import org.scalaquery.ql.Query
import scala.Some

case class DisplayItem(
                        id: Option[Long],
                        displayId: Long,
                        posx: Int,
                        posy: Int,
                        width: Int,
                        height: Int,
                        widgetNum: Int,
                        widgetConfigJson: String) {

  def this() = this(None, 0, 0, 0, 0, 0, 0, "{}")

  def widget: DisplayWidgets.Type = DisplayWidgets(widgetNum)

  def widgetConfig = Json.parse(widgetConfigJson)

  def burndownChartConfig = {
    if (widget == DisplayWidgets.BurndownChart) {
      Some(Json.fromJson[BurndownChartConfig](widgetConfig))
    } else {
      None
    }
  }

  def sprintTitleConfig = {
    if (widget == DisplayWidgets.SprintTitle) {
      Some(Json.fromJson[SprintTitleConfig](widgetConfig))
    } else {
      None
    }
  }

  def clockConfig = {
    if (widget == DisplayWidgets.Clock) {
      Some(Json.fromJson[ClockConfig](widgetConfig))
    } else {
      None
    }
  }

  def alarmsConfig = {
    if (widget == DisplayWidgets.Alarms) {
      Some(Json.fromJson[AlarmsConfig](widgetConfig))
    } else {
      None
    }
  }

  def iframeConfig = {
    if (widget == DisplayWidgets.IFrame) {
      Some(Json.fromJson[IFrameConfig](widgetConfig))
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

  def widgetNum = column[Int]("WIDGETNUM", O NotNull)

  def widgetConfigJson = column[String]("WIDGETCONFIGJSON", O NotNull)

  def * = id.? ~ displayId ~ posx ~ posy ~ width ~ height ~ widgetNum ~ widgetConfigJson <>((apply _).tupled, unapply _)

  def formApply(id: Option[Long],
                displayId: Long,
                posx: Int,
                posy: Int,
                width: Int,
                height: Int,
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

    DisplayItem(id, displayId, posx, posy, width, height, widgetNum, Json.stringify(widgetConfig))
  }

  def formUnapply(displayItem: DisplayItem) =
    Some(
      displayItem.id,
      displayItem.displayId,
      displayItem.posx,
      displayItem.posy,
      displayItem.width,
      displayItem.height,
      displayItem.widgetNum,
      displayItem.burndownChartConfig,
      displayItem.sprintTitleConfig,
      displayItem.clockConfig,
      displayItem.alarmsConfig,
      displayItem.iframeConfig)

  def query = Query(this)

  def findAllForDisplay(displayId: Long) = database.withSession {
    implicit db: Session =>
      query.where(d => d.displayId === displayId).orderBy(id.asc).list
  }

  def findById(displayItemId: Long) = database.withSession {
    implicit db: Session =>
      query.where(d => d.id === displayItemId).firstOption
  }

}