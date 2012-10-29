package controllers

import play.api.mvc.{Action, Controller}
import models.{DisplayItem, Sprint, Display}
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

  def showConfig(displayId: Long) = Action {
    Display.findById(displayId).map {
      display =>
        Ok(views.html.display.showConfig(display,
          Sprint.findAll(),
          displayForm(display),
          DisplayItem.findAllForDisplay(displayId),
          DisplayItems.displayItemFrom(display)))
    }.getOrElse(NotFound)
  }

  def showWall(displayId: Long) = Action {
    Display.findById(displayId).flatMap {
      display =>
        Sprint.findById(display.sprintId).map {
          sprint =>
            Ok(views.html.display.showWall(display, DisplayItem.findAllForDisplay(displayId), sprint))
        }
    }.getOrElse(NotFound)
  }

  def update(displayId: Long) = Action {
    implicit request =>
      Display.findById(displayId).map {
        display =>
          displayForm(display).bindFromRequest.fold(
          formWithErrors => BadRequest(views.html.display.showConfig(display,
            Sprint.findAll(),
            formWithErrors,
            DisplayItem.findAllForDisplay(displayId),
            DisplayItems.displayItemFrom(display))), {
            display =>
              display.save
              Redirect(routes.Displays.showConfig(displayId))
          })
      }.getOrElse(NotFound)
  }

  def delete(displayId: Long) = Action {
    implicit request =>
      Display.findById(displayId).map {
        display =>
          display.delete
          NoContent
      }.getOrElse(NotFound)
  }

  private def displayForm(display: Display = new Display): Form[Display] = Form(
    mapping(
      "name" -> text(maxLength = 255),
      "sprintId" -> longNumber,
      "backgroundColor" -> text
    ) {
      (name, sprintId, backgroundColor) =>
        display.name = name
        display.sprintId = sprintId
        display.backgroundColor = backgroundColor
        display
    } {
      display => Some((display.name, display.sprintId, display.backgroundColor))
    }
  ).fill(display)
}
