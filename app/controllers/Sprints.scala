package controllers

import play.api.mvc.{Action, Controller}
import models.{DayCount, Story, Sprint}
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import models.sprints.{SprintCounter, SprintCounterSide}
import validation.Constraints
import play.api.data.format._

object Sprints extends Controller {
  def index = Action {
    Ok(views.html.sprints.index(Sprint.findAll, sprintForm()))
  }

  def create = Action {
    implicit request =>
      sprintForm().bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.sprints.index(Sprint.findAll, formWithErrors)), {
        sprint =>
          sprint.insert
          Ok(views.html.sprints.index(Sprint.findAll, sprintForm()))
      })
  }

  def show(sprintId: Long) = Action {
    Sprint.findById(sprintId).map {
      sprint =>
        Ok(views.html.sprints.show(sprint,
          Story.findAllForSprint(sprintId), Stories.storyForm(sprint),
          DayCount.findAllForSprint(sprintId), DayCounts.dayCountForm(sprint)))
    }.getOrElse(NotFound)
  }

  def edit(sprintId: Long) = Action {
    Sprint.findById(sprintId).map {
      sprint =>
        Ok(views.html.sprints.edit(sprint, sprintForm(sprint)))
    }.getOrElse(NotFound)
  }

  def update(sprintId: Long) = Action {
    implicit request =>
      Sprint.findById(sprintId).map {
        sprint =>
          sprintForm(sprint).bindFromRequest.fold(
          formWithErrors => BadRequest(views.html.sprints.edit(sprint, sprintForm(sprint))), {
            sprint =>
              sprint.update
              Redirect(routes.Sprints.show(sprintId))
          })
      }.getOrElse(NotFound)
  }

  def delete(sprintId:Long ) = Action {
    implicit request =>
      Sprint.findById(sprintId).map {
        sprint =>
          sprint.delete
          Ok(views.html.sprints.list(Sprint.findAll))
      }.getOrElse(NotFound)
  }

  private def sprintForm(sprint: Sprint = new Sprint): Form[Sprint] = Form(
    mapping(
      "id" -> ignored(sprint.id),
      "title" -> text(maxLength = 255),
      "num" -> number(min = 1, max = 10000),
      "sprintStart" -> sqlDate("dd-MM-yyyy"),
      "sprintEnd" -> sqlDate("dd-MM-yyyy"),
      "languageTag" -> text,
      "counters" -> list(
        mapping(
          "name" -> text,
          "color" -> text,
          "side" -> number
        ) {
          (name, color, side) => SprintCounter(name, color, SprintCounterSide(side))
        } {
          counter => Some(counter.name, counter.color, counter.side.id)
        }
      )
    )(Sprint.formApply)(Sprint.formUnapply)).fill(sprint)
}