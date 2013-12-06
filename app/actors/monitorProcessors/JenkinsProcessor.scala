package actors.monitorProcessors

import models.{StatusTypes, StatusMonitor}
import play.api.libs.json._
import play.api.libs.ws.Response
import models.statusValues.{ResponseInfo, BuildStatus}
import play.api.Logger
import scala.util.{Success, Failure, Try}

case class JenkinsJobBuild(number: Int, url: String)

object JenkinsJobBuild {

  implicit object JenkinsJobBuildReads extends Reads[JenkinsJobBuild] {
    override def reads(json: JsValue): JsResult[JenkinsJobBuild] =
      JsSuccess(JenkinsJobBuild(
        (json \ "number").as[Int],
        (json \ "url").as[String]
      ))
  }

}

case class JenkinsJob(name: String,
                      lastBuild: Option[JenkinsJobBuild],
                      lastCompletedBuild: Option[JenkinsJobBuild],
                      lastStableBuild: Option[JenkinsJobBuild],
                      lastSuccessfulBuild: Option[JenkinsJobBuild])

object JenkinsJob {

  implicit object JenkinsJobReads extends Reads[JenkinsJob] {
    override def reads(json: JsValue): JsResult[JenkinsJob] =
      JsSuccess(JenkinsJob(
        (json \ "name").as[String],
        (json \ "lastBuild").asOpt[JenkinsJobBuild],
        (json \ "lastCompletedBuild").asOpt[JenkinsJobBuild],
        (json \ "lastStableBuild").asOpt[JenkinsJobBuild],
        (json \ "lastSuccessfulBuild").asOpt[JenkinsJobBuild]
      ))
  }

}

object JenkinsProcessor extends MonitorProcessor {
  override def apiUrl(url: String): String = url match {
    case url if url.endsWith("/api/json") => url
    case url if url.endsWith("/") => url + "api/json"
    case url => url + "/api/json"
  }

  def process(statusMonitor: StatusMonitor, response: ResponseInfo) {

    Try(response.bodyAsJson.as[JenkinsJob]) match {
      case Failure(e) =>
        val body = response.body
        Logger.error(s"cannot parse json from response. Status=${response.statusCode}. Body: " + body.substring(0, Math.min(body.length, 400)), e)
        updateStatus(statusMonitor, StatusTypes.Unknown, JsObject(Seq.empty))
      case Success(jenkinsJob) =>
        jenkinsJob.lastCompletedBuild.map {
          lastCompletedBuild =>
            val running = jenkinsJob.lastBuild.exists(lastBuild => lastCompletedBuild.number != lastBuild.number)
            val json = Json.toJson(BuildStatus(lastCompletedBuild.number, running, jenkinsJob.name))
            jenkinsJob.lastStableBuild.map {
              case lastStableBuild if lastCompletedBuild.number == lastStableBuild.number =>
                updateStatus(statusMonitor, StatusTypes.Ok, json)
              case _ =>
                updateStatus(statusMonitor, StatusTypes.Failure, json)
            }.getOrElse {
              updateStatus(statusMonitor, StatusTypes.Failure, json)
            }
        }.getOrElse {
          updateStatus(statusMonitor, StatusTypes.Unknown, JsObject(Seq.empty))
        }
    }
  }

}
