package views

import java.util.Date

package object utils {
  val dateFormatter = org.joda.time.format.DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")

  def dateFormat(date: Date) = dateFormatter.print(new org.joda.time.DateTime(date))

}
