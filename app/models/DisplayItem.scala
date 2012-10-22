package models

import json.SprintCounter
import org.squeryl.KeyedEntity
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.annotations.Transient
import play.api.libs.json.{JsValue, Json}

class DisplayItem(val id: Long,
                  val displayId: Long,
                  var posx: Int,
                  var posy: Int,
                  var width: Int,
                  var height: Int,
                  var widgetNum: Int,
                  var widgetConfigJson: String) extends KeyedEntity[Long] {

  def this() = this(0, 0, 0, 0, 0, 0, 0, "{}")

  @Transient
  def widget: DisplayWidgets.Type = DisplayWidgets(widgetNum)

  def widget_=(displayWidget: DisplayWidgets.Type) = {
    widgetNum = displayWidget.id
  }

  @Transient
  def widgetConfig = Json.parse(widgetConfigJson)

  def widgetConfig_=(widgetConfig: JsValue) = {
    widgetConfigJson = Json.stringify(widgetConfig)
  }

  def save = inTransaction {
    WallDeeSchema.displayItems.insertOrUpdate(this)
  }

  def delete = inTransaction {
    if (isPersisted) {
      WallDeeSchema.displayItems.delete(id)
    }
  }
}

object DisplayItem {
  def findAllForDisplay(displayId: Long) = inTransaction {
    from(WallDeeSchema.displayItems)(d => where(d.displayId === displayId) select (d) orderBy (d.id asc)).toList
  }

}