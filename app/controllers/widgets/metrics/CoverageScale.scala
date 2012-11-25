package controllers.widgets.metrics

import org.jfree.chart.plot.dial.{DialPlot, StandardDialScale}
import java.awt.{Color, Graphics2D}
import java.awt.geom.Rectangle2D

class CoverageScale extends StandardDialScale(0, 100, 225, -270, 100, 0) {
  override def draw(g2: Graphics2D, plot: DialPlot, frame: Rectangle2D, view: Rectangle2D) {
    g2.setPaint(Color.darkGray)
    g2.fill(frame)
  }

}
