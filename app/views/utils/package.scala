package views

import java.util.Date
import util.matching.Regex
import play.api.templates.Html

package object utils {
  val dateFormatter = org.joda.time.format.DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")

  def dateFormat(date: Date): String = dateFormatter.print(new org.joda.time.DateTime(date))

  def dateFormat(dateOpt: Option[Date]): String = dateOpt.map(dateFormat(_)).getOrElse("")

  def simplify(patternOpt: Option[Regex], value: String): String = {
    patternOpt.flatMap {
      pattern =>
        pattern.findFirstMatchIn(value).map {
          found =>
            Range.inclusive(1, found.groupCount).map(found.group(_)).mkString
        }
    }.getOrElse(value)
  }

  def mkTable[A](columns: Int, values: Seq[A])(tmpl: A => Html) = {
    Range(0, values.length, columns).foldLeft(Html("<tbody>")) {
      (html, idx) =>
        values.slice(idx, idx + columns).foldLeft(html + Html("<tr>")) {
          (html, value) =>
            html + tmpl(value)
        }
    } + Html("</tbody>")
  }
}
