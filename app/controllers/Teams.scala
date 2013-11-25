package controllers

import play.api.mvc.{Action, Controller}
import models.{Sprint, Team, Project}
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.{Json, JsArray}

object Teams extends Controller {
  def index = Action {
    implicit request =>
      render {
        case Accepts.Html() =>
          Ok(views.html.teams.index(Team.findAll, teamForm()))
        case Accepts.Json() =>
          Ok(JsArray(Team.findAll.map(Json.toJson(_))))
      }
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

  def show(teamId: Long) = Action {
    implicit request =>
      Team.findById(teamId).map {
        team =>
          render {
            case Accepts.Html() =>
              Ok(views.html.teams.show(team, Sprint.findAllForTeam(teamId), teamForm(team), Sprints.sprintForm(new Sprint(teamId)), Team.findAll))
            case Accepts.Json() =>
              Ok(Json.toJson(team))
          }
      }.getOrElse(NotFound)
  }

  def update(teamId: Long) = Action {
    implicit request =>
      Team.findById(teamId).map {
        team =>
          teamForm(team).bindFromRequest.fold(
          formWithErrors => BadRequest(views.html.teams.show(team, Sprint.findAllForTeam(teamId), formWithErrors, Sprints.sprintForm(new Sprint(teamId)), Team.findAll)), {
            team =>
              team.update
              Ok(views.html.teams.show(team, Sprint.findAllForTeam(teamId), teamForm(team), Sprints.sprintForm(new Sprint(teamId)), Team.findAll))
          })
      }.getOrElse(NotFound)
  }

  def delete(teamId: Long) = Action {
    Team.findById(teamId).map {
      team =>
        team.delete
        Ok(views.js.teams.ajaxList(Team.findAll))
    }.getOrElse(NotFound)
  }

  def teamForm(team: Team = new Team): Form[Team] = Form(
    mapping(
      "id" -> ignored(team.id),
      "name" -> text(maxLength = 255),
      "currentSprintId" -> optional(longNumber)
    )(Team.apply)(Team.unapply)).fill(team)
}
