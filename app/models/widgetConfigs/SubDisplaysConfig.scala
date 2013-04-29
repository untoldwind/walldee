package models.widgetConfigs

import play.api.libs.json._
import play.api.libs.json.JsObject
import play.api.libs.json.JsSuccess
import play.api.data.Forms._

case class SubDisplayRef(displayId: Long, duration: Long = -1)

object SubDisplayRef {

  implicit object SubDisplayFormat extends Format[SubDisplayRef] {
    override def reads(json: JsValue): JsResult[SubDisplayRef] =
      JsSuccess(SubDisplayRef(
        (json \ "displayId").as[Long],
        (json \ "duration").as[Long]))

    override def writes(subDisplay: SubDisplayRef): JsValue = JsObject(
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

case class SubDisplaysConfig(displays: Seq[SubDisplayRef] = Seq.empty) extends WidgetConfig

object SubDisplaysConfig extends WidgetConfigMapper[SubDisplaysConfig] {
  val default = apply()

  implicit val jsonFormat = new Format[SubDisplaysConfig] {
    override def reads(json: JsValue): JsResult[SubDisplaysConfig] =
      JsSuccess(SubDisplaysConfig(
        (json \ "displays").as[Seq[SubDisplayRef]]))

    override def writes(subDisplaysConfig: SubDisplaysConfig): JsValue = JsObject(
      Seq("displays" -> Json.toJson(subDisplaysConfig.displays)))
  }

  implicit val formMapping = mapping(
    "displays" -> seq(SubDisplayRef.formMapping)
  )(apply)(unapply)
}
