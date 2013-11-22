package controllers

import play.api.mvc.{Action, Controller}
import models.Alarm

object Config extends Controller {
  def index = Action {
    Ok(views.html.config.index())
  }
}
