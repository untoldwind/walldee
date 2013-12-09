package actors.monitorProcessors

import models.{StatusTypes, StatusMonitor}
import play.api.libs.json._
import models.statusValues.{ResponseInfo, BuildStatus}
import play.api.libs.ws.Response
import scala.util.{Success, Failure, Try}
import play.api.Logger

case class TeamcityBuildType(id: String, name: String, projectName: String)

object TeamcityBuildType {

  implicit object TeamcityBuildTypeReads extends Reads[TeamcityBuildType] {
    override def reads(json: JsValue): JsResult[TeamcityBuildType] =
      JsSuccess(TeamcityBuildType(
        (json \ "id").as[String],
        (json \ "name").as[String],
        (json \ "projectName").as[String]
      ))
  }

}

case class TeamcityBuild(id: Long, number: String, status: String, running: Boolean, buildType: TeamcityBuildType)

object TeamcityBuild {

  implicit object TeamcityBuildReads extends Reads[TeamcityBuild] {
    override def reads(json: JsValue): JsResult[TeamcityBuild] =
      JsSuccess(TeamcityBuild(
        (json \ "id").as[Long],
        (json \ "number").as[String],
        (json \ "status").as[String],
        (json \ "running").asOpt[Boolean].getOrElse(false),
        (json \ "buildType").as[TeamcityBuildType]
      ))
  }

}

class TeamcityProcessor(var statusMonitor: StatusMonitor) extends MonitorProcessor {
  val UrlPattern = """(http|https)://([a-zA-Z0-9\.:/]+)/viewType\.html\?buildTypeId=(bt[0-9]+).*""".r

  override def apiUrl = statusMonitor.url match {
    case UrlPattern(proto, base, id) =>
      proto + "://" + base + "/httpAuth/app/rest/builds/buildType:" + id + ",running:any"
    case url => url
  }

  def process(response: ResponseInfo) = {
    Try(response.bodyAsJson.as[TeamcityBuild]) match {
      case Failure(e) =>
        val body = response.body
        Logger.error(s"cannot parse json from response. Status=${response.statusCode}. Body: " + body.substring(0, Math.min(body.length, 400)), e)
        (StatusTypes.Unknown, JsObject(Seq.empty))
      case Success(teamcityBuild) =>
        val json = Json.toJson(BuildStatus(teamcityBuild.number.toInt, teamcityBuild.running, teamcityBuild.buildType.name))
        teamcityBuild.status match {
          case "SUCCESS" =>
            (StatusTypes.Ok, json)
          case _ =>
            (StatusTypes.Failure, json)
        }
    }

  }

}
