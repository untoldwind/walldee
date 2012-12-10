package models

import scala.slick.driver.H2Driver.simple._
import java.sql.Timestamp
import java.util.Date

object DateMapper {

  implicit val date2timestamp = MappedTypeMapper.base[Date, Timestamp](
    dt => if (dt != null) new Timestamp(dt.getTime) else null,
    ts => if (ts != null) new Date(ts.getTime) else null
  )
}

