package models.statusValues

import play.api.libs.json._
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import play.api.libs.json.JsArray
import play.api.libs.json.JsSuccess

sealed trait RequestResult {
}

case class RequestSuccess(statusCode: Int,
                          statusText: String,
                          headers: Seq[(String, String)],
                          body: String) extends RequestResult {
  def bodyAsJson = Json.parse(body)
}

object RequestSuccess {

  implicit object ResponseInfoFormat extends Format[RequestSuccess] {
    override def reads(json: JsValue): JsResult[RequestSuccess] =
      JsSuccess(RequestSuccess(
        (json \ "statusCode").as[Int],
        (json \ "statusText").as[String],
        (json \ "headers").as[JsArray].value.map {
          json =>
            (json \ "name").as[String] -> (json \ "value").as[String]
        },
        (json \ "body").as[String]))

    override def writes(responseInfo: RequestSuccess): JsValue = JsObject(
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

case class RequestFailure(exception: Throwable) extends RequestResult {

}

object RequestFailure {

  implicit object RequestFailureWrites extends Writes[RequestFailure] {
    override def writes(requestFailure: RequestFailure) = JsObject(Seq(
      "errorClass" -> JsString(requestFailure.exception.getClass.toString),
      "message" -> JsString(requestFailure.exception.getMessage)
    ))
  }

}