package models.statusValues

import play.api.libs.json._
import play.api.libs.json.JsObject

case class BuildStatus(number: Int)

object BuildStatus {

  implicit object BuildStatusFormat extends Format[BuildStatus] {
    override def reads(json: JsValue): BuildStatus =
      BuildStatus(
        (json \ "number").as[Int])

    override def writes(jenkinsStatus: BuildStatus): JsValue = JsObject(
      Seq("number" -> JsNumber(jenkinsStatus.number)))
  }

}
