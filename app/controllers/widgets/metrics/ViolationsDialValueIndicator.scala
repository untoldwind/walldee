package controllers.widgets.metrics

import java.awt.{Graphics2D, Color, Font}
import org.jfree.chart.plot.dial.{DialPlot, DialValueIndicator}
import java.text.{DecimalFormatSymbols, DecimalFormat}
import java.util.Locale
import java.awt.geom.{Arc2D, Rectangle2D}
import org.jfree.text.TextUtilities
import org.jfree.ui.{Size2D, RectangleAnchor}

class ViolationsDialValueIndicator(font: Font) extends DialValueIndicator {
  setAngle(90)
  setRadius(0.52)
  setFont(font)
  setPaint(Color.white)

  val formatter = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.US))

  override def draw(g2: Graphics2D, plot: DialPlot, frame: Rectangle2D, view: Rectangle2D) {
    // work out the anchor point
    val f = DialPlot.rectangleByRadius(frame, this.getRadius, this.getRadius)
    val arc = new Arc2D.Double(f, this.getAngle, 0.0, Arc2D.OPEN)
    val pt = arc.getStartPoint()

    // the indicator bounds is calculated from the templateValue (which
    // determines the minimum size), the maxTemplateValue (which, if
    // specified, provides a maximum size) and the actual value
    val fm = g2.getFontMetrics(this.font)
    val value = plot.getValue(this.getDatasetIndex)

    val valueStr = if (value >= 100) "100" else formatter.format(value)
    val valueBounds = TextUtilities.getTextBounds(valueStr, g2, fm)

    // align this rectangle to the frameAnchor
    val bounds = RectangleAnchor.createRectangle(new Size2D(valueBounds.getWidth, valueBounds.getHeight), pt.getX, pt.getY, this.getFrameAnchor)

    // add the insets
    val fb = this.getInsets.createOutsetRectangle(bounds)

    // now find the text anchor point
    val savedClip = g2.getClip()
    g2.clip(fb)

    val pt2 = RectangleAnchor.coordinates(bounds, this.getValueAnchor)
    g2.setPaint(this.getPaint)
    g2.setFont(this.font)
    TextUtilities.drawAlignedString(valueStr, g2, pt2.getX.toFloat, pt2.getY().toFloat, this.getTextAnchor)
    g2.setClip(savedClip)
  }
}