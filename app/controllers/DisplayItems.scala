package controllers

import play.api.mvc.{Action, Controller}
import models.{Team, Project, Display, DisplayItem}
import play.api.data.Form
import play.api.data.Forms._
import widgets.Widget
import models.widgetConfigs._

object DisplayItems extends Controller {
  def create(displayId: Long) = Action {
    implicit request =>
      Display.findById(displayId).map {
        display =>
          displayItemFrom(display).bindFromRequest.fold(
          formWithErrors => BadRequest, {
            displayItem =>
              displayItem.insert
              Redirect(routes.Displays.showConfig(displayId))
          })
      }.getOrElse(NotFound)
  }

  def show(displayId: Long, displayItemId: Long) = Action {
    (for {
      display <- Display.findById(displayId)
      displayItem <- DisplayItem.findById(displayItemId)
    } yield {
      def renderedWidget = Widget.forDisplayItem(displayItem).render(display, displayItem)

      Ok(views.html.displayItem.show(display, displayItem, renderedWidget))
    }).getOrElse(NotFound)
  }

  def edit(displayId: Long, displayItemId: Long) = Action {
    (for {
      display <- Display.findById(displayId)
      displayItem <- DisplayItem.findById(displayItemId)
    } yield {
      Ok(views.html.displayItem.edit(display, displayItem, Display.findAllOther(display.id.get), Project.findAll, Team.findAll,
        displayItemForm(displayItem)))
    }).getOrElse(NotFound)
  }

  def update(displayId: Long, displayItemId: Long) = Action {
    implicit request =>
      (for {
        display <- Display.findById(displayId)
        displayItem <- DisplayItem.findById(displayItemId)
      } yield {
        displayItemForm(displayItem).bindFromRequest.fold(
        formWithErrors => BadRequest(views.html.displayItem.edit(display, displayItem, Display.findAllOther(display.id.get), Project.findAll,
          Team.findAll, formWithErrors)), {
          displayItem =>
            displayItem.update
            Redirect(routes.Displays.showConfig(displayId))
        })
      }).getOrElse(NotFound)
  }

  def delete(displayId: Long, displayItemId: Long) = Action {
    implicit request =>
      (for {
        display <- Display.findById(displayId)
        displayItem <- DisplayItem.findById(displayItemId)
      } yield {
        displayItem.delete
        Ok(views.js.displayItem.ajaxList(display, DisplayItem.findAllForDisplay(displayId)))
      }).getOrElse(NotFound)
  }

  def displayItemFrom(display: Display) =
    displayItemForm(new DisplayItem(None, display.id.get, 0, 0, 0, 0, 0, None, None, false, false, "{}"))

  def displayItemForm(displayItem: DisplayItem) = Form(
    mapping(
      "id" -> ignored(displayItem.id),
      "displayId" -> ignored(displayItem.displayId),
      "posx" -> number(min = 0),
      "posy" -> number(min = 0),
      "width" -> number(min = 0),
      "height" -> number(min = 0),
      "projectId" -> optional(longNumber),
      "teamId" -> optional(longNumber),
      "appearsInFeed" -> boolean,
      "hidden" -> boolean,
      "widgetConfig" -> WidgetConfigMapping()
    )(DisplayItem.formApply)(DisplayItem.formUnapply)).fill(displayItem)
}
