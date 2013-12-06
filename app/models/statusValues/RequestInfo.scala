package models.statusValues

import play.api.libs.json._
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import play.api.libs.json.JsSuccess

case class RequestInfo(url: String,
                       method: String,
                       username: Option[String],
                       password: Option[String],
                       headers: Seq[(String, String)],
                       body: Option[String])

object RequestInfo {

  implicit object RequestInfoFormat extends Format[RequestInfo] {
    override def reads(json: JsValue): JsResult[RequestInfo] =
      JsSuccess(RequestInfo(
        (json \ "url").as[String],
        (json \ "method").as[String],
        (json \ "username").asOpt[String],
        (json \ "password").asOpt[String],
        (json \ "headers").as[JsArray].value.map {
          json =>
            (json \ "name").as[String] -> (json \ "value").as[String]
        },
        (json \ "body").asOpt[String]))

    override def writes(requestInfo: RequestInfo): JsValue = JsObject(
      Seq("url" -> JsString(requestInfo.url),
        "method" -> JsString(requestInfo.method),
        "headers" -> JsArray(requestInfo.headers.map {
          case (name, value) =>
            JsObject(Seq(
              "name" -> JsString(name),
              "value" -> JsString(value)
            ))
        })) ++
        requestInfo.username.map("username" -> JsString(_)).toSeq ++
        requestInfo.password.map("password" -> JsString(_)).toSeq ++
        requestInfo.body.map("body" -> JsString(_)).toSeq
    )
  }

}