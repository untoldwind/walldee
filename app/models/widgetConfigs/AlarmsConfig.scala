package models.widgetConfigs

import play.api.libs.json._
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import play.api.data.OptionalMapping

case class AlarmsConfig(labelFont: Option[String] = None,
                        labelSize: Option[Int] = None,
                        descriptionFont: Option[String] = None,
                        descriptionSize: Option[Int] = None,
                        alertPeriod: Option[Int] = None)

object AlarmsConfig {

  implicit object AlarmConfigFormat extends Format[AlarmsConfig] {
    override def reads(json: JsValue): JsResult[AlarmsConfig] =
      JsSuccess(AlarmsConfig(
        (json \ "labelFont").asOpt[String],
        (json \ "labelSize").asOpt[Int],
        (json \ "descriptionFont").asOpt[String],
        (json \ "descriptionSize").asOpt[Int],
        (json \ "alertPeriod").asOpt[Int]))

    override def writes(alarmsConfig: AlarmsConfig): JsValue = JsObject(
      alarmsConfig.labelFont.map("labelFont" -> JsString(_)).toSeq ++
        alarmsConfig.labelSize.map("labelSize" -> JsNumber(_)).toSeq ++
        alarmsConfig.descriptionFont.map("descriptionFont" -> JsString(_)).toSeq ++
        alarmsConfig.descriptionSize.map("descriptionSize" -> JsNumber(_)).toSeq ++
        alarmsConfig.alertPeriod.map("alertPeriod" -> JsNumber(_)).toSeq)
  }

}
