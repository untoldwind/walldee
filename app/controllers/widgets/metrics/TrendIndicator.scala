package controllers.widgets.metrics

import org.jfree.chart.plot.dial.{DialPlot, AbstractDialLayer}
import java.awt.{Paint, Graphics2D}
import java.awt.geom.{Path2D, Rectangle2D}
import scala.math.{min, sqrt}

class TrendIndicator(rising: Boolean, paint: Paint) extends AbstractDialLayer {
  def isClippedToWindow = false

  def draw(g2: Graphics2D, plot: DialPlot, frame: Rectangle2D, view: Rectangle2D) {
    val triangle = new Path2D.Double(Path2D.WIND_EVEN_ODD)
    val radius = min(frame.getWidth, frame.getHeight) * 0.1
    val sqrt3_2 = sqrt(3.0) / 2.0
    if (rising) {
      triangle.moveTo(frame.getCenterX, frame.getCenterY + radius)
      triangle.lineTo(frame.getCenterX - sqrt3_2 * radius, frame.getCenterY + 2.5 * radius)
      triangle.lineTo(frame.getCenterX + sqrt3_2 * radius, frame.getCenterY + 2.5 * radius)
    } else {
      triangle.moveTo(frame.getCenterX, frame.getCenterY + 3.0 * radius)
      triangle.lineTo(frame.getCenterX + sqrt3_2 * radius, frame.getCenterY + 1.5 * radius)
      triangle.lineTo(frame.getCenterX - sqrt3_2 * radius, frame.getCenterY + 1.5 * radius)
    }
    g2.setPaint(paint)
    g2.fill(triangle)
  }
}
