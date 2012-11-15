package controllers.widgets

import play.api.mvc.Controller
import play.api.data.Mapping
import models.{DisplayItem, Display}
import play.api.templates.Html
import models.utils.DataDigest

trait Widget[Config] extends Controller {
  def configMapping: Mapping[Config]

  def render(display: Display, displayItem: DisplayItem): Html

  def etag(display: Display, displayItem: DisplayItem): String = {
    val dataDigest = DataDigest()

    dataDigest.update(displayItem.posx)
    dataDigest.update(displayItem.posy)
    dataDigest.update(displayItem.width)
    dataDigest.update(displayItem.height)
    dataDigest.update(displayItem.styleNum)
    dataDigest.update(displayItem.widgetConfigJson)

    dataDigest.base64Digest()
  }
}
