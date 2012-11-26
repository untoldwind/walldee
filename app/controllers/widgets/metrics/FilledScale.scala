package controllers.widgets.metrics

import org.jfree.chart.plot.dial.{DialPlot, StandardDialScale}
import java.awt.{Paint, Color, Graphics2D}
import java.awt.geom.Rectangle2D

class FilledScale(maxValue:Double, paint:Paint) extends StandardDialScale(0, maxValue, 225, -270, maxValue, 0) {
  override def draw(g2: Graphics2D, plot: DialPlot, frame: Rectangle2D, view: Rectangle2D) {
    g2.setPaint(paint)
    g2.fill(frame)
  }

}
