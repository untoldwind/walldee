package controllers.widgets.metrics

import org.jfree.chart.plot.dial.{DialPlot, DialPointer}
import java.awt.{Paint, Graphics2D}
import java.awt.geom.{Arc2D, Rectangle2D}

class FillPointer(paint: Paint) extends DialPointer {
  def draw(g2: Graphics2D, plot: DialPlot, frame: Rectangle2D, view: Rectangle2D) {
    val value = plot.getValue(0)
    val scale = plot.getScaleForDataset(0)
    val angle = scale.valueToAngle(value)
    val arc = new Arc2D.Double(frame, angle, scale.valueToAngle(0) - angle, Arc2D.PIE)
    g2.setPaint(paint)
    g2.fill(arc)
  }
}
