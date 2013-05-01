package models.widgetConfigs

import play.api.libs.json._
import play.api.libs.json.JsSuccess
import play.api.data.Forms._

case class HeadingConfig(title: Option[String] = None, rotation: Option[Int] = None) extends WidgetConfig {

}

object HeadingConfig extends WidgetConfigMapper[HeadingConfig] {
  def default = apply()

  def jsonFormat = new Format[HeadingConfig] {
    override def reads(json: JsValue): JsResult[HeadingConfig] =
      JsSuccess(HeadingConfig((json \ "title").asOpt[String],
        (json \ "rotation").asOpt[Int]))

    override def writes(headingConfig: HeadingConfig): JsValue = JsObject(
      headingConfig.title.map("title" -> JsString(_)).toSeq ++
        headingConfig.rotation.map("rotation" -> JsNumber(_)).toSeq

    )
  }

  def formMapping = mapping(
    "title" -> optional(text),
    "rotation" -> optional(number)
  )(apply)(unapply)
}