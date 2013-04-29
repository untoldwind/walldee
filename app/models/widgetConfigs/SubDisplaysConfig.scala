package models.widgetConfigs

import play.api.libs.json._
import play.api.libs.json.JsObject
import play.api.libs.json.JsSuccess
import play.api.data.Forms._

case class SubDisplay(displayId: Long, duration: Long = -1)

object SubDisplay {

  implicit object SubDisplayFormat extends Format[SubDisplay] {
    override def reads(json: JsValue): JsResult[SubDisplay] =
      JsSuccess(SubDisplay(
        (json \ "displayId").as[Long],
        (json \ "duration").as[Long]))

    override def writes(subDisplay: SubDisplay): JsValue = JsObject(
      Seq(
        "displayId" -> JsNumber(subDisplay.displayId),
        "duration" -> JsNumber(subDisplay.duration)
      ))
  }

  val formMapping = mapping(
    "displayId" -> longNumber,
    "duration" -> longNumber
  )(apply)(unapply)
}

case class SubDisplaysConfig(displays: Seq[SubDisplay] = Seq.empty) extends WidgetConfig

object SubDisplaysConfig extends WidgetConfigMapper[SubDisplaysConfig] {
  val default = apply()

  implicit val jsonFormat = new Format[SubDisplaysConfig] {
    override def reads(json: JsValue): JsResult[SubDisplaysConfig] =
      JsSuccess(SubDisplaysConfig(
        (json \ "displays").as[Seq[SubDisplay]]))

    override def writes(subDisplaysConfig: SubDisplaysConfig): JsValue = JsObject(
      Seq("displays" -> Json.toJson(subDisplaysConfig.displays)))
  }

  implicit val formMapping = mapping(
    "displays" -> seq(SubDisplay.formMapping)
  )(apply)(unapply)
}
