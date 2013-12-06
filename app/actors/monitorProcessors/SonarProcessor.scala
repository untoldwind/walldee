package actors.monitorProcessors

import models.{StatusTypes, StatusValue, StatusMonitor}
import play.api.libs.ws.Response
import play.api.libs.json._
import models.statusValues._
import play.api.libs.ws.Response
import play.api.libs.json.JsObject
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsObject

object SonarMetricTypes extends Enumeration {
  type Type = Value
  val coverage, violations, blocker_violations, critical_violations, major_violations, minor_violations, info_violations = Value
}

case class SonarMetrics(key: SonarMetricTypes.Type, value: Double)

object SonarMetrics {

  implicit object SonarMetricsReads extends Reads[SonarMetrics] {
    override def reads(json: JsValue): JsResult[SonarMetrics] =
      JsSuccess(SonarMetrics(
        SonarMetricTypes.withName((json \ "key").as[String]),
        (json \ "val").as[Double]
      ))
  }

}

case class SonarResource(id: Long, key: String, name: String, metrics: Seq[SonarMetrics])

object SonarResource {

  implicit object SonarResourceReads extends Reads[SonarResource] {
    override def reads(json: JsValue): JsResult[SonarResource] =
      JsSuccess(SonarResource(
        (json \ "id").as[Long],
        (json \ "key").as[String],
        (json \ "name").as[String],
        (json \ "msr").as[Seq[SonarMetrics]]
      ))
  }

}

object SonarProcessor extends MonitorProcessor {

  val UrlPattern = """(http|https)://([a-zA-Z0-9\.:/]+)/dashboard/index/([0-9]+).*""".r

  override def apiUrl(url: String) = url match {
    case UrlPattern(proto, base, id) =>
      proto + "://" + base + "/api/resources?resource=" + id + "&metrics=" + SonarMetricTypes.values.mkString(",") + "&format=json"
    case url => url
  }

  def process(statusMonitor: StatusMonitor, response: ResponseInfo) {
    val sonarResources = response.bodyAsJson.as[Seq[SonarResource]]

    if (sonarResources.length == 1) {
      var coverage = 0.0
      var violationsCount = 0
      val violations = Seq.newBuilder[MetricViolation]
      sonarResources(0).metrics.foreach {
        metric =>
          metric.key match {
            case SonarMetricTypes.coverage =>
              coverage = metric.value
            case SonarMetricTypes.violations =>
              violationsCount = metric.value.toInt
            case SonarMetricTypes.blocker_violations =>
              violations += MetricViolation(MetricSeverityTypes.Blocker, metric.value.toInt)
            case SonarMetricTypes.critical_violations =>
              violations += MetricViolation(MetricSeverityTypes.Critical, metric.value.toInt)
            case SonarMetricTypes.major_violations =>
              violations += MetricViolation(MetricSeverityTypes.Major, metric.value.toInt)
            case SonarMetricTypes.minor_violations =>
              violations += MetricViolation(MetricSeverityTypes.Minor, metric.value.toInt)
            case SonarMetricTypes.info_violations =>
              violations += MetricViolation(MetricSeverityTypes.Info, metric.value.toInt)
          }
      }

      val sonarStatus = MetricStatus(sonarResources(0).name, coverage, violationsCount, violations.result())
      updateStatus(statusMonitor, StatusTypes.Ok, Json.toJson(sonarStatus))
    } else {
      updateStatus(statusMonitor, StatusTypes.Failure, JsObject(Seq.empty))
    }
  }
}
