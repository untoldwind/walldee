package models.utils

import play.api.templates.Html

case class RenderedWidget(var posx: Int,
                          var posy: Int,
                          var width: Int,
                          var height: Int,
                          var style: String,
                          var content: Html)
