package models.utils

import play.api.templates.Html
import play.api.libs.json._
import models.sprints.SprintCounter
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import models.DisplayItem

case class RenderedWidget(displayItem: DisplayItem,
                          content: Html) {
  val id = "displayItem-%d".format(displayItem.id.get)
  val posx = displayItem.posx
  val posy = displayItem.posy
  val width = displayItem.width
  val height = displayItem.height
  val style = displayItem.style.toString.toLowerCase

  lazy val etag = {
    val dataDigest = new DataDigest

    dataDigest.update(id)
    dataDigest.update(posx)
    dataDigest.update(posy)
    dataDigest.update(width)
    dataDigest.update(height)
    dataDigest.update(style)
    dataDigest.update(content.toString())
    dataDigest.base64Digest()
  }
}

object RenderedWidget {

  implicit object RenderedWidgetFormat extends Writes[RenderedWidget] {

    override def writes(renderedWidget: RenderedWidget): JsValue = JsObject(Seq(
      "id" -> JsString(renderedWidget.id),
      "posx" -> JsNumber(renderedWidget.posx),
      "posy" -> JsNumber(renderedWidget.posy),
      "width" -> JsNumber(renderedWidget.width),
      "height" -> JsNumber(renderedWidget.height),
      "style" -> JsString(renderedWidget.style),
      "etag" -> JsString(renderedWidget.etag),
      "content" -> JsString(renderedWidget.content.toString)))
  }

}
