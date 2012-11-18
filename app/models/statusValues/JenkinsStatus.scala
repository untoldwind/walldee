package models.statusValues

import play.api.libs.json._
import play.api.libs.json.JsObject

case class JenkinsStatus(number: Option[Int])

object JenkinsStatus {

  implicit object JenkinsStatusFormat extends Format[JenkinsStatus] {
    override def reads(json: JsValue): JenkinsStatus =
      JenkinsStatus(
        (json \ "number").asOpt[Int])

    override def writes(jenkinsStatus: JenkinsStatus): JsValue = JsObject(
      jenkinsStatus.number.map("number" -> JsNumber(_)).toSeq)
  }

}
