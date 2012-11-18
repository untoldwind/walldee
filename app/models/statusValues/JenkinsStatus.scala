package models.statusValues

import play.api.libs.json._
import play.api.libs.json.JsObject

case class JenkinsStatus(number: Int)

object JenkinsStatus {

  implicit object JenkinsStatusFormat extends Format[JenkinsStatus] {
    override def reads(json: JsValue): JenkinsStatus =
      JenkinsStatus(
        (json \ "number").as[Int])

    override def writes(jenkinsStatus: JenkinsStatus): JsValue = JsObject(
      Seq("number" -> JsNumber(jenkinsStatus.number)))
  }

}
