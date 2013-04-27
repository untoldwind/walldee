package models.widgetConfigs

import play.api.libs.json._
import play.api.data.Forms._
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import play.api.libs.json.JsSuccess

case class IFrameConfig(url: Option[String] = None) extends WidgetConfig

object IFrameConfig extends WidgetConfigMapper[IFrameConfig] {
  val default = apply()

  implicit val jsonFormat = new Format[IFrameConfig] {
    override def reads(json: JsValue): JsResult[IFrameConfig] =
      JsSuccess(IFrameConfig(
        (json \ "url").asOpt[String]))

    override def writes(iframeConfig: IFrameConfig): JsValue = JsObject(
      iframeConfig.url.map("url" -> JsString(_)).toSeq)
  }

  implicit val formMapping = mapping(
    "url" -> optional(text)
  )(apply)(unapply)
}
