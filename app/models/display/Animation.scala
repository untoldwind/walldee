package models.display

import play.api.libs.json._
import play.api.libs.json.JsObject
import play.api.libs.json.JsNumber

case class Animation(widgetIds: Seq[Long], delay: Int) {

}

object Animation {

  implicit object AnimationFormat extends Format[Animation] {
    override def reads(json: JsValue): JsResult[Animation] =
      JsSuccess(Animation(
        (json \ "widgetIds").as[Seq[Long]],
        (json \ "delay").as[Int]
      ))

    override def writes(animation: Animation): JsValue = JsObject(Seq(
      "widgetIds" -> Json.toJson(animation.widgetIds),
      "delay" -> JsNumber(animation.delay))
    )
  }

}