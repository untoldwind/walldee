package controllers

import play.api.mvc.{Action, Controller}
import models.{DayCount, Story}
import play.api.data.Form
import play.api.data.Forms._
import scala.Some
import models.json.SprintCounterValue

object DayCounts extends Controller {
  def create(sprintId: Long) = Action {
    implicit request =>
      storyForm(sprintId).bindFromRequest.fold(
      formWithErrors => BadRequest, {
        story =>
          story.save
          Redirect(routes.Sprints.show(sprintId))
      })
  }

  def storyForm(sprintId: Long): Form[DayCount] =
    dayCountForm(new DayCount(dayNum = 0, sprintId = sprintId))

  def dayCountForm(dayCount: DayCount): Form[DayCount] = Form(
    mapping(
      "dayNum" -> number(min = 0, max = 100),
      "counterValues" -> list(
        mapping(
          "name" -> text,
          "value" -> number
        ) {
          (name, value) => SprintCounterValue(name, value)
        } {
          counterValue => Some(counterValue.name, counterValue.value)
        }
      )
    ) {
      (dayNum, counterValues) =>
        dayCount.dayNum = dayNum
        dayCount.counterValues = counterValues
        dayCount
    } {
      dayCount => Some(dayCount.dayNum, dayCount.counterValues.toList)
    }
  ).fill(dayCount)

}
