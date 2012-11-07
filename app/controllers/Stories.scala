package controllers

import play.api.mvc.{Action, Controller}
import models.{Sprint, Story}
import play.api.data._
import play.api.data.Forms._

object Stories extends Controller {
  def create(sprintId: Long) = Action {
    implicit request =>
      Sprint.findById(sprintId).map {
        sprint =>
          storyForm(sprint).bindFromRequest.fold(
          formWithErrors => BadRequest, {
            story =>
              story.insert
              Redirect(routes.Sprints.show(sprintId))
          })
      }.getOrElse(NotFound)
  }

  def storyForm(sprint: Sprint): Form[Story] =
    storyForm(new Story(id = None, tag = "", description = "", points = 0, sprintId = sprint.id.get))

  def storyForm(story: Story): Form[Story] = Form(
    mapping(
      "id" -> ignored(story.id),
      "sprintId" -> ignored(story.sprintId),
      "tag" -> text(maxLength = 50),
      "description" -> text(maxLength = 1000),
      "points" -> number(min = 0, max = 100)
    )(Story.apply)(Story.unapply)).fill(story)
}
