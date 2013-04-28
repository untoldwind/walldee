package controllers.widgets

import play.api.mvc.{Action, Controller}
import models._
import play.api.data.Forms._
import models.widgetConfigs.BurndownConfig
import play.api.templates.Html
import models.utils.DataDigest
import charts.burndown.BurndownChart

object Burndown extends Controller with Widget[BurndownConfig] {
  override def renderHtml(display: Display, displayItem: DisplayItem): Html = {
    (for {
      sprintId <- getSprintId(display, displayItem)
      sprint <- Sprint.findById(sprintId)
    } yield {
      views.html.display.widgets.burndown.render(display, displayItem, calculateETag(displayItem, sprint))
    }).getOrElse(Html(""))
  }

  def getPng(displayItemId: Long, token: String, width: Int, height: Int) = Action {
    request =>
      (for {
        displayItem <- DisplayItem.findById(displayItemId)
        display <- Display.findById(displayItem.displayId)
        sprintId <- getSprintId(display, displayItem)
        sprint <- Sprint.findById(sprintId)
      } yield {
        request.headers.get(IF_NONE_MATCH).filter(_ == token).map(_ => NotModified).getOrElse {
          val chart = new BurndownChart(width, height, sprint, display.style,
            displayItem.widgetConfig[BurndownConfig].getOrElse(BurndownConfig()))

          Ok(content = chart.toPng).withHeaders(CONTENT_TYPE -> "image/png", ETAG -> token)
        }
      }).getOrElse(NotFound)
  }

  private def getSprintId(display: Display, displayItem: DisplayItem): Option[Long] = {
    val teamIdOpt = displayItem.teamId.map(Some(_)).getOrElse(display.teamId)

    for {
      teamId <- teamIdOpt
      team <- Team.findById(teamId)
      sprintId <- team.currentSprintId
    } yield sprintId
  }

  private def calculateETag(displayItem: DisplayItem, sprint: Sprint): String = {

    val dataDigest = DataDigest()

    dataDigest.update(displayItem.id)
    dataDigest.update(displayItem.width)
    dataDigest.update(displayItem.height)
    dataDigest.update(displayItem.widgetConfigJson)
    dataDigest.update(sprint.id)
    dataDigest.update(sprint.numberOfDays)
    dataDigest.update(sprint.languageTag)
    sprint.counters.foreach {
      counter =>
        dataDigest.update(counter.name)
        dataDigest.update(counter.color)
    }
    DayCount.findAllForSprint(sprint.id.get).foreach {
      dayCount =>
        dataDigest.update(dayCount.id)
        dataDigest.update(dayCount.dayNum)
        dayCount.counterValues.foreach {
          counterValue =>
            dataDigest.update(counterValue.value)
        }
    }

    dataDigest.base64Digest()
  }
}
