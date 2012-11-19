package models.statusValues

import play.api.libs.json._
import play.api.libs.json.JsObject

object MetricSeverityTypes extends Enumeration {
  type Type = Value
  val Blocker, Critical, Major, Minor, Info = Value
}

case class MetricViolation(severity: MetricSeverityTypes.Type, count: Int)

object MetricViolation {

  implicit object SonarViolationFormat extends Format[MetricViolation] {
    override def reads(json: JsValue): MetricViolation =
      MetricViolation(
        MetricSeverityTypes((json \ "severity").as[Int]),
        (json \ "count").as[Int])

    override def writes(sonarViolation: MetricViolation): JsValue = JsObject(
      Seq("severity" -> JsNumber(sonarViolation.severity.id),
        "count" -> JsNumber(sonarViolation.count)))
  }

}

case class MetricStatus(name: String,
                        coverage: Double,
                        violationsCount: Int,
                        violations: Seq[MetricViolation])


object MetricStatus {

  implicit object SonarStatusFormat extends Format[MetricStatus] {
    override def reads(json: JsValue): MetricStatus =
      MetricStatus(
        (json \ "name").as[String],
        (json \ "coverage").as[Double],
        (json \ "violationsCount").as[Int],
        (json \ "violations").as[Seq[MetricViolation]])

    override def writes(sonarStatus: MetricStatus): JsValue = JsObject(
      Seq("name" -> JsString(sonarStatus.name),
        "coverage" -> JsNumber(sonarStatus.coverage),
        "violationsCount" -> JsNumber(sonarStatus.violationsCount),
        "violations" -> Json.toJson(sonarStatus.violations)))
  }

}
