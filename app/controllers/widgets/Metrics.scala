package controllers.widgets

import play.api.data.Forms._
import models.statusValues.MetricSeverityTypes
import models._
import scala.Some
import play.api.templates.Html
import utils.DataDigest
import play.api.mvc.{Controller, Action}
import widgetConfigs.{BurndownChartConfig, MetricsConfig, MetricsItem, MetricsItemTypes}
import scala.Some
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import org.jfree.chart.JFreeChart
import java.awt.{BasicStroke, Graphics2D, Font, Color}
import org.jfree.chart.plot._
import org.jfree.data.general.{DefaultPieDataset, DefaultValueDataset}
import org.jfree.chart.plot.dial._
import scala.Some
import dial.DialPointer.{Pin, Pointer}
import scala.Some
import java.awt.geom.{Arc2D, Rectangle2D}

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
    "valueFont" -> optional(text),
    "valueSize" -> optional(number),
    "severities" -> severityMapping
  )(MetricsItem.apply)(MetricsItem.unapply)

  def configMapping = mapping(
    "labelFont" -> optional(text),
    "labelSize" -> optional(number),
    "columns" -> optional(number),
    "items" -> seq(metricsItemMapping)
  )(MetricsConfig.apply)(MetricsConfig.unapply)

  def render(display: Display, displayItem: DisplayItem) = {
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

  def getCoveragePng(displayItemId: Long, projectId: Long, itemIdx: Int, width: Int, height: Int) = Action {
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
          val chart = createCoverageChart(statusMonitorsWithValues, displayItem.style, config, config.items(itemIdx))

          val image = chart.createBufferedImage(width, height)
          val out = new ByteArrayOutputStream()

          ImageIO.write(image, "png", out)

          val response = Ok(content = out.toByteArray).withHeaders("Content-Type" -> "image/png")

          response
      }.getOrElse(NotFound)
  }

  private def createCoverageChart(statusMonitors: Seq[(StatusMonitor, Option[StatusValue])],
                                  style: DisplayStyles.Type, config: MetricsConfig, item: MetricsItem): JFreeChart = {
    val valueFont = new Font("SansSerif", Font.BOLD, item.valueSize.getOrElse(12))

    val coverage = statusMonitors.map {
      case (statusMonitor, statusValues) =>
        statusValues.flatMap(_.metricStatus).map(_.coverage).getOrElse(0.0)
    }.max
    val dataSet = new DefaultValueDataset(coverage)
    val plot = new DialPlot
    val frame = new CoverageDialFrame
    plot.setDialFrame(frame)
    plot.addScale(0, new CoverageScale)
    plot.addPointer(new FillPointer())

    plot.setDataset(dataSet)
    plot.setBackgroundPaint(null)

    val titleFont = new Font("SansSerif", Font.BOLD, config.labelSize.getOrElse(12))
    val chart = new JFreeChart(null, titleFont, plot, false)
    chart.setBackgroundPaint(null)
    chart
  }

  override def etag(display: Display, displayItem: DisplayItem): String = {
    val dataDigest = DataDigest()

    dataDigest.update(displayItem.posx)
    dataDigest.update(displayItem.posy)
    dataDigest.update(displayItem.width)
    dataDigest.update(displayItem.height)
    dataDigest.update(displayItem.styleNum)
    dataDigest.update(displayItem.widgetConfigJson)

    dataDigest.update(display.projectId)
    display.projectId.map {
      projectId =>
        StatusMonitor.finaAllForProject(projectId, Seq(StatusMonitorTypes.Sonar)).foreach {
          statusMonitor =>
            dataDigest.update(statusMonitor.id)
            dataDigest.update(statusMonitor.active)
            StatusValue.findLastForStatusMonitor(statusMonitor.id.get).foreach {
              statusValue =>
                dataDigest.update(statusValue.id)
                dataDigest.update(statusValue.statusNum)
            }
        }
    }

    dataDigest.base64Digest()
  }
}

class FillPointer extends DialPointer {
  def draw(g2: Graphics2D, plot: DialPlot, frame: Rectangle2D, view: Rectangle2D) {
    g2.setPaint(Color.darkGray);
    g2.fill(frame);
    val value = plot.getValue(0);
    val scale = plot.getScaleForDataset(0);
    val angle = scale.valueToAngle(value);
    val arc = new Arc2D.Double(frame, angle, scale.valueToAngle(0)-angle, Arc2D.PIE)
    g2.setPaint(Color.green)
    g2.fill(arc)
  }
}

class CoverageScale extends StandardDialScale(0, 100, 225, -270, 100, 0) {
  override def draw(g2: Graphics2D, plot: DialPlot, frame: Rectangle2D, view: Rectangle2D) {
  }

}

class CoverageDialFrame extends ArcDialFrame(225, -270) {
  setOuterRadius(0.95)

  override def draw(g2: Graphics2D, plot: DialPlot, frame: Rectangle2D, view: Rectangle2D) {
    g2.setStroke(new BasicStroke(5.0f));
    g2.setPaint(Color.black);
    g2.draw(getWindow(frame));
  }
}