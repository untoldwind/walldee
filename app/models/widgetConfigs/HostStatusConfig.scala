package models.widgetConfigs

import play.api.libs.json._
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import util.matching.Regex

case class HostStatusConfig(titleFont: Option[String] = None,
                            titleSize: Option[Int] = None,
                            labelFont: Option[String] = None,
                            labelSize: Option[Int] = None,
                            columns: Option[Int] = None,
                            hostNamePattern: Option[Regex] = None)

object HostStatusConfig {

  implicit object HostStatusConfigFormat extends Format[HostStatusConfig] {
    override def reads(json: JsValue): HostStatusConfig =
      HostStatusConfig(
        (json \ "titleFont").asOpt[String],
        (json \ "titleSize").asOpt[Int],
        (json \ "labelFont").asOpt[String],
        (json \ "labelSize").asOpt[Int],
        (json \ "columns").asOpt[Int],
        (json \ "hostNamePattern").asOpt[String].map(_.r))

    override def writes(hostStatusConfig: HostStatusConfig): JsValue = JsObject(
      hostStatusConfig.titleFont.map("titleFont" -> JsString(_)).toSeq ++
        hostStatusConfig.titleSize.map("titleSize" -> JsNumber(_)).toSeq ++
        hostStatusConfig.labelFont.map("labelFont" -> JsString(_)).toSeq ++
        hostStatusConfig.labelSize.map("labelSize" -> JsNumber(_)).toSeq ++
        hostStatusConfig.columns.map("columns" -> JsNumber(_)).toSeq ++
        hostStatusConfig.hostNamePattern.map(_.toString).map("hostNamePattern" -> JsString(_)).toSeq)
  }

}
