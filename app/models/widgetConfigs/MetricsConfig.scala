package models.widgetConfigs

import play.api.libs.json._
import models.statusValues.MetricSeverityTypes
import play.api.data.Forms._
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsObject
import play.api.libs.json.JsBoolean
import play.api.libs.json.JsString
import scala.Some
import play.api.libs.json.JsNumber

object MetricsItemTypes extends Enumeration {
  type Type = Value
  val Coverage, ViolationsCount, ViolationsDetail = Value
}

case class MetricsItem(itemType: MetricsItemTypes.Type,
                       asGauge: Option[Boolean] = None,
                       valueFont: Option[String] = None,
                       valueSize: Option[Int] = None,
                       warnAt: Option[Int] = None,
                       severities: Seq[MetricSeverityTypes.Type] = Seq.empty,
                       showTrend: Option[Boolean] = None)

object MetricsItem {

  implicit object MetricsConfigFormat extends Format[MetricsItem] {
    override def reads(json: JsValue): JsResult[MetricsItem] =
      JsSuccess(MetricsItem(
        MetricsItemTypes((json \ "itemType").as[Int]),
        (json \ "asGauge").asOpt[Boolean],
        (json \ "valueFont").asOpt[String],
        (json \ "valueSize").asOpt[Int],
        (json \ "warnAt").asOpt[Int],
        (json \ "severities").as[Seq[Int]].map(MetricSeverityTypes(_)),
        (json \ "showTrend").asOpt[Boolean]))

    override def writes(metricsItem: MetricsItem): JsValue = JsObject(
      Seq("itemType" -> JsNumber(metricsItem.itemType.id),
        "severities" -> Json.toJson(metricsItem.severities.map(_.id))) ++
        metricsItem.asGauge.map("asGauge" -> JsBoolean(_)).toSeq ++
        metricsItem.valueFont.map("valueFont" -> JsString(_)).toSeq ++
        metricsItem.valueSize.map("valueSize" -> JsNumber(_)).toSeq ++
        metricsItem.warnAt.map("warnAt" -> JsNumber(_)).toSeq ++
        metricsItem.showTrend.map("showTrend" -> JsBoolean(_)).toSeq
    )
  }

}

case class MetricsConfig(labelFont: Option[String] = None,
                         labelSize: Option[Int] = None,
                         columns: Option[Int] = None,
                         items: Seq[MetricsItem] = Seq.empty) extends WidgetConfig


object MetricsConfig extends WidgetConfigMapper[MetricsConfig] {
  val default = apply()

  implicit val jsonFormat = new Format[MetricsConfig] {
    override def reads(json: JsValue): JsResult[MetricsConfig] =
      JsSuccess(MetricsConfig(
        (json \ "labelFont").asOpt[String],
        (json \ "labelSize").asOpt[Int],
        (json \ "columns").asOpt[Int],
        (json \ "items").as[Seq[MetricsItem]]))

    override def writes(metricsConfig: MetricsConfig): JsValue = JsObject(
      metricsConfig.labelFont.map("labelFont" -> JsString(_)).toSeq ++
        metricsConfig.labelSize.map("labelSize" -> JsNumber(_)).toSeq ++
        metricsConfig.columns.map("columns" -> JsNumber(_)).toSeq ++
        Seq("items" -> Json.toJson(metricsConfig.items))
    )
  }

  def itemTypeMapping = number.transform[MetricsItemTypes.Type](
    id => MetricsItemTypes(id),
    itemType => itemType.id
  )

  def severityMapping = mapping(
    "Blocker" -> boolean,
    "Critical" -> boolean,
    "Major" -> boolean,
    "Minor" -> boolean,
    "Info" -> boolean
  ) {
    (blocker, critical, major, minor, info) =>
      MetricSeverityTypes.values
      val severities = Seq.newBuilder[MetricSeverityTypes.Type]
      if (blocker)
        severities += MetricSeverityTypes.Blocker
      if (critical)
        severities += MetricSeverityTypes.Critical
      if (major)
        severities += MetricSeverityTypes.Major
      if (minor)
        severities += MetricSeverityTypes.Minor
      if (info)
        severities += MetricSeverityTypes.Info
      severities.result()
  } {
    severities =>
      Some(
        severities.exists(_ == MetricSeverityTypes.Blocker),
        severities.exists(_ == MetricSeverityTypes.Critical),
        severities.exists(_ == MetricSeverityTypes.Major),
        severities.exists(_ == MetricSeverityTypes.Minor),
        severities.exists(_ == MetricSeverityTypes.Info)
      )
  }

  def metricsItemMapping = mapping(
    "itemType" -> itemTypeMapping,
    "asGauge" -> optional(boolean),
    "valueFont" -> optional(text),
    "valueSize" -> optional(number),
    "warnAt" -> optional(number),
    "severities" -> severityMapping,
    "showTrend" -> optional(boolean)
  )(MetricsItem.apply)(MetricsItem.unapply)

  implicit def formMapping = mapping(
    "labelFont" -> optional(text),
    "labelSize" -> optional(number),
    "columns" -> optional(number),
    "items" -> seq(metricsItemMapping)
  )(apply)(unapply)


}
