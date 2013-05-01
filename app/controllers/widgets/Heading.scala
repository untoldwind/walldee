package controllers.widgets

import models.widgetConfigs.HeadingConfig
import models.{DisplayItem, Display}
import play.api.templates.Html

object Heading extends Widget[HeadingConfig] {
  def renderHtml(display: Display, displayItem: DisplayItem): Html =
    views.html.display.widgets.heading(display, displayItem)

}
