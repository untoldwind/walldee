package models.statusValues

import play.api.libs.json._
import play.api.libs.json.JsObject

case class BuildStatus(number: Int, running: Boolean, name: String)

object BuildStatus {

  implicit object BuildStatusFormat extends Format[BuildStatus] {
    override def reads(json: JsValue): JsResult[BuildStatus] =
      JsSuccess(BuildStatus(
        (json \ "number").as[Int],
        (json \ "running").asOpt[Boolean].getOrElse(false),
        (json \ "name").asOpt[String].getOrElse("")))

    override def writes(buildStatus: BuildStatus): JsValue = JsObject(
      Seq("number" -> JsNumber(buildStatus.number),
        "running" -> JsBoolean(buildStatus.running),
        "name" -> JsString(buildStatus.name)))
  }

}
