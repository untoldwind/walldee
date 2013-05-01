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
    val html = Html("<tbody>")
    Range(0, values.length, columns).foreach {
      idx =>
        html += Html("<tr>")
        values.slice(idx, idx + columns).foreach {
          value =>
            html += tmpl(value)
        }
    }
    html += Html("</tbody>")
    html
  }

  def mkGrid[A](values: Seq[A], columns: Int = 1, styles: Seq[String] = Seq.empty[String])(tmpl: A => Html) = {
    val html = Html( """<div class="wall-grid %s">""".format(styles.mkString(" ")))
    val rows = if (values.isEmpty) 0 else (values.length - 1) / columns + 1
    Range(0, rows).foreach {
      row =>
        Range(0, columns).foreach {
          column =>
            val idx = row * columns + column
            val top = 100 * row / rows
            val left = 100 * column / columns
            val bottom = 100 * (row + 1) / rows
            val right = 100 * (column + 1) / columns
            html += Html( """<div class="wall-cell" style="left: %d%%;top: %d%%;width: %d%%;height: %d%%;">""".
              format(left, top, right - left, bottom - top))
            if (idx < values.length) {
              html += tmpl(values(idx))
            }
            html += Html("</div>")
        }
    }
    html += Html("</div>")
    html
  }
}
