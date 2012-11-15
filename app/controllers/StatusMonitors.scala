package controllers

import play.api.mvc.{Action, Controller}
import models.{Sprint, StatusMonitor}
import play.api.data._
import play.api.data.Forms._

object StatusMonitors extends Controller {
  def index = Action {
    Ok(views.html.statusMonitors.index(StatusMonitor.findAll, statusMonitorForm()))
  }

  def create = Action {
    implicit request =>
      statusMonitorForm().bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.statusMonitors.index(StatusMonitor.findAll, formWithErrors)), {
        statusMonitor =>
          statusMonitor.insert
          Ok(views.html.statusMonitors.index(StatusMonitor.findAll, statusMonitorForm()))
      })
  }

  def show(statusMonitorId: Long) = Action {
    Ok("Bla")
  }

  private def statusMonitorForm(statusMonitor: StatusMonitor = new StatusMonitor): Form[StatusMonitor] = Form(
    mapping(
      "id" -> ignored(statusMonitor.id),
      "name" -> text,
      "typeNum" -> number,
      "url" -> text(),
      "active" -> boolean,
      "keepHistory" -> number
    )(StatusMonitor.apply)(StatusMonitor.unapply)).fill(statusMonitor)
}
