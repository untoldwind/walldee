package controllers.widgets.metrics

import java.awt.{Graphics2D, Color, Font}
import org.jfree.chart.plot.dial.{DialPlot, DialValueIndicator}
import java.text.{DecimalFormatSymbols, DecimalFormat}
import java.util.Locale
import java.awt.geom.{Arc2D, Rectangle2D}
import org.jfree.text.TextUtilities
import org.jfree.ui.{TextAnchor, Size2D, RectangleAnchor}

class CoverageDialValueIndicator(font: Font) extends DialValueIndicator {
  setAngle(90)
  setRadius(0.52)
  setFont(font)
  setPaint(Color.white)

  val formatter = new DecimalFormat("0.0", DecimalFormatSymbols.getInstance(Locale.US))

  override def draw(g2: Graphics2D, plot: DialPlot, frame: Rectangle2D, view: Rectangle2D) {
    val rect = new Rectangle2D.Double(0.22 * frame.getWidth, 0.13 * frame.getHeight,
      0.56 * frame.getWidth, 0.22 * frame.getHeight)
    val value = plot.getValue(this.getDatasetIndex)
    val valueStr = if (value >= 100) "100" else formatter.format(value)
    val fm = g2.getFontMetrics(font.deriveFont(rect.getHeight.toFloat))
    val bounds = TextUtilities.getTextBounds(valueStr, g2, fm)
    val factorW = rect.getWidth / bounds.getWidth
    val factorH = rect.getHeight / -bounds.getY

    val rescaledFont = if (factorW < factorH)
      font.deriveFont((factorW * rect.getHeight).toFloat)
    else
      font.deriveFont((factorH * rect.getHeight).toFloat)

    g2.setPaint(this.getPaint)
    g2.setFont(rescaledFont)

    TextUtilities.drawAlignedString(valueStr, g2,
      rect.getX.toFloat + rect.getWidth.toFloat / 2, rect.getY.toFloat + rect.getHeight.toFloat,
      TextAnchor.BASELINE_CENTER)
  }
}