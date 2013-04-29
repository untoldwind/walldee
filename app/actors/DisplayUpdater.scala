package actors

import play.api.Play.current
import akka.actor.Actor
import akka.event.slf4j.SLF4JLogging
import models._
import controllers.widgets.Widget
import play.api.cache.Cache
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.concurrent.Redeemable
import utils.{DisplayUpdate, RenderedWidget}
import actors.DisplayUpdater.{CheckListeners, FindUpdates}
import scala.collection.mutable
import models.widgetConfigs.SubDisplaysConfig

class DisplayUpdater extends Actor with SLF4JLogging {
  val listeners = mutable.ListBuffer.empty[FindUpdates]

  def receive = {
    case alarm: Alarm =>
      Display.findAll.foreach {
        display =>
          self ! display
      }

    case project: Project => {
      Display.findAllForProject(project.id.get).foreach {
        display =>
          self ! display
      }
      DisplayItem.findAllForProject(project.id.get).foreach {
        displayItem =>
          self ! displayItem
      }
    }

    case team: Team => {
      Display.findAllForTeam(team.id.get).foreach {
        display =>
          self ! display
      }
      DisplayItem.findAllForTeam(team.id.get).foreach {
        displayItem =>
          self ! displayItem
      }
    }

    case statusMonitor: StatusMonitor => {
      Project.findById(statusMonitor.projectId).foreach {
        project =>
          self ! project
      }
    }

    case statusValue: StatusValue =>
      StatusMonitor.findById(statusValue.statusMonitorId).map {
        statusMonitor =>
          self ! statusMonitor
      }

    case sprint: Sprint =>
      Team.findById(sprint.teamId).foreach {
        team =>
          self ! team
      }

    case story: Story =>
      Sprint.findById(story.sprintId).foreach {
        sprint =>
          self ! sprint
      }

    case dayCount: DayCount =>
      Sprint.findById(dayCount.sprintId).foreach {
        sprint =>
          self ! sprint
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

          DisplayItem.findAllOfWidgetType(DisplayWidgets.SubDisplays).
            filter(_.widgetConfig[SubDisplaysConfig].exists(_.displays.exists(_.displayId == display.id.get))).foreach {
            subDisplayItem =>
              self ! subDisplayItem
          }
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
      val removedIds = state.keys.filter(key => !widgetIds.contains(key) && !key.startsWith("sub-")).toSeq
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
