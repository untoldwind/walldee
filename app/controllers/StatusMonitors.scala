package controllers

import play.api.mvc.{Action, Controller}
import models.{Project, StatusValue, StatusMonitor}
import play.api.data._
import play.api.data.Forms._
import models.statusMonitors.{FreestyleTypes, FreestyleConfig, IcingaExpected, IcingaConfig}
import scala.util.matching.Regex
import play.api.libs.json.{JsArray, Json}

object StatusMonitors extends Controller {
  def index(projectId: Long) = Action {
    implicit request =>
      render {
        case Accepts.Json() =>
          Project.findById(projectId).map {
            project =>
              Ok(JsArray(StatusMonitor.finaAllForProject(projectId).map(Json.toJson(_))))
          }.getOrElse(NotFound)
      }
  }

  def create(projectId: Long) = Action {
    implicit request =>
      Project.findById(projectId).map {
        project =>
          statusMonitorForm(projectId).bindFromRequest.fold(
          formWithErrors => BadRequest(views.html.projects.show(project, StatusMonitor.findAllGroupedByType(projectId), Projects.projectForm(project), formWithErrors)), {
            statusMonitor =>
              statusMonitor.insert
              Ok(views.html.projects.show(project, StatusMonitor.findAllGroupedByType(projectId), Projects.projectForm(project), statusMonitorForm(projectId)))
          })
      }.getOrElse(NotFound)
  }

  def show(projectId: Long, statusMonitorId: Long) = Action {
    (for {
      project <- Project.findById(projectId)
      statusMonitor <- StatusMonitor.findById(statusMonitorId)
    } yield {
      Ok(views.html.statusMonitors.show(project, statusMonitor, StatusValue.findAllForStatusMonitor(statusMonitorId)))
    }).getOrElse(NotFound)
  }

  def values(projectId: Long, statusMonitorId: Long) = Action {
    implicit request =>
      render {
        case Accepts.Json() =>
          Project.findById(projectId).flatMap {
            project =>
              StatusMonitor.findById(statusMonitorId).map {
                statusMonitor =>
                  Ok(JsArray(StatusValue.findAllForStatusMonitor(statusMonitorId).map(Json.toJson(_))))
              }
          }.getOrElse(NotFound)
      }
  }

  def edit(projectId: Long, statusMonitorId: Long) = Action {
    (for {
      project <- Project.findById(projectId)
      statusMonitor <- StatusMonitor.findById(statusMonitorId)
    } yield {
      Ok(views.html.statusMonitors.edit(project, statusMonitor, statusMonitorForm(statusMonitor)))
    }).getOrElse(NotFound)
  }

  def update(projectId: Long, statusMonitorId: Long) = Action {
    implicit request =>
      (for {
        project <- Project.findById(projectId)
        statusMonitor <- StatusMonitor.findById(statusMonitorId)
      } yield {
        statusMonitorForm(statusMonitor).bindFromRequest.fold(
        formWithErrors => BadRequest(views.html.statusMonitors.edit(project, statusMonitor, formWithErrors)), {
          statusMonitor =>
            statusMonitor.update
            Ok(views.html.statusMonitors.edit(project, statusMonitor, statusMonitorForm(statusMonitor)))
        })
      }).getOrElse(NotFound)
  }

  def delete(projectId: Long, statusMonitorId: Long) = Action {
    (for {
      project <- Project.findById(projectId)
      statusMonitor <- StatusMonitor.findById(statusMonitorId)
    } yield {
      statusMonitor.delete
      Ok(views.js.statusMonitors.ajaxList(StatusMonitor.findAllGroupedByType(projectId)))
    }).getOrElse(NotFound)
  }

  def statusMonitorForm(projectId: Long): Form[StatusMonitor] =
    statusMonitorForm(new StatusMonitor(projectId))

  private def statusMonitorForm(statusMonitor: StatusMonitor): Form[StatusMonitor] = Form(
    mapping(
      "id" -> ignored(statusMonitor.id),
      "projectId" -> ignored(statusMonitor.projectId),
      "name" -> text,
      "typeNum" -> number,
      "url" -> text(),
      "username" -> optional(text),
      "password" -> optional(text),
      "active" -> boolean,
      "keepHistory" -> number,
      "updatePeriod" -> number,
      "lastQueried" -> ignored(statusMonitor.lastQueried),
      "lastUpdated" -> ignored(statusMonitor.lastUpdated),
      "icingaConfig" -> optional(icingaConfigMapping),
      "freestyleConfig" -> optional(freestyleConfigMapping)
    )(StatusMonitor.formApply)(StatusMonitor.formUnapply)).fill(statusMonitor)

  private def icingaConfigMapping = mapping(
    "hostNameFilter" -> optional(regexMapping),
    "expected" -> seq(icingaExpectedMapping)
  )(IcingaConfig.apply)(IcingaConfig.unapply)

  private def freestyleConfigMapping = mapping(
    "freestyleType" -> freestyleTypeMapping,
    "selector" -> optional(text)
  )(FreestyleConfig.apply)(FreestyleConfig.unapply)

  private def freestyleTypeMapping = number.transform[FreestyleTypes.Type](
    i => FreestyleTypes(i),
    freestyleType => freestyleType.id
  )

  private def regexMapping = text.transform[Regex](
    str => str.r,
    regex => regex.toString()
  )

  private def icingaExpectedMapping = mapping(
    "host" -> text,
    "criticals" -> number,
    "warnings" -> number
  )(IcingaExpected.apply)(IcingaExpected.unapply)
}
