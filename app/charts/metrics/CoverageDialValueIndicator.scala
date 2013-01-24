package charts.metrics

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
    val value = plot.getValue(this.getDatasetIndex)
    val valueStr = if (value >= 100) "100" else formatter.format(value)

    g2.setPaint(this.getPaint)
    g2.setFont(this.getFont)

    TextUtilities.drawAlignedString(valueStr, g2,
      frame.getX.toFloat + frame.getWidth.toFloat / 2, frame.getHeight.toFloat * 0.23f,
      TextAnchor.CENTER)
  }
}