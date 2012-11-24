package controllers.widgets

import play.api.data.Forms._
import models.statusValues.MetricSeverityTypes
import models._
import play.api.templates.Html
import play.api.mvc.{Controller, Action}
import widgetConfigs.{MetricsConfig, MetricsItem, MetricsItemTypes}
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import org.jfree.chart.JFreeChart
import java.awt.{BasicStroke, Graphics2D, Font, Color}
import org.jfree.data.general.DefaultValueDataset
import org.jfree.chart.plot.dial._
import scala.Some
import java.awt.geom.{Point2D, Arc2D, Rectangle2D}
import org.jfree.chart.plot.dial.DialPointer.Pin
import org.jfree.ui.{TextAnchor, Size2D, RectangleAnchor}
import org.jfree.chart.plot.XYPlot
import org.jfree.text.TextUtilities
import java.text.{DecimalFormatSymbols, DecimalFormat}
import java.util.Locale

object Metrics extends Controller with Widget[MetricsConfig] {
  def itemTypeMapping = number.transform[MetricsItemTypes.Type](
    id => MetricsItemTypes(id),
    itemType => itemType.id
  )

  def severityMapping = mapping(
    "Blocker" -> boolean,
    "Critical" -> boolean,
    "Major" -> boolean,
    "Minor" -> boolean,
    "Info" -> boolean
  ) {
    (blocker, critical, major, minor, info) =>
      MetricSeverityTypes.values
      val severities = Seq.newBuilder[MetricSeverityTypes.Type]
      if (blocker)
        severities += MetricSeverityTypes.Blocker
      if (critical)
        severities += MetricSeverityTypes.Critical
      if (major)
        severities += MetricSeverityTypes.Major
      if (minor)
        severities += MetricSeverityTypes.Minor
      if (info)
        severities += MetricSeverityTypes.Info
      severities.result()
  } {
    severities =>
      Some(
        severities.exists(_ == MetricSeverityTypes.Blocker),
        severities.exists(_ == MetricSeverityTypes.Critical),
        severities.exists(_ == MetricSeverityTypes.Major),
        severities.exists(_ == MetricSeverityTypes.Minor),
        severities.exists(_ == MetricSeverityTypes.Info)
      )
  }

  def metricsItemMapping = mapping(
    "itemType" -> itemTypeMapping,
    "asGauge" -> optional(boolean),
    "valueFont" -> optional(text),
    "valueSize" -> optional(number),
    "warnAt" -> optional(number),
    "severities" -> severityMapping
  )(MetricsItem.apply)(MetricsItem.unapply)

  def configMapping = mapping(
    "labelFont" -> optional(text),
    "labelSize" -> optional(number),
    "columns" -> optional(number),
    "items" -> seq(metricsItemMapping)
  )(MetricsConfig.apply)(MetricsConfig.unapply)

  def renderHtml(display: Display, displayItem: DisplayItem) = {
    display.projectId.map {
      projectId =>
        var statusMonitors = StatusMonitor.finaAllForProject(projectId, Seq(StatusMonitorTypes.Sonar))
        var statusMonitorsWithValues = statusMonitors.map {
          statusMonitor =>
            (statusMonitor,
              StatusValue.findLastForStatusMonitor(statusMonitor.id.get))
        }
        views.html.display.widgets.metrics(display, displayItem, statusMonitorsWithValues)
    }.getOrElse(Html(""))
  }

  def getGaugePng(displayItemId: Long, projectId: Long, itemIdx: Int, etag: String) = Action {
    request =>
      DisplayItem.findById(displayItemId).map {
        displayItem =>
          var statusMonitors = StatusMonitor.finaAllForProject(projectId, Seq(StatusMonitorTypes.Sonar))
          var statusMonitorsWithValues = statusMonitors.map {
            statusMonitor =>
              (statusMonitor,
                StatusValue.findLastForStatusMonitor(statusMonitor.id.get))
          }

          val config = displayItem.metricsConfig.getOrElse(MetricsConfig())
          val width = (displayItem.width - 14) / displayItem.metricsConfig.flatMap(_.columns).getOrElse(1);
          val chart = createCoverageChart(statusMonitorsWithValues, displayItem.style, width, config, config.items(itemIdx))

          val image = chart.createBufferedImage(width, width)
          val out = new ByteArrayOutputStream()

          ImageIO.write(image, "png", out)

          Ok(content = out.toByteArray).withHeaders(CONTENT_TYPE -> "image/png", ETAG -> etag)
      }.getOrElse(NotFound)
  }

  private def createCoverageChart(statusMonitors: Seq[(StatusMonitor, Option[StatusValue])],
                                  style: DisplayStyles.Type, width: Int,
                                  config: MetricsConfig, item: MetricsItem): JFreeChart = {
    val titleFont = new Font("SansSerif", Font.BOLD, (width / 6.4).toInt)
    val valueFont = new Font("SansSerif", Font.BOLD, (width / 3.6).toInt)

    val coverage = statusMonitors.map {
      case (statusMonitor, statusValues) =>
        statusValues.flatMap(_.metricStatus).map(_.coverage).getOrElse(0.0)
    }.max
    val dataSet = new DefaultValueDataset(coverage)
    val plot = new DialPlot
    val frame = new CoverageDialFrame(titleFont)
    plot.setDialFrame(frame)
    plot.addScale(0, new CoverageScale)
    plot.addLayer(new FillPointer())
    val valueIndicator = new CoverageDialValueIndicator(valueFont)
    plot.addLayer(valueIndicator)
    plot.setDataset(dataSet)
    plot.setBackgroundPaint(null)

    val chart = new JFreeChart(null, titleFont, plot, false)
    chart.setBackgroundPaint(null)
    chart
  }
}

class FillPointer extends DialPointer {
  def draw(g2: Graphics2D, plot: DialPlot, frame: Rectangle2D, view: Rectangle2D) {
    g2.setPaint(Color.darkGray)
    g2.fill(frame)
    val value = plot.getValue(0)
    val scale = plot.getScaleForDataset(0)
    val angle = scale.valueToAngle(value)
    val arc = new Arc2D.Double(frame, angle, scale.valueToAngle(0) - angle, Arc2D.PIE)
    g2.setPaint(Color.decode("#00AA00"))
    g2.fill(arc)
  }
}

class CoverageScale extends StandardDialScale(0, 100, 225, -270, 100, 0) {
  override def draw(g2: Graphics2D, plot: DialPlot, frame: Rectangle2D, view: Rectangle2D) {
  }

}

class CoverageDialFrame(font: Font) extends ArcDialFrame(225, -270) {
  setOuterRadius(0.93)

  override def draw(g2: Graphics2D, plot: DialPlot, frame: Rectangle2D, view: Rectangle2D) {
    val pt = new Point2D.Double(frame.getWidth * 0.5, frame.getHeight * 0.9)

    // the indicator bounds is calculated from the templateValue (which
    // determines the minimum size), the maxTemplateValue (which, if
    // specified, provides a maximum size) and the actual value
    val fm = g2.getFontMetrics(font)
    val valueStr = "Coverage"
    val valueBounds = TextUtilities.getTextBounds(valueStr, g2, fm)

    // align this rectangle to the frameAnchor
    val bounds = RectangleAnchor.createRectangle(new Size2D(valueBounds.getWidth, valueBounds.getHeight), pt.getX, pt.getY, RectangleAnchor.CENTER)

    // now find the text anchor point
    val savedClip = g2.getClip()
    g2.clip(bounds)

    val pt2 = RectangleAnchor.coordinates(bounds, RectangleAnchor.RIGHT)
    g2.setPaint(Color.white)
    g2.setFont(this.font)
    TextUtilities.drawAlignedString(valueStr, g2, pt2.getX.toFloat, pt2.getY().toFloat, TextAnchor.CENTER_RIGHT)
    g2.setClip(savedClip)

    g2.setStroke(new BasicStroke(5.0f))
    g2.setPaint(Color.black)
    g2.draw(getWindow(frame))
  }
}

class CoverageDialValueIndicator(font: Font) extends DialValueIndicator {
  setAngle(90)
  setRadius(0.52)
  setFont(font)
  setPaint(Color.white)

  val formatter = new DecimalFormat("0.0", DecimalFormatSymbols.getInstance(Locale.US))

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