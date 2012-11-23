package models.utils

import play.api.templates.Html
import play.api.libs.json._
import models.sprints.SprintCounter
import play.api.libs.json.JsObject
import play.api.libs.json.JsString

case class RenderedWidget(var posx: Int,
                          var posy: Int,
                          var width: Int,
                          var height: Int,
                          var style: String,
                          var content: Html) {
  lazy val etag = {
    val dataDigest = new DataDigest

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

  implicit object SprintCounterFormat extends Writes[RenderedWidget] {

    override def writes(renderedWidget: RenderedWidget): JsValue = JsObject(Seq(
      "posx" -> JsNumber(renderedWidget.posx),
      "posy" -> JsNumber(renderedWidget.posy),
      "width" -> JsNumber(renderedWidget.width),
      "height" -> JsNumber(renderedWidget.height),
      "style" -> JsString(renderedWidget.style),
      "content" -> JsString(renderedWidget.content.toString)))
  }

}
