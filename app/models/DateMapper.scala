package models

import java.sql.Timestamp
import java.util.Date
import slick.driver.H2Driver.simple._

object DateMapper {

  implicit val date2timestamp = MappedTypeMapper.base[Date, Timestamp](
    dt => if (dt != null) new Timestamp(dt.getTime) else null,
    ts => if (ts != null) new Date(ts.getTime) else null
  )
}

