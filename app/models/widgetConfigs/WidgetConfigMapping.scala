package models.widgetConfigs

import models.DisplayWidgets.Type
import play.api.data.{FormError, Mapping}
import models.DisplayWidgets
import play.api.data.validation.Constraint
import play.api.data.Forms._

case class WidgetConfigMapping(key: String = "",
                               constraints: Seq[Constraint[(DisplayWidgets.Type, WidgetConfig)]] = Nil) extends Mapping[(DisplayWidgets.Type, WidgetConfig)] {

  val widgetMapping = mapping(
    "widget" -> number
  )(DisplayWidgets.apply)(widget => Some(widget.id)).withPrefix(key)

  val mappings: Seq[Mapping[_]] = Seq(this, widgetMapping) ++ DisplayWidgets.values.map {
    widget =>
      widget.configMappger.formMapping.withPrefix(key + "." + widget.toString).asInstanceOf[Mapping[_]]
  }.toSeq

  def withPrefix(prefix: String) = addPrefix(prefix).map(newKey => this.copy(key = newKey)).getOrElse(this)

  def bind(data: Map[String, String]) = {
    widgetMapping.bind(data) match {
      case Right(widget) =>
        widget.configMappger.formMapping.withPrefix(key + "." + widget.toString).bind(data) match {
          case Right(widgetConfig) => Right(widget, widgetConfig)
          case Left(errors) => Left(errors)
        }
      case Left(errors) => Left(errors)
    }
  }

  def unbind(value: (DisplayWidgets.Type, WidgetConfig)) = {
    val widgetData = widgetMapping.unbind(value._1)
    val widgetConfigData = value._1.configMappger.formMapping.withPrefix(key + "." + value._1.toString).unbind(value._2)

    widgetData ++ widgetConfigData
  }


  def unbindAndValidate(value: (Type, WidgetConfig)): (Map[String, String], Seq[FormError]) = {
    val widgetData = widgetMapping.unbindAndValidate(value._1)
    val widgetConfigData = value._1.configMappger.formMapping.withPrefix(key + "." + value._1.toString).unbindAndValidate(value._2)

    (widgetData._1 ++ widgetConfigData._1, widgetData._2 ++ widgetConfigData._2)
  }

  def verifying(addConstraints: Constraint[(DisplayWidgets.Type, WidgetConfig)]*) =
    this.copy(constraints = constraints ++ addConstraints.toSeq)

}
