package controllers.widgets.metrics

import java.awt.{BasicStroke, Color, Graphics2D, Font}
import org.jfree.chart.plot.dial.{DialPlot, ArcDialFrame}
import java.awt.geom.{Point2D, Rectangle2D}
import org.jfree.text.TextUtilities
import org.jfree.ui.{TextAnchor, Size2D, RectangleAnchor}

class ThreeQuartersDialFrame(title: String, titleFont: Font) extends ArcDialFrame(225, -270) {
  setOuterRadius(0.93)

  override def draw(g2: Graphics2D, plot: DialPlot, frame: Rectangle2D, view: Rectangle2D) {
    val pt = new Point2D.Double(frame.getWidth * 0.5, frame.getHeight * 0.9)

    // the indicator bounds is calculated from the templateValue (which
    // determines the minimum size), the maxTemplateValue (which, if
    // specified, provides a maximum size) and the actual value
    val fm = g2.getFontMetrics(titleFont)
    val valueBounds = TextUtilities.getTextBounds(title, g2, fm)

    // align this rectangle to the frameAnchor
    val bounds = RectangleAnchor.createRectangle(new Size2D(valueBounds.getWidth, valueBounds.getHeight), pt.getX, pt.getY, RectangleAnchor.CENTER)

    // now find the text anchor point
    val savedClip = g2.getClip
    g2.clip(bounds)

    val pt2 = RectangleAnchor.coordinates(bounds, RectangleAnchor.RIGHT)
    g2.setPaint(Color.white)
    g2.setFont(titleFont)
    TextUtilities.drawAlignedString(title, g2, pt2.getX.toFloat, pt2.getY.toFloat, TextAnchor.CENTER_RIGHT)
    g2.setClip(savedClip)

    g2.setStroke(new BasicStroke(5.0f))
    g2.setPaint(Color.black)
    g2.draw(getWindow(frame))
  }
}