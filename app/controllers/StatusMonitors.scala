package controllers

import play.api.mvc.{Action, Controller}
import models.{Project, StatusValue, StatusMonitor}
import play.api.data._
import play.api.data.Forms._
import models.statusMonitors.{IcingaExpected, IcingaConfig}

object StatusMonitors extends Controller {
  def index = Action {
    Ok(views.html.statusMonitors.index(StatusMonitor.findAll, Project.findAll, statusMonitorForm()))
  }

  def create = Action {
    implicit request =>
      statusMonitorForm().bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.statusMonitors.index(StatusMonitor.findAll, Project.findAll, formWithErrors)), {
        statusMonitor =>
          statusMonitor.insert
          Ok(views.html.statusMonitors.index(StatusMonitor.findAll, Project.findAll, statusMonitorForm()))
      })
  }

  def show(statusMonitorId: Long) = Action {
    StatusMonitor.findById(statusMonitorId).map {
      statusMonitor =>
        Ok(views.html.statusMonitors.show(statusMonitor, StatusValue.findAllForStatusMonitor(statusMonitorId)))
    }.getOrElse(NotFound)
  }

  def edit(statusMonitorId: Long) = Action {
    StatusMonitor.findById(statusMonitorId).map {
      statusMonitor =>
        Ok(views.html.statusMonitors.edit(statusMonitor, Project.findAll, statusMonitorForm(statusMonitor)))
    }.getOrElse(NotFound)
  }

  def update(statusMonitorId: Long) = Action {
    implicit request =>
      StatusMonitor.findById(statusMonitorId).map {
        statusMonitor =>
          statusMonitorForm(statusMonitor).bindFromRequest.fold(
          formWithErrors => BadRequest(views.html.statusMonitors.edit(statusMonitor, Project.findAll, formWithErrors)), {
            statusMonitor =>
              statusMonitor.update
              Ok(views.html.statusMonitors.edit(statusMonitor, Project.findAll, statusMonitorForm(statusMonitor)))
          })
      }.getOrElse(NotFound)
  }

  def delete(statusMonitorId: Long) = Action {
    StatusMonitor.findById(statusMonitorId).map {
      statusMonitor =>
        statusMonitor.delete
        Ok(views.html.statusMonitors.list(StatusMonitor.findAll))
    }.getOrElse(NotFound)
  }

  private def statusMonitorForm(statusMonitor: StatusMonitor = new StatusMonitor): Form[StatusMonitor] = Form(
    mapping(
      "id" -> ignored(statusMonitor.id),
      "projectId" -> longNumber,
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
      "icingaConfig" -> optional(icingaConfigMapping)
    )(StatusMonitor.formApply)(StatusMonitor.formUnapply)).fill(statusMonitor)

  private def icingaConfigMapping = mapping(
    "expected" -> seq(icingaExpectedMapping)
  )(IcingaConfig.apply)(IcingaConfig.unapply)

  private def icingaExpectedMapping = mapping(
    "host" -> text,
    "criticals" -> number,
    "warnings" -> number
  )(IcingaExpected.apply)(IcingaExpected.unapply)
}
