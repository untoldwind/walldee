package controllers

import play.api.mvc.{Action, Controller}
import models.{Story, Sprint}
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._

object Sprints extends Controller {
  def index = Action {
    Ok(views.html.sprints.index(Sprint.findAll(), sprintForm()))
  }

  def create = Action {
    implicit request =>
      sprintForm().bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.sprints.index(Sprint.findAll(), formWithErrors)), {
        sprint =>
          sprint.save
          Ok(views.html.sprints.index(Sprint.findAll(), sprintForm()))
      })
  }

  def show(sprintId: Long) = Action {
    Sprint.findById(sprintId).map {
      sprint =>
        Ok(views.html.sprints.show(sprint, Story.findAllForSprint(sprintId), Stories.storyForm(sprintId)))
    }.getOrElse(NotFound)
  }

  private def sprintForm(sprint: Sprint = new Sprint): Form[Sprint] = Form(
    mapping(
      "title" -> text(maxLength = 255),
      "num" -> number(min = 1, max = 10000),
      "sprintStart" -> date("dd-MM-yyyy"),
      "sprintEnd" -> date("dd-MM-yyyy")
    ) {
      (title, num, sprintStart, sprintEnd) =>
        sprint.title = title
        sprint.num = num
        sprint.sprintStart = sprintStart
        sprint.sprintEnd = sprintEnd
        sprint
    } {
      sprint => Some((sprint.title, sprint.num, sprint.sprintStart, sprint.sprintEnd))
    }
  ).fill(sprint)
}