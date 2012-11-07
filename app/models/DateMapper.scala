package models

import org.scalaquery.ql.{MappedTypeMapper => Mapper}
import java.sql.Timestamp
import java.util.Date

object DateMapper {

  implicit val date2timestamp = Mapper.base[Date, Timestamp](
                                                              dt => new Timestamp(dt.getTime),
                                                              ts => new Date(ts.getTime)
                                                            )
}

