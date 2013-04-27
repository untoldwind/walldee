package controllers.widgets

import play.api.Play.current
import play.api.mvc.{RequestHeader, Controller}
import play.api.data.Mapping
import models.{DisplayWidgets, DisplayItem, Display}
import play.api.templates.Html
import models.utils.{RenderedWidget, DataDigest}
import play.api.cache.Cache
import xml.NodeSeq

trait Widget[Config] extends Controller {
  def renderHtml(display: Display, displayItem: DisplayItem): Html

  def renderAtom(display: Display, displayItem: DisplayItem)(implicit request: RequestHeader): (NodeSeq, Long) =
    (NodeSeq.Empty, 0)

  def render(display: Display, displayItem: DisplayItem): RenderedWidget = {
    val content = renderHtml(display, displayItem)
    RenderedWidget(displayItem, content)
  }
}

object Widget {
  def cacheKey(display: Display, displayItem: DisplayItem) =
    "renderedWidget-%d-%d".format(display.id.get, displayItem.id.get)

  def forDisplayItem(displayItem: DisplayItem): Widget[_] = {
    displayItem.widget match {
      case DisplayWidgets.Burndown => Burndown
      case DisplayWidgets.SprintTitle => SprintTitle
      case DisplayWidgets.Clock => Clock
      case DisplayWidgets.Alarms => Alarms
      case DisplayWidgets.IFrame => IFrame
      case DisplayWidgets.BuildStatus => BuildStatus
      case DisplayWidgets.HostStatus => HostStatus
      case DisplayWidgets.Metrics => Metrics
    }
  }

  def getRenderedWidget(display: Display, displayItem: DisplayItem): RenderedWidget = {
    Cache.getOrElse(cacheKey(display, displayItem)) {
      forDisplayItem(displayItem).render(display, displayItem)
    }
  }
}