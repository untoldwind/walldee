package models

import org.scalaquery.ql.{MappedTypeMapper => Mapper}
import java.sql.Timestamp
import java.util.Date

object DateMapper {

  implicit val date2timestamp = Mapper.base[Date, Timestamp](
    dt => if (dt != null) new Timestamp(dt.getTime) else null,
    ts => if (ts != null) new Date(ts.getTime) else null
  )
}

