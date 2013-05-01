package templates

import play.api.mvc.{Codec, Content}
import play.templates.{Format, Appendable}
import org.apache.commons.lang3.StringEscapeUtils
import play.api.http.{ContentTypes, ContentTypeOf}

class Js(val buffer: StringBuilder) extends Appendable[Js] with Content with play.mvc.Content {
  def +=(other: Js): Js = {
    buffer.append(other.buffer)
    this
  }

  override def toString = buffer.toString

  def contentType: String = "text/javascript"

  def body: String = toString
}

object Js {
  def apply(text: String): Js = {
    new Js(new StringBuilder(text))
  }

  def empty: Js = new Js(new StringBuilder())

  implicit def contentTypeOf_Xml(implicit codec: Codec): ContentTypeOf[Js] = {
    ContentTypeOf[Js](Some(ContentTypes.JAVASCRIPT))
  }
}

object JsFormat extends Format[Js] {
  def raw(text: String): Js = Js(text)

  def escape(text: String): Js = Js(StringEscapeUtils.escapeEcmaScript(text))
}