package actors.monitorProcessors

import models.{StatusTypes, StatusValue, StatusMonitor}
import play.api.libs.json._
import play.api.libs.ws.Response
import models.statusValues.JenkinsStatus

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
                      lastSuccessfulBuild: Option[JenkinsJobBuild])

object JenkinsJob {

  implicit object JenkinsJobReads extends Reads[JenkinsJob] {
    override def reads(json: JsValue): JenkinsJob =
      JenkinsJob(
        (json \ "name").as[String],
        (json \ "lastBuild").asOpt[JenkinsJobBuild],
        (json \ "lastCompletedBuild").asOpt[JenkinsJobBuild],
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

    val statusValue = jenkinsJob.lastCompletedBuild.map {
      lastCompletedBuild =>
        val json = Json.toJson(JenkinsStatus(lastCompletedBuild.number))
        jenkinsJob.lastSuccessfulBuild.map {
          case lastSuccessfulBuild if lastCompletedBuild.number == lastSuccessfulBuild.number =>
            new StatusValue(statusMonitor.id.get, StatusTypes.Ok, json)
          case _ =>
            new StatusValue(statusMonitor.id.get, StatusTypes.Failure, json)
        }.getOrElse {
          new StatusValue(statusMonitor.id.get, StatusTypes.Failure, json)
        }
    }.getOrElse {
      new StatusValue(statusMonitor.id.get, StatusTypes.Unknown, JsObject(Seq.empty))
    }
    statusValue.insert
  }

}
