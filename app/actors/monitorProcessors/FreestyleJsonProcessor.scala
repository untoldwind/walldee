package actors.monitorProcessors

import models.statusValues.FreestyleStatus
import play.api.libs.json.{JsArray, JsValue, JsObject}
import scala.collection.JavaConversions._

object FreestyleJsonProcessor {
  val ArraySelect = """([^\[]+)\[([0-9]+)\]""".r

  def processJson(selectorOpt: Option[String], json: JsValue): Option[FreestyleStatus] = {
    selectorOpt.map {
      selector =>
        val node = selector.split("\\.").foldLeft(json) {
          case (js, ArraySelect(name, index)) =>
            (js \ name).as[JsArray].value.get(index.toInt)
          case (js, name) =>
            js \ name
        }

        node match {
          case obj: JsObject =>
            Some(FreestyleStatus(Some(obj)))
          case _ =>
            None
        }
    }.getOrElse {
      json match {
        case obj: JsObject =>
          Some(FreestyleStatus(Some(obj)))
        case _ =>
          None
      }
    }
  }
}
