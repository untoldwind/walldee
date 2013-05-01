package controllers

import play.api.mvc.{Action, Controller}
import models.{Team, DayCount, Story, Sprint}
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import models.sprints.{SprintCounter, SprintCounterSide}
import validation.Constraints
import play.api.data.format._

object Sprints extends Controller {
  def create(teamId: Long) = Action {
    implicit request =>
      Team.findById(teamId).map {
        team =>
          sprintForm(new Sprint(teamId)).bindFromRequest.fold(
          formWithErrors => BadRequest(views.html.teams.show(team, Sprint.findAllForTeam(teamId), Teams.teamForm(team), formWithErrors, Team.findAll)), {
            sprint =>
              sprint.insert
              Ok(views.html.teams.show(team, Sprint.findAllForTeam(teamId), Teams.teamForm(team), sprintForm(new Sprint(teamId)), Team.findAll))
          })
      }.getOrElse(NotFound)
  }

  def show(teamId: Long, sprintId: Long) = Action {
    (for {
      team <- Team.findById(teamId)
      sprint <- Sprint.findById(sprintId)
    } yield {
      Ok(views.html.sprints.show(team, sprint,
        Story.findAllForSprint(sprintId), Stories.storyForm(sprint),
        DayCount.findAllForSprint(sprintId), DayCounts.dayCountForm(sprint)))
    }).getOrElse(NotFound)
  }

  def edit(teamId: Long, sprintId: Long) = Action {
    (for {
      team <- Team.findById(teamId)
      sprint <- Sprint.findById(sprintId)
    } yield {
      Ok(views.html.sprints.edit(team, sprint, sprintForm(sprint), Team.findAll))
    }).getOrElse(NotFound)
  }

  def update(teamId: Long, sprintId: Long) = Action {
    implicit request =>
      (for {
        team <- Team.findById(teamId)
        sprint <- Sprint.findById(sprintId)
      } yield {
        sprintForm(sprint).bindFromRequest.fold(
        formWithErrors => BadRequest(views.html.sprints.edit(team, sprint, sprintForm(sprint), Team.findAll)), {
          sprint =>
            sprint.update
            Redirect(routes.Sprints.show(teamId, sprintId))
        })
      }).getOrElse(NotFound)
  }

  def delete(teamId: Long, sprintId: Long) = Action {
    implicit request =>
      (for {
        team <- Team.findById(teamId)
        sprint <- Sprint.findById(sprintId)
      } yield {
        sprint.delete
        Ok(views.js.sprints.ajaxList(Sprint.findAllForTeam(teamId)))
      }).getOrElse(NotFound)
  }

  def sprintForm(sprint: Sprint): Form[Sprint] = Form(
    mapping(
      "id" -> ignored(sprint.id),
      "teamId" -> longNumber,
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