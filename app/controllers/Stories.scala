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
              story.save
              Redirect(routes.Sprints.show(sprintId))
          })
      }.getOrElse(NotFound)
  }

  def storyForm(sprint: Sprint): Form[Story] =
    storyForm(new Story(id = 0, tag = "", description = "", points = 0, sprintId = sprint.id))

  def storyForm(story: Story): Form[Story] = Form(
    mapping(
      "tag" -> text(maxLength = 50),
      "description" -> text(maxLength = 1000),
      "points" -> number(min = 0, max = 100)
    ) {
      (tag, description, points) =>
        story.tag = tag
        story.description = description
        story.points = points
        story
    } {
      story => Some(story.tag, story.description, story.points)
    }
  ).fill(story)
}
