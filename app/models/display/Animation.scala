package models.display

import play.api.libs.json.{JsNumber, JsObject, JsValue, Format}

case class Animation(delay: Int) {

}

object Animation {

  implicit object AnimationFormat extends Format[Animation] {
    override def reads(json: JsValue): Animation =
      Animation(
        (json \ "delay").as[Int]
      )

    override def writes(animation: Animation): JsValue = JsObject(Seq(
      "delay" -> JsNumber(animation.delay))
    )
  }

}