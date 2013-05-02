package models.statusMonitors

import play.api.libs.json._
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsNumber

object FreestyleTypes extends Enumeration {
  type Type = Value
  val Regex, Json, Xml, Html = Value
}

case class FreestyleConfig(freestyleType: FreestyleTypes.Type = FreestyleTypes.Regex,
                           selector: Option[String] = None)

object FreestyleConfig {

  implicit object FreestyleConfigdFormat extends Format[FreestyleConfig] {
    override def reads(json: JsValue): JsResult[FreestyleConfig] =
      JsSuccess(FreestyleConfig(
        FreestyleTypes((json \ "freestyleType").as[Int]),
        (json \ "selector").asOpt[String])

    )

    override def writes(freestyleConfig: FreestyleConfig): JsValue = JsObject(
      Seq("freestyleType" -> JsNumber(freestyleConfig.freestyleType.id)) ++
        freestyleConfig.selector.map("selector" -> JsString(_)).toSeq )
  }

}