package controllers.widgets

import models.widgetConfigs.SubDisplaysConfig
import play.api.templates.Html
import models.{Display, DisplayItem}

object SubDisplays extends Widget[SubDisplaysConfig] {
  def renderHtml(display: Display, displayItem: DisplayItem): Html = {
    displayItem.widgetConfig[SubDisplaysConfig].map {
      subDisplaysConfig =>
        val subDisplays = subDisplaysConfig.displays.flatMap {
          subDisplayRef =>
            Display.findById(subDisplayRef.displayId).map {
              subDisplay =>
                val renderedWidgets = DisplayItem.findAllForDisplay(subDisplayRef.displayId).map {
                  subDisplayItem =>
                    Widget.forDisplayItem(subDisplayItem).render(subDisplay, subDisplayItem)
                }
                subDisplay -> renderedWidgets
            }.toSeq
        }
        views.html.display.widgets.subDisplay(display, displayItem, subDisplays)
    }.getOrElse(Html(""))
  }
}
