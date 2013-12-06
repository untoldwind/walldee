package models

import play.api.libs.json.{JsNumber, JsObject, Writes}
import widgetConfigs._
import play.api.db._
import play.api.Play.current
import slick.driver.H2Driver.simple._
import scala.Some
import globals.Global

case class DisplayItem(id: Option[Long] = None,
                       displayId: Long,
                       posx: Int,
                       posy: Int,
                       width: Int,
                       height: Int,
                       widgetNum: Int,
                       projectId: Option[Long],
                       teamId: Option[Long],
                       appearsInFeed: Boolean,
                       hidden: Boolean,
                       widgetConfigJson: String) {

  def this() = this(None, 0, 0, 0, 0, 0, 0, None, None, false, false, "{}")

  def widget: DisplayWidgets.Type = DisplayWidgets(widgetNum)

  def widgetConfig[T <: WidgetConfig](implicit manifest: Manifest[T]): Option[T] = {
    widget.jsonToConfig(widgetConfigJson) match {
      case config if manifest.runtimeClass.isInstance(config) => Some(config.asInstanceOf[T])
      case _ => None

    }
  }

  def insert = {
    val insertedId = DisplayItem.database.withSession {
      implicit db: Session =>
        DisplayItem.insert(this)
        Query(DisplayItem.seqID).first
    }
    val result = this.copy(id = Some(insertedId))
    Global.displayUpdater ! result
    result
  }

  def update: Boolean = {
    if (DisplayItem.database.withSession {
      implicit db: Session =>
        DisplayItem.where(_.id === id).update(this) == 1
    }) {
      Global.displayUpdater ! this
      true
    } else
      false
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

  def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)

  def displayId = column[Long]("DISPLAYID", O.NotNull)

  def posx = column[Int]("POSX", O.NotNull)

  def posy = column[Int]("POSY", O.NotNull)

  def width = column[Int]("WIDTH", O.NotNull)

  def height = column[Int]("HEIGHT", O.NotNull)

  def widgetNum = column[Int]("WIDGETNUM", O.NotNull)

  def projectId = column[Long]("PROJECTID", O.Nullable)

  def teamId = column[Long]("TEAMID")

  def appearsInFeed = column[Boolean]("APPEARSINFEED", O.NotNull)

  def hidden = column[Boolean]("HIDDEN", O.NotNull)

  def widgetConfigJson = column[String]("WIDGETCONFIGJSON", O.NotNull)

  def * = id.? ~ displayId ~ posx ~ posy ~ width ~ height ~ widgetNum ~ projectId.? ~ teamId.? ~
    appearsInFeed ~ hidden ~ widgetConfigJson <>((apply _).tupled, unapply _)

  def formApply(id: Option[Long],
                displayId: Long,
                posx: Int,
                posy: Int,
                width: Int,
                height: Int,
                projectId: Option[Long],
                teamId: Option[Long],
                appearsInFeed: Boolean,
                hidden: Boolean,
                widgetConfig: (DisplayWidgets.Type, WidgetConfig)): DisplayItem = {

    DisplayItem(id, displayId, posx, posy, width, height, widgetConfig._1.id, projectId, teamId, appearsInFeed,
      hidden, widgetConfig._1.configToJson(widgetConfig._2))
  }

  def formUnapply(displayItem: DisplayItem) =
    Some(
      displayItem.id,
      displayItem.displayId,
      displayItem.posx,
      displayItem.posy,
      displayItem.width,
      displayItem.height,
      displayItem.projectId,
      displayItem.teamId,
      displayItem.appearsInFeed,
      displayItem.hidden,
      (displayItem.widget, displayItem.widgetConfig[WidgetConfig].getOrElse(displayItem.widget.configMappger.default)))

  def query = Query(this)

  def findAllOfWidgetType(widgetType: DisplayWidgets.Type) = database.withSession {
    implicit db: Session =>
      query.where(d => d.widgetNum === widgetType.id).sortBy(d => d.id.asc).list
  }

  def findAllForDisplay(displayId: Long): Seq[DisplayItem] = database.withSession {
    implicit db: Session =>
      query.where(d => d.displayId === displayId).sortBy(d => d.id.asc).list
  }

  def findAllForDisplayFeed(displayId: Long): Seq[DisplayItem] = database.withSession {
    implicit db: Session =>
      query.where(d => d.displayId === displayId && d.appearsInFeed).sortBy(d => d.id.asc).list
  }

  def findAllForTeam(teamId: Long): Seq[DisplayItem] = database.withSession {
    implicit db: Session =>
      query.where(d => d.teamId === teamId).sortBy(d => d.id.asc).list
  }

  def findAllForProject(projectId: Long): Seq[DisplayItem] = database.withSession {
    implicit db: Session =>
      query.where(d => d.projectId === projectId).sortBy(d => d.id.asc).list
  }

  def findById(displayItemId: Long): Option[DisplayItem] = database.withSession {
    implicit db: Session =>
      query.where(d => d.id === displayItemId).firstOption
  }

  implicit val jsonWrites = new Writes[DisplayItem] {
    def writes(displayItem: DisplayItem) = JsObject(
      displayItem.id.map("id" -> JsNumber(_)).toSeq ++
        displayItem.projectId.map("projectId" -> JsNumber(_)).toSeq ++
        displayItem.teamId.map("teamId" -> JsNumber(_)).toSeq ++
        Seq(
          "displayId" -> JsNumber(displayItem.displayId),
          "posx" -> JsNumber(displayItem.posx),
          "posy" -> JsNumber(displayItem.posy),
          "width" -> JsNumber(displayItem.width),
          "height" -> JsNumber(displayItem.height)
        ))
  }
}