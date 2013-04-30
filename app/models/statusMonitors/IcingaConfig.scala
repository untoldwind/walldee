package models.statusMonitors

import play.api.libs.json._
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import scala.util.matching.Regex

case class IcingaExpected(host: String, criticals: Int, warnings: Int)

object IcingaExpected {

  implicit object IcingaExpectedFormat extends Format[IcingaExpected] {
    override def reads(json: JsValue): JsResult[IcingaExpected] =
      JsSuccess(IcingaExpected(
        (json \ "host").as[String],
        (json \ "criticals").as[Int],
        (json \ "warnings").as[Int]))

    override def writes(icingaExpected: IcingaExpected): JsValue = JsObject(
      Seq("host" -> JsString(icingaExpected.host),
        "criticals" -> JsNumber(icingaExpected.criticals),
        "warnings" -> JsNumber(icingaExpected.warnings)))
  }

}

case class IcingaConfig(hostNameFilter: Option[Regex] = None, expected: Seq[IcingaExpected] = Seq.empty)

object IcingaConfig {

  implicit object IcingaConfigFormat extends Format[IcingaConfig] {
    override def reads(json: JsValue): JsResult[IcingaConfig] =
      JsSuccess(IcingaConfig(
        (json \ "hostNameFilter").asOpt[String].map(_.r),
        (json \ "expected").as[Seq[IcingaExpected]]))

    override def writes(icingaConfig: IcingaConfig): JsValue = JsObject(
      icingaConfig.hostNameFilter.map(_.toString()).map("hostNameFilter" -> JsString(_)).toSeq ++
        Seq("expected" -> Json.toJson(icingaConfig.expected)))
  }

}
