package models.json

import play.api.libs.json._
import play.api.libs.json.JsObject
import play.api.libs.json.JsString

case class SprintCounterValue(name: String, value: Int)

object SprintCounterValue {

  implicit object SprintCounterValueFormat extends Format[SprintCounterValue] {
    override def reads(json: JsValue): SprintCounterValue =
      SprintCounterValue(
        (json \ "name").as[String],
        (json \ "value").as[Int])

    override def writes(sprintCounter: SprintCounterValue): JsValue = JsObject(Seq(
      "name" -> JsString(sprintCounter.name),
      "value" -> JsNumber(sprintCounter.value)))
  }

}
