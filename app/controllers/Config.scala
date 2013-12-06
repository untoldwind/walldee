package controllers

import play.api.mvc.{Action, Controller}
import models.Alarm

object Config extends Controller {
  def index = Action {
    Ok(views.html.config.index())
  }

  def partial(partial: String) = Action {
    partial match {
      case "projects" => Ok(views.html.config.projects())
      case "teams" => Ok(views.html.config.teams())
      case "statusMonitor" => Ok(views.html.config.statusMonitor())
      case "displays" => Ok(views.html.config.displays())
    }
  }
}
