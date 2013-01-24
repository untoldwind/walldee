package charts.metrics

import models.{DisplayStyles, StatusValue, StatusMonitor}
import models.widgetConfigs.{MetricsItem, MetricsConfig}
import charts.Chart
import org.jfree.chart.JFreeChart
import java.awt.{Color, Font}
import org.jfree.chart.plot.dial.DialPlot
import org.jfree.data.general.DefaultValueDataset

class CoverageGauge(statusMonitors: Seq[(StatusMonitor, Seq[StatusValue])],
                    style: DisplayStyles.Type, val width: Int,
                    config: MetricsConfig, item: MetricsItem) extends Chart {
  val height = width

  def jfreeChart: JFreeChart = {
    val titleFont = new Font(config.labelFont.getOrElse("SansSerif"), Font.BOLD, config.labelSize.getOrElse((width / 6.4).toInt))
    val valueFont = new Font(item.valueFont.getOrElse("SansSerif"), Font.BOLD, item.valueSize.getOrElse((width / 3.6).toInt))

    val lastCoverage = if (statusMonitors.isEmpty)
      0.0
    else statusMonitors.map {
      case (statusMonitor, statusValues) =>
        statusValues.headOption.flatMap(_.metricStatus).map(_.coverage).getOrElse(0.0)
    }.max
    val minCoverage = if (statusMonitors.isEmpty)
      0.0
    else
      statusMonitors.map {
        case (statusMonitor, statusValues) =>
          if (statusValues.isEmpty)
            0.0
          else
            statusValues.flatMap(_.metricStatus).map(_.coverage).min
      }.min
    val maxCoverage = if (statusMonitors.isEmpty)
      0.0
    else
      statusMonitors.map {
        case (statusMonitor, statusValues) =>
          if (statusValues.isEmpty)
            0.0
          else
            statusValues.flatMap(_.metricStatus).map(_.coverage).max
      }.max

    val warnAt = item.warnAt.getOrElse(75)
    val plot = new DialPlot
    val frame = new ThreeQuartersDialFrame("Coverage", titleFont)
    plot.setDialFrame(frame)
    plot.addScale(0, new FilledScale(100.0, Color.darkGray))
    if (lastCoverage < maxCoverage) {
      plot.addLayer(new FillPointer(2, if (lastCoverage > warnAt) Colors.warnColor else Colors.warnHighlight))
      plot.addLayer(new FillPointer(0, if (lastCoverage > warnAt) Colors.okColor else Colors.warnColor))
    } else {
      plot.addLayer(new FillPointer(0, if (lastCoverage > warnAt) Colors.okHighlight else Colors.warnHighlight))
      plot.addLayer(new FillPointer(1, if (lastCoverage > warnAt) Colors.okColor else Colors.warnColor))
    }
    val valueIndicator = new CoverageDialValueIndicator(valueFont)
    plot.addLayer(valueIndicator)
    if (item.showTrend.getOrElse(false)) {
      val prevCoverage = if (statusMonitors.isEmpty)
        0.0
      else
        statusMonitors.map {
          case (statusMonitor, statusValues) =>
            if (statusValues.length < 2) 0.0 else statusValues(1).metricStatus.map(_.coverage).getOrElse(0.0)
        }.max

      if (lastCoverage < prevCoverage) {
        plot.addLayer(new TrendIndicator(false, Colors.warnHighlight))
      } else if (lastCoverage > prevCoverage) {
        plot.addLayer(new TrendIndicator(true, Colors.okHighlight))
      }
    }
    plot.setDataset(0, new DefaultValueDataset(lastCoverage))
    plot.setDataset(1, new DefaultValueDataset(minCoverage))
    plot.setDataset(2, new DefaultValueDataset(maxCoverage))
    plot.setBackgroundPaint(null)

    val chart = new JFreeChart(null, titleFont, plot, false)
    chart.setBackgroundPaint(null)
    chart
  }
}
