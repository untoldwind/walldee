package charts.metrics

import models.{DisplayStyles, StatusValue, StatusMonitor}
import models.widgetConfigs.{MetricsItem, MetricsConfig}
import org.jfree.chart.JFreeChart
import java.awt.{Color, Font}
import org.jfree.data.general.DefaultValueDataset
import org.jfree.chart.plot.dial.DialPlot
import charts.Chart

class ViolationsGauge(statusMonitors: Seq[(StatusMonitor, Seq[StatusValue])],
                      style: DisplayStyles.Type, val width: Int,
                      config: MetricsConfig, item: MetricsItem) extends Chart {
  val height = width

  def jfreeChart: JFreeChart = {
    val titleFont = new Font(config.labelFont.getOrElse("SansSerif"), Font.BOLD, config.labelSize.getOrElse((width / 6.4).toInt))
    val valueFont = new Font(item.valueFont.getOrElse("SansSerif"), Font.BOLD, item.valueSize.getOrElse((width / 3.6).toInt))

    val currentViolations = if (statusMonitors.isEmpty)
      0
    else statusMonitors.map {
      case (statusMonitor, statusValues) =>
        val violations = statusValues.headOption.flatMap(_.metricStatus).map(_.violations).getOrElse(Seq.empty)
        violations.filter(s => item.severities.contains(s.severity)).map(_.count).sum
    }.max
    val maxViolations = if (statusMonitors.isEmpty)
      1
    else statusMonitors.map {
      case (statusMonitor, statusValues) =>
        if (statusValues.isEmpty)
          1
        else statusValues.map {
          statusValue =>
            statusValue.metricStatus.map {
              metricStatus =>
                val violationCount = metricStatus.violations.filter(s => item.severities.contains(s.severity)).map(_.count).sum
                if (violationCount <= 0) 1 else violationCount
            }.getOrElse(1)
        }.max
    }.max
    val warnAt = item.warnAt.getOrElse(0)
    val dataSet = new DefaultValueDataset(currentViolations)
    val plot = new DialPlot
    val frame = new ThreeQuartersDialFrame(item.severities.head.toString, titleFont)
    plot.setDialFrame(frame)
    plot.addScale(0, new FilledScale(maxViolations, if (currentViolations > warnAt) Color.darkGray else Colors.okColor))
    plot.addLayer(new FillPointer(0, if (currentViolations > warnAt) Colors.warnHighlight else Colors.okHighlight))
    val valueIndicator = new ViolationsDialValueIndicator(valueFont)
    plot.addLayer(valueIndicator)
    if (item.showTrend.getOrElse(false)) {
      val prevViolations = if (statusMonitors.isEmpty)
        0.0
      else
        statusMonitors.map {
          case (statusMonitor, statusValues) =>
            val violations = if (statusValues.length < 2)
              Seq.empty
            else statusValues(2).metricStatus.map(_.violations).getOrElse(Seq.empty)
            violations.filter(s => item.severities.contains(s.severity)).map(_.count).sum
        }.max

      if (currentViolations < prevViolations) {
        plot.addLayer(new TrendIndicator(false, Colors.okHighlight))
      } else if (currentViolations > prevViolations) {
        plot.addLayer(new TrendIndicator(true, Colors.warnHighlight))
      }
    }
    plot.setDataset(dataSet)
    plot.setBackgroundPaint(null)

    val chart = new JFreeChart(null, titleFont, plot, false)
    chart.setBackgroundPaint(null)
    chart
  }
}
