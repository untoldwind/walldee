package models.widgetConfigs

import play.api.libs.json._
import scala.util.matching.Regex
import play.api.data.Forms._
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsNumber

case class HostStatusConfig(titleFont: Option[String] = None,
                            titleSize: Option[Int] = None,
                            labelFont: Option[String] = None,
                            labelSize: Option[Int] = None,
                            columns: Option[Int] = None,
                            hostNamePattern: Option[Regex] = None) extends WidgetConfig

object HostStatusConfig extends WidgetConfigMapper[HostStatusConfig] {
  val default = apply()

  implicit val jsonFormat = new Format[HostStatusConfig] {
    override def reads(json: JsValue): JsResult[HostStatusConfig] =
      JsSuccess(HostStatusConfig(
        (json \ "titleFont").asOpt[String],
        (json \ "titleSize").asOpt[Int],
        (json \ "labelFont").asOpt[String],
        (json \ "labelSize").asOpt[Int],
        (json \ "columns").asOpt[Int],
        (json \ "hostNamePattern").asOpt[String].map(_.r)))

    override def writes(hostStatusConfig: HostStatusConfig): JsValue = JsObject(
      hostStatusConfig.titleFont.map("titleFont" -> JsString(_)).toSeq ++
        hostStatusConfig.titleSize.map("titleSize" -> JsNumber(_)).toSeq ++
        hostStatusConfig.labelFont.map("labelFont" -> JsString(_)).toSeq ++
        hostStatusConfig.labelSize.map("labelSize" -> JsNumber(_)).toSeq ++
        hostStatusConfig.columns.map("columns" -> JsNumber(_)).toSeq ++
        hostStatusConfig.hostNamePattern.map(_.toString()).map("hostNamePattern" -> JsString(_)).toSeq)
  }

  val regexMapping = text.transform[Regex](
    str => str.r,
    regex => regex.toString()
  )

  implicit val formMapping = mapping(
    "titleFont" -> optional(text),
    "titleSize" -> optional(number),
    "labelFont" -> optional(text),
    "labelSize" -> optional(number),
    "columns" -> optional(number),
    "hostNamePattern" -> optional(regexMapping)
  )(apply)(unapply)
}
