package actors.monitorProcessors

import models.{StatusTypes, StatusValue, StatusMonitor}
import play.api.libs.json._
import play.api.libs.ws.Response
import models.statusValues.BuildStatus

case class JenkinsJobBuild(number: Int, url: String)

object JenkinsJobBuild {

  implicit object JenkinsJobBuildReads extends Reads[JenkinsJobBuild] {
    override def reads(json: JsValue): JenkinsJobBuild =
      JenkinsJobBuild(
        (json \ "number").as[Int],
        (json \ "url").as[String]
      )
  }

}

case class JenkinsJob(name: String,
                      lastBuild: Option[JenkinsJobBuild],
                      lastCompletedBuild: Option[JenkinsJobBuild],
                      lastStableBuild: Option[JenkinsJobBuild],
                      lastSuccessfulBuild: Option[JenkinsJobBuild])

object JenkinsJob {

  implicit object JenkinsJobReads extends Reads[JenkinsJob] {
    override def reads(json: JsValue): JenkinsJob =
      JenkinsJob(
        (json \ "name").as[String],
        (json \ "lastBuild").asOpt[JenkinsJobBuild],
        (json \ "lastCompletedBuild").asOpt[JenkinsJobBuild],
        (json \ "lastStableBuild").asOpt[JenkinsJobBuild],
        (json \ "lastSuccessfulBuild").asOpt[JenkinsJobBuild]
      )
  }

}

object JenkinsProcessor extends MonitorProcessor {
  override def apiUrl(url: String): String = url match {
    case url if url.endsWith("/api/json") => url
    case url if url.endsWith("/") => url + "api/json"
    case url => url + "/api/json"
  }

  def process(statusMonitor: StatusMonitor, response: Response) {
    val jenkinsJob = response.json.as[JenkinsJob]

    jenkinsJob.lastCompletedBuild.map {
      lastCompletedBuild =>
        val json = Json.toJson(BuildStatus(lastCompletedBuild.number))
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
