package models.statusValues

import play.api.libs.json._
import play.api.libs.json.JsObject
import scala.collection.immutable.SortedMap

case class FreestyleStatus(jsValues: Option[JsObject] = None) {
  lazy val values: SortedMap[String, Any] = {
    val builder = SortedMap.newBuilder[String, Any]

    def processValue(key: String, jsValue: JsValue) {
      jsValue match {
        case JsString(value) => builder += key -> value
        case JsBoolean(value) => builder += key -> value
        case JsNumber(value) => builder += key -> value
        case JsObject(value) => processObject(key + ".", value)
        case JsArray(value) => value.zipWithIndex.foreach {
          case (elem, idx) => processValue(key + "[" + idx + "]", elem)
        }
        case _ => // ignore
      }
    }

    def processObject(prefix: String, fields: Seq[(String, JsValue)]) {
      fields.foreach {
        case (key, value) => processValue(prefix + key, value)
      }
    }

    jsValues.foreach(obj => processObject("", obj.fields))

    builder.result()
  }
}

object FreestyleStatus {

  implicit object FreestyleStatusFormat extends Format[FreestyleStatus] {
    override def reads(json: JsValue): JsResult[FreestyleStatus] =
      JsSuccess(FreestyleStatus((json \ "values").asOpt[JsObject]))

    override def writes(freestyleStatus: FreestyleStatus): JsValue = JsObject(
      freestyleStatus.jsValues.map("values" -> _).toSeq)
  }

}