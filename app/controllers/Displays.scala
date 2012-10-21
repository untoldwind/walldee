package controllers

import play.api.mvc.{Action, Controller}
import models.{Sprint, Display}
import play.api.data.Form
import play.api.data.Forms._
import scala.Some
import models.json.SprintCounter

object Displays extends Controller {
  def index = Action {
    Ok(views.html.display.index(Display.findAll(), Sprint.findAll(), displayForm()))
  }

  def create = Action {
    implicit request =>
      displayForm().bindFromRequest().fold(
      formWithErrors => BadRequest(views.html.display.index(Display.findAll(), Sprint.findAll(), formWithErrors)), {
        display =>
          display.save
          Ok(views.html.display.index(Display.findAll(), Sprint.findAll(), displayForm()))
      })
  }

  def showWall(displayId: Long) = Action {
    Display.findById(displayId).map {
      display =>
        Ok(views.html.display.showWall(display))
    }.getOrElse(NotFound)
  }

  private def displayForm(display: Display = new Display): Form[Display] = Form(
    mapping(
      "name" -> text(maxLength = 255),
      "sprintId" -> longNumber
    ) {
      (name, sprintId) =>
        display.name = name
        display.sprintId = sprintId
        display
    } {
      display => Some((display.name, display.sprintId))
    }
  ).fill(display)
}
