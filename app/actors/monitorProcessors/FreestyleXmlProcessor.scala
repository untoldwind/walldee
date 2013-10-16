package actors.monitorProcessors

import org.w3c.dom._
import play.api.libs.json._
import scala.collection.mutable
import models.statusValues.FreestyleStatus
import play.api.libs.json.JsArray
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import scala.Some
import play.api.libs.json.JsNumber
import javax.xml.xpath.{XPathConstants, XPathFactory}

object FreestyleXmlProcessor {
  def processXml(selectorOpt: Option[String], doc: Document): Option[FreestyleStatus] = {
    selectorOpt.map {
      selector =>
        val nodes = selectNodes(selector, doc)
        if (nodes.getLength > 0)
          Some(FreestyleStatus(Some(JsObject(handleNodes(nodes)))))
        else
          None
    }.getOrElse {
      Some(FreestyleStatus(Some(JsObject(handleElement(doc.getDocumentElement).toSeq))))
    }
  }

  private def selectNodes(path: String, doc: Document): NodeList = {
    val factory = XPathFactory.newInstance()
    val xpath = factory.newXPath()

    xpath.evaluate(path, doc, XPathConstants.NODESET).asInstanceOf[NodeList]
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

  private def handleNodes(nodes: NodeList): Seq[(String, JsValue)] = {
    val fields = mutable.Map.empty[String, JsValue]
    Range(0, nodes.getLength).foreach {
      idx =>
        val node = nodes.item(idx)
        if (node.getNodeType == Node.ELEMENT_NODE) {
          handleElement(node.asInstanceOf[Element]).foreach {
            case (key, value) =>
              addField(fields, key, value)
          }
        }
    }
    fields.toSeq.sortBy(_._1)
  }

  private def handleElement(element: Element): Option[(String, JsValue)] = {
    val children = childElements(element)
    if (element.getAttributes.getLength == 0 && children.isEmpty) {
      text(element.getTextContent.trim).map(value => (element.getTagName, value))
    } else {
      val fields = mutable.Map.empty[String, JsValue]

      attributes(element).foreach {
        attr =>
          text(attr.getValue).foreach(value => addField(fields, "@" + attr.getName, value))
      }

      if (children.isEmpty) {
        text(element.getTextContent.trim).foreach(value => addField(fields, "_text", value))
      } else {
        childElements(element).foreach {
          elem =>
            handleElement(elem).foreach {
              case (key, value) =>
                addField(fields, key, value)
            }
        }
      }
      Some(element.getTagName, JsObject(fields.toSeq.sortBy(_._1)))
    }
  }

  def attributes(element: Element): Seq[Attr] = {
    val attrs = element.getAttributes
    Range(0, attrs.getLength).map {
      idx =>
        attrs.item(idx).asInstanceOf[Attr]
    }
  }

  def childElements(element: Element): Seq[Element] = {
    val nodeList = element.getChildNodes
    Range(0, nodeList.getLength).map {
      idx =>
        nodeList.item(idx)
    }.filter(_.getNodeType == Node.ELEMENT_NODE).map(_.asInstanceOf[Element])
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
