package models.widgetConfigs

import play.api.libs.json._
import play.api.libs.json.JsObject
import models.statusValues.MetricSeverityTypes

object MetricsItemTypes extends Enumeration {
  type Type = Value
  val Coverage, ViolationsCount, ViolationsDetail = Value
}

case class MetricsItem(itemType: MetricsItemTypes.Type,
                       labelSize: Option[Int] = None,
                       severities: Seq[MetricSeverityTypes.Type] = Seq.empty)

object MetricsItem {

  implicit object MetricsConfigFormat extends Format[MetricsItem] {
    override def reads(json: JsValue): MetricsItem =
      MetricsItem(
        MetricsItemTypes((json \ "itemType").as[Int]),
        (json \ "labelSize").asOpt[Int],
        (json \ "severities").as[Seq[Int]].map(MetricSeverityTypes(_)))

    override def writes(metricsItem: MetricsItem): JsValue = JsObject(
      Seq("itemType" -> JsNumber(metricsItem.itemType.id),
        "severities" -> Json.toJson(metricsItem.severities.map(_.id))) ++
        metricsItem.labelSize.map("labelSize" -> JsNumber(_)).toSeq)
  }

}

case class MetricsConfig(labelFont: Option[String] = None,
                         columns: Option[Int] = None,
                         items: Seq[MetricsItem] = Seq.empty)


object MetricsConfig {

  implicit object MetricsConfigFormat extends Format[MetricsConfig] {
    override def reads(json: JsValue): MetricsConfig =
      MetricsConfig(
        (json \ "labelFont").asOpt[String],
        (json \ "columns").asOpt[Int],
        (json \ "items").as[Seq[MetricsItem]])

    override def writes(metricsConfig: MetricsConfig): JsValue = JsObject(
      metricsConfig.labelFont.map("labelFont" -> JsString(_)).toSeq ++
        metricsConfig.columns.map("columns" -> JsNumber(_)).toSeq ++
        Seq("items" -> Json.toJson(metricsConfig.items))
    )
  }

}
