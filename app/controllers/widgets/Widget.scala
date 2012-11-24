package controllers.widgets

import play.api.Play.current
import play.api.mvc.Controller
import play.api.data.Mapping
import models.{DisplayWidgets, DisplayItem, Display}
import play.api.templates.Html
import models.utils.{RenderedWidget, DataDigest}
import play.api.cache.Cache

trait Widget[Config] extends Controller {
  def configMapping: Mapping[Config]

  def renderHtml(display: Display, displayItem: DisplayItem): Html

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
      case DisplayWidgets.BurndownChart => BurndownChart
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