package controllers.widgets

import play.api.mvc.{Action, Controller}
import org.jfree.chart.JFreeChart
import org.jfree.chart.plot.XYPlot
import java.awt.{BasicStroke, Stroke, Color, Font}
import org.jfree.chart.axis.NumberAxis
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import org.jfree.data.xy.{XYSeries, DefaultTableXYDataset}
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer
import models.{DisplayItem, DayCount, Sprint}
import models.json.{SprintCounter, SprintCounterSide}
import play.api.data.Forms._
import models.widgetConfigs.BurndownChartConfig
import org.jfree.chart.title.LegendTitle
import org.jfree.ui.{RectangleAnchor, RectangleEdge, RectangleInsets}
import org.jfree.chart.block.LineBorder
import org.jfree.chart.annotations.XYTitleAnnotation

object BurndownChart extends Controller {
  val configMapping = mapping(
    "chartBackground" -> optional(text),
    "plotBackground" -> optional(text),
    "titleSize" -> optional(number),
    "tickSize" -> optional(number),
    "labelSize" -> optional(number),
    "lineWidth" -> optional(number)
  )(BurndownChartConfig.apply)(BurndownChartConfig.unapply)

  def getPng(displayItemId: Long, sprintId: Long, width: Int, height: Int) = Action {
    DisplayItem.findById(displayItemId).flatMap {
      displayItem =>
        Sprint.findById(sprintId).map {
          sprint =>
            val chart = createChart(sprint, displayItem.burndownChartConfig.getOrElse(BurndownChartConfig()))

            val image = chart.createBufferedImage(width, height)
            val out = new ByteArrayOutputStream()

            ImageIO.write(image, "png", out)

            Ok(content = out.toByteArray)
        }
    }.getOrElse(NotFound)
  }

  private def createChart(sprint: Sprint, config: BurndownChartConfig): JFreeChart = {
    val titleFont = new Font("SansSerif", Font.BOLD, config.titleSize.getOrElse(12))
    val tickFont = new Font("SansSerif", Font.PLAIN, config.tickSize.getOrElse(12))
    val labelFont = new Font("SansSerif", Font.PLAIN, config.labelSize.getOrElse(12))
    val lineStroke = new BasicStroke(config.lineWidth.map(_.toFloat).getOrElse(1.0f))

    val seriesSeq = createXYSeriesSeq(sprint)
    fillValues(sprint, seriesSeq)
    val (leftSeries, rightSeries) = splitLeftRight(seriesSeq)

    val leftDataset = new DefaultTableXYDataset
    val rendererLeft = new XYLineAndShapeRenderer
    val rangeAxisLeft = new NumberAxis(leftSeries.map(_._1.name).mkString(", "))
    rangeAxisLeft.setStandardTickUnits(
      NumberAxis.createIntegerTickUnits())
    leftSeries.foreach {
      case (counter, series) =>
        val idx = leftDataset.getSeriesCount
        leftDataset.addSeries(series)
        rendererLeft.setSeriesPaint(idx, Color.decode(counter.color))
        rendererLeft.setSeriesStroke(idx, lineStroke)
    }
    rangeAxisLeft.setLabelFont(labelFont)
    rangeAxisLeft.setTickLabelFont(tickFont)

    val rightDataset = new DefaultTableXYDataset
    val rendererRight = new XYLineAndShapeRenderer
    val rangeAxisRight = new NumberAxis(rightSeries.map(_._1.name).mkString(", "))
    var maxY = 0.0
    rangeAxisRight.setStandardTickUnits(
      NumberAxis.createIntegerTickUnits())
    rightSeries.foreach {
      case (counter, series) =>
        val idx = rightDataset.getSeriesCount
        rightDataset.addSeries(series)
        rendererRight.setSeriesPaint(idx, Color.decode(counter.color))
        rendererRight.setSeriesStroke(idx, lineStroke)
        maxY = if (series.getMaxY > maxY) series.getMaxY else maxY
    }
    rangeAxisRight.setRange(0.0, maxY * 1.1)
    rangeAxisRight.setLabelFont(labelFont)
    rangeAxisRight.setTickLabelFont(tickFont)

    val plot = new XYPlot
    plot.setDataset(0, leftDataset)
    plot.setRangeAxis(0, rangeAxisLeft)
    plot.setDomainGridlinesVisible(true)
    plot.mapDatasetToRangeAxis(0, 0)

    plot.setRangeAxis(1, rangeAxisRight)
    plot.setDataset(1, rightDataset)
    plot.mapDatasetToRangeAxis(1, 1)

    plot.setRenderer(0, rendererLeft)
    plot.setRenderer(1, rendererRight)

    val domainAxis = new NumberAxis("Days")
    domainAxis.setStandardTickUnits(
      NumberAxis.createIntegerTickUnits())
    domainAxis.setRange(0, sprint.numberOfDays + 1)
    domainAxis.setLabelFont(labelFont)
    domainAxis.setTickLabelFont(tickFont)
    plot.setDomainAxis(domainAxis)

    val legend = new LegendTitle(plot);
    legend.setMargin(new RectangleInsets(1.0, 1.0, 1.0, 1.0));
    legend.setFrame(new LineBorder());
    legend.setBackgroundPaint(Color.white);
    val annotation = new XYTitleAnnotation(0.01, 0.01, legend, RectangleAnchor.BOTTOM_LEFT)
    plot.addAnnotation(annotation)

    val chart = new JFreeChart(null, titleFont, plot, false)

    chart.setBackgroundPaint(config.chartBackground.map(Color.decode(_)).getOrElse(null))
    chart
  }

  private def createXYSeriesSeq(sprint: Sprint): Seq[(SprintCounter, XYSeries)] = {
    sprint.counters.map {
      counter =>
        val series = new XYSeries(counter.name, false, false)
        (counter, series)
    }
  }

  private def fillValues(sprint: Sprint, seriesSeq: Seq[(SprintCounter, XYSeries)]) {
    DayCount.findAllForSprint(sprint.id).foreach {
      dayCount =>
        dayCount.counterValues.zipWithIndex.foreach {
          case (counterValue, idx) =>
            seriesSeq(idx)._2.add(dayCount.dayNum, counterValue.value)
        }
    }
  }

  private def splitLeftRight(seriesSeq: Seq[(SprintCounter, XYSeries)]):
  (Seq[(SprintCounter, XYSeries)], Seq[(SprintCounter, XYSeries)]) = {
    val leftSeries = Seq.newBuilder[(SprintCounter, XYSeries)]
    val rightSeries = Seq.newBuilder[(SprintCounter, XYSeries)]

    seriesSeq.foreach {
      case (counter, series) =>
        counter.side match {
          case SprintCounterSide.Left =>
            leftSeries += counter -> series
          case SprintCounterSide.Right =>
            rightSeries += counter -> series
        }
    }
    (leftSeries.result(), rightSeries.result())
  }

}
