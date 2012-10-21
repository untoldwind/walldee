package models.json

import play.api.libs.json.{JsString, JsObject, JsValue, Format}

case class SprintCounter(name: String, color: String)

object SprintCounter {

  implicit object CallRecipeCommandFormat extends Format[SprintCounter] {
    override def reads(json: JsValue): SprintCounter =
      SprintCounter(
        (json \ "name").as[String],
        (json \ "color").as[String])

    override def writes(sprintCounter: SprintCounter): JsValue = JsObject(Seq(
      "name" -> JsString(sprintCounter.name),
      "color" -> JsString(sprintCounter.color)))
  }

}