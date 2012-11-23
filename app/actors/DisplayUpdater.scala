package actors

import play.api.Play.current
import akka.actor.Actor
import akka.event.slf4j.SLF4JLogging
import models.{Display, DisplayItem}
import controllers.widgets.Widget
import play.api.cache.Cache

class DisplayUpdater extends Actor with SLF4JLogging {
  def receive = {
    case display: Display =>
      DisplayItem.findAllForDisplay(display.id.get).foreach {
        displayItem =>
          self ! displayItem
      }

    case displayItem: DisplayItem =>
      Display.findById(displayItem.displayId).map {
        display =>
          val rendered = Widget.forDisplayItem(displayItem).render(display, displayItem)
          val cacheKey = Widget.cacheKey(display, displayItem)

          Cache.set(cacheKey, rendered)
      }

    case message =>
      log.error("Received invalid message " + message.toString)
  }
}
