package controllers.widgets

import play.api.data.Forms._
import models.widgetConfigs.IFrameConfig
import models.{DisplayItem, Display}
import play.api.templates.Html

object IFrame {
  val configMapping = mapping(
    "url" -> optional(text)
  )(IFrameConfig.apply)(IFrameConfig.unapply)

  def render(display: Display, displayItem: DisplayItem): Html = {
    views.html.display.widgets.iframe.render(display, displayItem)
  }
}
