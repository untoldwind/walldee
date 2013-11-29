package models.statusValues

import play.api.libs.json._
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import play.api.libs.json.JsArray
import play.api.libs.json.JsSuccess

case class ResponseInfo(statusCode: Int, statusText: String, headers: Map[String, Seq[String]], body: Option[String])

object ResponseInfo {

  implicit object ResponseInfoFormat extends Format[ResponseInfo] {
    override def reads(json: JsValue): JsResult[ResponseInfo] =
      JsSuccess(ResponseInfo(
        (json \ "statusCode").as[Int],
        (json \ "statusText").as[String],
        (json \ "headers").as[JsObject].value.map {
          case (key: String, values: JsArray) =>
            key -> values.value.map(_.as[String])
        }.toMap,
        (json \ "body").asOpt[String]))

    override def writes(responseInfo: ResponseInfo): JsValue = JsObject(
      Seq("statusCode" -> JsNumber(responseInfo.statusCode),
        "statusText" -> JsString(responseInfo.statusText),
        "headers" -> JsObject(responseInfo.headers.toSeq.map {
          case (key, values) =>
            key -> JsArray(values.map(JsString.apply))
        })) ++
        responseInfo.body.map("body" -> JsString(_)).toSeq
    )
  }
}
