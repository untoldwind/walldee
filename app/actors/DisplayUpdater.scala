package actors

import play.api.Play.current
import akka.actor.Actor
import akka.event.slf4j.SLF4JLogging
import models._
import controllers.widgets.Widget
import play.api.cache.Cache
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.concurrent.{Redeemable, Redeemed}
import utils.{DisplayUpdate, RenderedWidget}
import actors.DisplayUpdater.{CheckListeners, FindUpdates}
import scala.collection.mutable

class DisplayUpdater extends Actor with SLF4JLogging {
  val listeners = mutable.ListBuffer.empty[FindUpdates]

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

    case team: Team =>
      Display.findAllForTeam(team.id.get).foreach {
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
          self ! CheckListeners(display)
      }

    case findUpdates: FindUpdates =>
      if (!findUpdates.check(currentWidgets(findUpdates.display)))
        listeners += findUpdates

    case CheckListeners(display) =>
      listeners.filter(_.display.id == display.id).toSeq.foreach {
        findUpdates =>
          if (findUpdates.check(currentWidgets(findUpdates.display)))
            listeners -= findUpdates
      }

    case message =>
      log.error("Received invalid message " + message.toString)
  }

  private def currentWidgets(display: Display) = {
    DisplayItem.findAllForDisplay(display.id.get).map {
      displayItem =>
        Widget.getRenderedWidget(display, displayItem)
    }
  }
}

object DisplayUpdater {

  case class FindUpdates(display: Display, state: Map[String, String], result: Redeemable[DisplayUpdate]) {
    def check(renderedWidgets: Seq[RenderedWidget]) = {
      val changed = renderedWidgets.filter {
        renderedWidget =>
          state.get(renderedWidget.id).map(_ != renderedWidget.etag).getOrElse(true)
      }
      val widgetIds = renderedWidgets.map(_.id).toSet
      val removedIds = state.keys.filter(!widgetIds.contains(_)).toSeq
      if (!changed.isEmpty || !removedIds.isEmpty) {
        result.redeem {
          DisplayUpdate(removedIds, changed, display.animationConfigJson)
        }
        true
      } else
        false
    }
  }

  case class CheckListeners(display: Display)

}
