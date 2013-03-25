package controllers

import play.api.mvc.{Action, Controller}
import models.{Sprint, Team, Project}
import play.api.data.Form
import play.api.data.Forms._

object Teams extends Controller {
  def index = Action {
    Ok(views.html.teams.index(Team.findAll, teamForm()))
  }

  def create = Action {
    implicit request =>
      teamForm().bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.teams.index(Team.findAll, formWithErrors)), {
        project =>
          project.insert
          Ok(views.html.teams.index(Team.findAll, teamForm()))
      })
  }

  def edit(projectId: Long) = Action {
    Team.findById(projectId).map {
      team =>
        Ok(views.html.teams.edit(team, Sprint.findAllForTeam(team.id.get), teamForm(team)))
    }.getOrElse(NotFound)
  }

  def update(teamId: Long) = Action {
    implicit request =>
      Team.findById(teamId).map {
        team =>
          teamForm(team).bindFromRequest.fold(
          formWithErrors => BadRequest(views.html.teams.edit(team, Sprint.findAllForTeam(team.id.get), formWithErrors)), {
            team =>
              team.update
              Ok(views.html.teams.edit(team, Sprint.findAllForTeam(team.id.get), teamForm(team)))
          })
      }.getOrElse(NotFound)
  }

  def delete(projectId: Long) = Action {
    Team.findById(projectId).map {
      team =>
        team.delete
        Ok(views.html.teams.list(Team.findAll))
    }.getOrElse(NotFound)
  }

  private def teamForm(team: Team = new Team): Form[Team] = Form(
    mapping(
      "id" -> ignored(team.id),
      "name" -> text(maxLength = 255),
      "currentSprintId" -> optional(longNumber)
    )(Team.apply)(Team.unapply)).fill(team)
}
