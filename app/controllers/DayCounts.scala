package controllers

import play.api.mvc.{Action, Controller}
import models.{Team, Sprint, DayCount, Story}
import play.api.data.Form
import play.api.data.Forms._
import scala.Some
import models.sprints.SprintCounterValue

object DayCounts extends Controller {
  def create(teamId: Long, sprintId: Long) = Action {
    implicit request =>
      (for {
        team <- Team.findById(teamId)
        sprint <- Sprint.findById(sprintId)
      } yield {
        dayCountForm(sprint).bindFromRequest.fold(
        formWithErrors => BadRequest, {
          dayCount =>
            dayCount.insert
            Ok(views.html.sprints.dayCountList(team, sprint, DayCount.findAllForSprint(sprintId)))
        })
      }).getOrElse(NotFound)
  }

  def update(teamId: Long, sprintId: Long, dayCountId: Long) = Action {
    implicit request =>
      (for {
        team <- Team.findById(teamId)
        sprint <- Sprint.findById(sprintId)
        dayCount <- DayCount.findById(dayCountId)
      } yield {
        dayCountForm(dayCount).bindFromRequest.fold(
        formWithErrors => BadRequest, {
          dayCount =>
            dayCount.update
            Ok(views.html.sprints.dayCountList(team, sprint, DayCount.findAllForSprint(sprintId)))
        })
      }).getOrElse(NotFound)
  }

  def delete(teamId: Long, sprintId: Long, dayCountId: Long) = Action {
    (for {
      team <- Team.findById(teamId)
      sprint <- Sprint.findById(sprintId)
      dayCount <- DayCount.findById(dayCountId)
    } yield {
      dayCount.delete
      Ok(views.html.sprints.dayCountList(team, sprint, DayCount.findAllForSprint(sprintId)))
    }).getOrElse(NotFound)
  }

  def dayCountForm(sprint: Sprint): Form[DayCount] = {
    val dayCount = DayCount.formApply(None, sprint.id.get, 0, sprint.counters.map {
      counter =>
        SprintCounterValue(counter.name, 0)
    }.toList)

    dayCountForm(dayCount)
  }

  def dayCountForm(dayCount: DayCount): Form[DayCount] = Form(
    mapping(
      "id" -> ignored(dayCount.id),
      "sprintId" -> ignored(dayCount.sprintId),
      "dayNum" -> number(min = 0, max = 100),
      "counterValues" -> list(
        mapping(
          "name" -> text,
          "value" -> number
        ) {
          (name, value) => SprintCounterValue(name, value)
        } {
          counterValue =>
            Some(counterValue.name, counterValue.value)
        }
      )
    )(DayCount.formApply)(DayCount.formUnapply)).fill(dayCount)

}
