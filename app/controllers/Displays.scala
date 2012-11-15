package controllers

import play.api.mvc.{Action, Controller}
import models.{DisplayWidgets, DisplayItem, Sprint, Display}
import play.api.data.Form
import play.api.data.Forms._
import scala.Some
import models.json.SprintCounter
import models.utils.{DataDigest, RenderedWidget}
import widgets.Widget

object Displays extends Controller {
  def index = Action {
    Ok(views.html.display.index(Display.findAll, Sprint.findAll, displayForm()))
  }

  def create = Action {
    implicit request =>
      displayForm().bindFromRequest().fold(
      formWithErrors => BadRequest(views.html.display.index(Display.findAll, Sprint.findAll, formWithErrors)), {
        display =>
          display.insert
          Ok(views.html.display.index(Display.findAll, Sprint.findAll, displayForm()))
      })
  }

  def showConfig(displayId: Long) = Action {
    Display.findById(displayId).map {
      display =>
        Ok(views.html.display.showConfig(display,
          Sprint.findAll,
          displayForm(display),
          DisplayItem.findAllForDisplay(displayId),
          DisplayItems.displayItemFrom(display)))
    }.getOrElse(NotFound)
  }

  def showWall(displayId: Long) = Action {
    implicit request =>
      Display.findById(displayId).map {
        display =>
          val displayItems = DisplayItem.findAllForDisplay(displayId)
          val etag = getEtag(display, displayItems)

          request.headers.get(IF_NONE_MATCH).filter(_ == etag).map(_ => NotModified).getOrElse {
            val renderedWidgets = displayItems.map {
              displayItem =>
                val style = displayItem.style.toString.toLowerCase()
                val content = widget(displayItem).render(display, displayItem)

                RenderedWidget(displayItem.posx, displayItem.posy, displayItem.width, displayItem.height, style, content)
            }
            Ok(views.html.display.showWall(display, renderedWidgets)).withHeaders(ETAG -> etag)
          }
      }.getOrElse(NotFound)
  }

  def update(displayId: Long) = Action {
    implicit request =>
      Display.findById(displayId).map {
        display =>
          displayForm(display).bindFromRequest.fold(
          formWithErrors => BadRequest(views.html.display.showConfig(display,
            Sprint.findAll,
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
          Ok(views.html.display.list(Display.findAll))
      }.getOrElse(NotFound)
  }

  private def widget(displayItem: DisplayItem): Widget[_] = {
    displayItem.widget match {
      case DisplayWidgets.BurndownChart => widgets.BurndownChart
      case DisplayWidgets.SprintTitle => widgets.SprintTitle
      case DisplayWidgets.Clock => widgets.Clock
      case DisplayWidgets.Alarms => widgets.Alarms
      case DisplayWidgets.IFrame => widgets.IFrame
    }

  }

  private def getEtag(display: Display, displayItems: Seq[DisplayItem]): String = {
    val dataDigest = DataDigest()

    dataDigest.update(display.id)
    dataDigest.update(display.sprintId)
    dataDigest.update(display.backgroundColor)
    dataDigest.update(display.refreshTime)
    displayItems.foreach {
      displayItem =>
        dataDigest.update(widget(displayItem).etag(display, displayItem))
    }
    dataDigest.base64Digest()
  }

  private def displayForm(display: Display = new Display): Form[Display] = Form(
    mapping(
      "id" -> ignored(display.id),
      "name" -> text(maxLength = 255),
      "sprintId" -> longNumber,
      "backgroundColor" -> text,
      "refreshTime" -> number(min = 1, max = 3600)
    )(Display.apply)(Display.unapply)).fill(display)
}
