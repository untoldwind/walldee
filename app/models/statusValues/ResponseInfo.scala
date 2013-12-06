package models.statusValues

import play.api.libs.json._
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import play.api.libs.json.JsArray
import play.api.libs.json.JsSuccess

case class ResponseInfo(statusCode: Int, statusText: String, headers: Seq[(String, String)], body: String) {
  def bodyAsJson = Json.parse(body)
}

object ResponseInfo {

  implicit object ResponseInfoFormat extends Format[ResponseInfo] {
    override def reads(json: JsValue): JsResult[ResponseInfo] =
      JsSuccess(ResponseInfo(
        (json \ "statusCode").as[Int],
        (json \ "statusText").as[String],
        (json \ "headers").as[JsArray].value.map {
          json =>
            (json \ "name").as[String] -> (json \ "value").as[String]
        },
        (json \ "body").as[String]))

    override def writes(responseInfo: ResponseInfo): JsValue = JsObject(
      Seq("statusCode" -> JsNumber(responseInfo.statusCode),
        "statusText" -> JsString(responseInfo.statusText),
        "headers" -> JsArray(responseInfo.headers.map {
          case (name, value) =>
            JsObject(Seq(
              "name" -> JsString(name),
              "value" -> JsString(value)
            ))
        }),
        "body" -> JsString(responseInfo.body)
      ))
  }
}
