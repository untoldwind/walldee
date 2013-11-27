package controllers

import play.api.mvc.{AnyContentAsJson, Action, Controller}
import models.{StatusMonitor, Project}
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.{JsSuccess, JsArray, Json}

object Projects extends Controller {
  def index = Action {
    implicit request =>
      render {
        case Accepts.Html() =>
          Ok(views.html.projects.index(Project.findAll, projectForm()))
        case Accepts.Json() =>
          Ok(JsArray(Project.findAll.map(Json.toJson(_))))
      }
  }

  def show(projectId: Long) = Action {
    implicit request =>
      Project.findById(projectId).map {
        project =>
          render {
            case Accepts.Html() =>
              Ok(views.html.projects.show(project, StatusMonitor.findAllGroupedByType(projectId), projectForm(project),
                StatusMonitors.statusMonitorForm(projectId)))
            case Accepts.Json() =>
              Ok(Json.toJson(project))
          }
      }.getOrElse(NotFound)
  }

  def create = Action {
    implicit request =>
      projectForm().bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.projects.index(Project.findAll, formWithErrors)), {
        project =>
          project.insert
          Ok(views.html.projects.index(Project.findAll, projectForm()))
      })
  }

  def update(projectId: Long) = Action {
    implicit request =>
      request.body match {
        case AnyContentAsJson(json) =>
          Json.fromJson[Project](json)(Project.jsonReads(Some(projectId))) match {
            case JsSuccess(projectFromJson, _) =>
              if (projectFromJson.update) NoContent else NotFound
            case _ =>
              BadRequest
          }
        case _ =>
          Project.findById(projectId).map {
            project =>
              projectForm(project).bindFromRequest.fold(
              formWithErrors => BadRequest(views.html.projects.show(project, StatusMonitor.findAllGroupedByType(projectId), formWithErrors,
                StatusMonitors.statusMonitorForm(projectId))), {
                project =>
                  project.update
                  Ok(views.html.projects.show(project, StatusMonitor.findAllGroupedByType(projectId), projectForm(project),
                    StatusMonitors.statusMonitorForm(projectId)))
              })
          }.getOrElse(NotFound)
      }
  }

  def delete(projectId: Long) = Action {
    Project.findById(projectId).map {
      project =>
        project.delete
        Ok(views.js.projects.ajaxList(Project.findAll))
    }.getOrElse(NotFound)
  }

  def projectForm(project: Project = new Project): Form[Project] = Form(
    mapping(
      "id" -> ignored(project.id),
      "name" -> text(maxLength = 255)
    )(Project.apply)(Project.unapply)).fill(project)
}
