package controllers

import play.api.mvc.{Action, Controller}
import models.{DisplayWidgets, DisplayItem, Sprint, Display}
import play.api.data.Form
import play.api.data.Forms._
import scala.Some
import models.json.SprintCounter
import models.utils.RenderedWidget

object Displays extends Controller {
  def index = Action {
    Ok(views.html.display.index(Display.findAll, Sprint.findAll(), displayForm()))
  }

  def create = Action {
    implicit request =>
      displayForm().bindFromRequest().fold(
      formWithErrors => BadRequest(views.html.display.index(Display.findAll, Sprint.findAll(), formWithErrors)), {
        display =>
          display.insert
          Ok(views.html.display.index(Display.findAll, Sprint.findAll(), displayForm()))
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
    Display.findById(displayId).map {
      display =>
        val renderedWidgets = DisplayItem.findAllForDisplay(displayId).map {
          displayItem =>
            val content = displayItem.widget match {
              case DisplayWidgets.BurndownChart =>
                widgets.BurndownChart.render(display, displayItem)
              case DisplayWidgets.SprintTitle =>
                widgets.SprintTitle.render(display, displayItem)
              case DisplayWidgets.Clock =>
                widgets.Clock.render(display, displayItem)
              case DisplayWidgets.Alarms =>
                widgets.Alarms.render(display, displayItem)
            }
            RenderedWidget(displayItem.posx, displayItem.posy, displayItem.width, displayItem.height, content)
        }
        Ok(views.html.display.showWall(display, renderedWidgets))
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
              display.update
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
      "id" -> ignored(display.id),
      "name" -> text(maxLength = 255),
      "sprintId" -> longNumber,
      "backgroundColor" -> text
    )(Display.apply)(Display.unapply)).fill(display)
}
