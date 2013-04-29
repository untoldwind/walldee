package controllers.widgets

import models.widgetConfigs.SubDisplaysConfig
import play.api.templates.Html
import models.{Display, DisplayItem}

object SubDisplays extends Widget[SubDisplaysConfig] {
  def renderHtml(display: Display, displayItem: DisplayItem): Html = {
    println(">>>> Render dender " + display)
    (for {
      subDisplayConfig <- displayItem.widgetConfig[SubDisplaysConfig]
      subDisplayId <- subDisplayConfig.displays.headOption.map(_.displayId)
      subDisplay <- Display.findById(subDisplayId)
    } yield {
      println(">>> Meep")
      val renderedWidgets = DisplayItem.findAllForDisplay(subDisplayId).map {
        subDisplayItem =>
          println(">>> Meh " + subDisplayItem)
          Widget.forDisplayItem(subDisplayItem).render(subDisplay, subDisplayItem)
      }
      println(">>> go " + renderedWidgets.length)
      views.html.display.widgets.subDisplay(display, displayItem, renderedWidgets)
    }).getOrElse {
      println(">>> buckup")
      Html("")
    }
  }
}
