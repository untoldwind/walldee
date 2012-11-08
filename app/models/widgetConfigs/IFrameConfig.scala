package models.widgetConfigs

import play.api.libs.json._
import play.api.libs.json.JsObject
import play.api.libs.json.JsString

case class IFrameConfig(url: Option[String] = None)

object IFrameConfig {

  implicit object IFrameConfigFormat extends Format[IFrameConfig] {
    override def reads(json: JsValue): JsResult[IFrameConfig] =
      JsSuccess(IFrameConfig(
        (json \ "url").asOpt[String]))

    override def writes(iframeConfig: IFrameConfig): JsValue = JsObject(
      iframeConfig.url.map("url" -> JsString(_)).toSeq)
  }

}
