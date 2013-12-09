package models.statusValues

import models.StatusTypes
import play.api.libs.json._
import play.api.libs.json.JsObject

case class StatusMonitorTestInfo(request: RequestInfo, result: RequestResult, status: Option[StatusTypes.Type], values: Option[JsValue])

object StatusMonitorTestInfo {

  implicit object StatusMonitorTestInfoWrites extends Writes[StatusMonitorTestInfo] {
    override def writes(testInfo: StatusMonitorTestInfo) = JsObject(Seq(
      "request" -> Json.toJson(testInfo.request)
    ) ++
      (testInfo.result match {
        case success: RequestSuccess =>
          Seq("result" -> Json.toJson(success))
        case failure: RequestFailure =>
          Seq("failure" -> Json.toJson(failure))
      }) ++
      testInfo.status.map(status => "status" -> JsString(status.toString)).toSeq ++
      testInfo.values.map("values" -> _).toSeq
    )
  }

}