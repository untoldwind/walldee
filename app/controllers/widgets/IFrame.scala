package controllers.widgets

import play.api.data.Forms._
import models.widgetConfigs.IFrameConfig
import models.{DisplayItem, Display}
import play.api.templates.Html
import models.utils.DataDigest
import xml.NodeSeq

object IFrame extends Widget[IFrameConfig] {
  override def renderHtml(display: Display, displayItem: DisplayItem): Html = {
    views.html.display.widgets.iframe.render(display, displayItem)
  }
}
