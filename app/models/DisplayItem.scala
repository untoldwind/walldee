package models

import org.squeryl.KeyedEntity

class DisplayItem(val id: Long,
                  val displayId: Long) extends KeyedEntity[Long] {

}
