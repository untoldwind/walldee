package actors.monitorProcessors

import models.statusValues.FreestyleStatus
import org.jsoup.nodes.{Element, Document}
import org.jsoup.select.{Elements, Selector}
import play.api.libs.json.{JsString, JsNumber, JsObject, JsArray, JsValue}
import scala.collection.mutable
import scala.collection.JavaConversions._

object FreestyleHtmlProcessor {


  def processHtml(selectorOpt: Option[String], doc: Document): Option[FreestyleStatus] = {
    selectorOpt.map {
      selector =>
        val elements = Selector.select(selector, doc)
        if (!elements.isEmpty)
          Some(FreestyleStatus(Some(JsObject(handleElements(elements)))))
        else
          None
    }.getOrElse {
      Some(FreestyleStatus(Some(JsObject(handleElement(doc).toSeq))))
    }
  }

  private def handleElements(elements: Elements): Seq[(String, JsValue)] = {
    val fields = mutable.Map.empty[String, JsValue]

    elements.foreach {
      element =>
        handleElement(element).foreach {
          case (key, value) =>
            addField(fields, key, value)
        }
    }
    fields.toSeq.sortBy(_._1)
  }

  private def handleElement(element: Element): Option[(String, JsValue)] = {
    val children = element.children()
    if (element.attributes().isEmpty && children.isEmpty) {
      text(element.text()).map {
        value => (element.tag().getName, value)
      }
    } else {
      val fields = mutable.Map.empty[String, JsValue]

      handleElements(children).foreach {
        case (key, value) =>
          addField(fields, key, value)
      }

      Some(element.tag().getName, JsObject(fields.toSeq.sortBy(_._1)))
    }
  }

  private def addField(fields: mutable.Map[String, JsValue], key: String, value: JsValue) {
    fields.get(key).map {
      case arr: JsArray =>
        fields.put(key, arr :+ value)
      case prev =>
        fields.put(key, JsArray(Seq(prev, value)))
    }.getOrElse {
      fields.put(key, value)
    }
  }

  def text(str: String): Option[JsValue] = {
    if (str.isEmpty)
      None
    else {
      try {
        Some(JsNumber(str.trim.toLong))
      } catch {
        case _: NumberFormatException =>
          Some(JsString(str))
      }
    }
  }
}
