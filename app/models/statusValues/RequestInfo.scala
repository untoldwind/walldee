package models.statusValues

import play.api.libs.json._
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import play.api.libs.json.JsSuccess

case class RequestInfo(url: String, method: String, headers: Map[String, Seq[String]], body: Option[String])

object RequestInfo {

  implicit object RequestInfoFormat extends Format[RequestInfo] {
    override def reads(json: JsValue): JsResult[RequestInfo] =
      JsSuccess(RequestInfo(
        (json \ "url").as[String],
        (json \ "method").as[String],
        (json \ "headers").as[JsObject].value.map {
          case (key: String, values: JsArray) =>
            key -> values.value.map(_.as[String])
        }.toMap,
        (json \ "body").asOpt[String]))

    override def writes(requestInfo: RequestInfo): JsValue = JsObject(
      Seq("url" -> JsString(requestInfo.url),
        "method" -> JsString(requestInfo.method),
        "headers" -> JsObject(requestInfo.headers.toSeq.map {
          case (key, values) =>
            key -> JsArray(values.map(JsString.apply))
        })) ++
        requestInfo.body.map("body" -> JsString(_)).toSeq
    )
  }

}