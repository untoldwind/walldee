package models.statusValues

import models.StatusTypes
import play.api.libs.json._
import play.api.libs.json.JsObject

case class TestResult(response: ResponseInfo, status: StatusTypes.Type, values: JsValue) {

}

object TestResult {

  implicit object TestResultWrites extends Writes[TestResult] {
    override def writes(testResult: TestResult) = JsObject(Seq(
      "response" -> Json.toJson(testResult.response),
      "status" -> JsString(testResult.status.toString),
      "values" -> testResult.values
    ))
  }

}

case class TestFailure(message: String)

object TestFailure {

  implicit object TestFailureWrites extends Writes[TestFailure] {
    override def writes(testFailure: TestFailure) = JsObject(Seq(
      "message" -> JsString(testFailure.message)
    ))
  }

}

case class StatusMonitorTestInfo(request: RequestInfo, result: Either[TestResult, TestFailure])

object StatusMonitorTestInfo {

  implicit object StatusMonitorTestInfoWrites extends Writes[StatusMonitorTestInfo] {
    override def writes(testInfo: StatusMonitorTestInfo) = JsObject(Seq(
      "request" -> Json.toJson(testInfo.request)
    ) ++
      testInfo.result.fold(
        result => Seq("result" -> Json.toJson(result)),
        failure => Seq("failure" -> Json.toJson(failure)))
    )
  }
}