package models.statusValues

import play.api.libs.json._
import play.api.libs.json.JsObject

object SonarSeverityTypes extends Enumeration {
  type Type = Value
  val Blocker, Critical, Major, Minor, Info = Value
}

case class SonarViolation(severity: SonarSeverityTypes.Type, count: Int)

object SonarViolation {

  implicit object SonarViolationFormat extends Format[SonarViolation] {
    override def reads(json: JsValue): SonarViolation =
      SonarViolation(
        SonarSeverityTypes((json \ "severity").as[Int]),
        (json \ "count").as[Int])

    override def writes(sonarViolation: SonarViolation): JsValue = JsObject(
      Seq("severity" -> JsNumber(sonarViolation.severity.id),
        "count" -> JsNumber(sonarViolation.count)))
  }

}

case class SonarStatus(name: String,
                       coverage: Double,
                       violationsCount: Int,
                       violations: Seq[SonarViolation])


object SonarStatus {

  implicit object SonarStatusFormat extends Format[SonarStatus] {
    override def reads(json: JsValue): SonarStatus =
      SonarStatus(
        (json \ "name").as[String],
        (json \ "coverage").as[Double],
        (json \ "violationsCount").as[Int],
        (json \ "violations").as[Seq[SonarViolation]])

    override def writes(sonarStatus: SonarStatus): JsValue = JsObject(
      Seq("name" -> JsString(sonarStatus.name),
        "coverage" -> JsNumber(sonarStatus.coverage),
        "violationsCount" -> JsNumber(sonarStatus.violationsCount),
        "violations" -> Json.toJson(sonarStatus.violations)))
  }

}
