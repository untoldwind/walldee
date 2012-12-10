package actors.monitorProcessors

import models.{StatusTypes, StatusMonitor}
import play.api.libs.ws.Response
import play.api.libs.json._
import models.statusValues.BuildStatus
import play.api.libs.ws.Response

case class TeamcityBuild(id: Long, number: String, status: String)

object TeamcityBuild {

  implicit object TeamcityBuildReads extends Reads[TeamcityBuild] {
    override def reads(json: JsValue): JsResult[TeamcityBuild] =
      JsSuccess(TeamcityBuild(
        (json \ "id").as[Long],
        (json \ "number").as[String],
        (json \ "status").as[String]
      ))
  }

}

object TeamcityProcessor extends MonitorProcessor {
  val UrlPattern = """(http|https)://([a-zA-Z0-9\.:/]+)/viewType\.html\?buildTypeId=(bt[0-9]+).*""".r

  override def apiUrl(url: String) = url match {
    case UrlPattern(proto, base, id) =>
      proto + "://" + base + "/httpAuth/app/rest/builds/buildType:" + id
    case url => url
  }

  def process(statusMonitor: StatusMonitor, response: Response) {
    val teamcityBuild = response.json.as[TeamcityBuild]

    val json = Json.toJson(BuildStatus(teamcityBuild.number.toInt))
    teamcityBuild.status match {
      case "SUCCESS" =>
        updateStatus(statusMonitor, StatusTypes.Ok, json)
      case _ =>
        updateStatus(statusMonitor, StatusTypes.Failure, json)
    }
  }

}
