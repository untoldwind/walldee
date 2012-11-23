package actors

import play.api.Play.current
import akka.actor.Actor
import akka.event.slf4j.SLF4JLogging
import models._
import controllers.widgets.Widget
import play.api.cache.Cache

class DisplayUpdater extends Actor with SLF4JLogging {
  def receive = {
    case alarm: Alarm =>
      Display.findAll.foreach {
        display =>
          self ! display
      }

    case project: Project =>
      Display.findAllForProject(project.id.get).foreach {
        display =>
          self ! display
      }

    case statusMonitor: StatusMonitor =>
      Display.findAllForProject(statusMonitor.projectId).foreach {
        display =>
          self ! display
      }

    case statusValue: StatusValue =>
      StatusMonitor.findById(statusValue.statusMonitorId).map {
        statusMonitor =>
          self ! statusMonitor
      }

    case sprint: Sprint =>
      Display.findAllForSprint(sprint.id.get).foreach {
        display =>
          self ! display
      }

    case story: Story =>
      Display.findAllForSprint(story.sprintId).foreach {
        display =>
          self ! display
      }

    case dayCount: DayCount =>
      Display.findAllForSprint(dayCount.sprintId).foreach {
        display =>
          self ! display
      }

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
