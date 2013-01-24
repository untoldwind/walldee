package charts.burndown

import charts.Chart
import models.{DayCount, DisplayStyles, Sprint}
import models.widgetConfigs.BurndownConfig
import java.awt.{Color, BasicStroke, Font}
import org.jfree.data.xy.{XYSeries, DefaultTableXYDataset}
import org.jfree.chart.renderer.xy.{StackedXYAreaRenderer2, XYAreaRenderer, StackedXYAreaRenderer, XYLineAndShapeRenderer}
import org.jfree.chart.axis.{SymbolAxis, NumberAxis}
import org.jfree.chart.plot.XYPlot
import org.jfree.chart.title.LegendTitle
import org.jfree.ui.{RectangleAnchor, RectangleInsets}
import org.jfree.chart.block.LineBorder
import org.jfree.chart.annotations.XYTitleAnnotation
import org.jfree.chart.JFreeChart
import models.sprints.{SprintCounterSide, SprintCounter}

class BurndownChart(val width: Int, val height: Int, sprint: Sprint,
                    style: DisplayStyles.Type, config: BurndownConfig) extends Chart {

  def jfreeChart = {
    val titleFont = new Font("SansSerif", Font.BOLD, config.titleSize.getOrElse(12))
    val tickFont = new Font("SansSerif", Font.PLAIN, config.tickSize.getOrElse(12))
    val labelFont = new Font("SansSerif", Font.PLAIN, config.labelSize.getOrElse(12))
    val lineStroke = new BasicStroke(config.lineWidth.map(_.toFloat).getOrElse(1.0f))

    val seriesSeq = createXYSeriesSeq(sprint)
    fillValues(sprint, seriesSeq)
    val (leftSeries, rightSeries) = splitLeftRight(seriesSeq)

    val leftDataset = new DefaultTableXYDataset
    val leftStackDataset = new DefaultTableXYDataset
    val rendererLeft = new XYLineAndShapeRenderer
    val rendererLeftStack = new StackedXYAreaRenderer2
    val rangeAxisLeft = new NumberAxis(leftSeries.map(_._1.name).mkString(", "))
    rangeAxisLeft.setStandardTickUnits(NumberAxis.createIntegerTickUnits())
    leftSeries.foreach {
      case (counter, series, false) =>
        val idx = leftDataset.getSeriesCount
        leftDataset.addSeries(series)
        rendererLeft.setSeriesPaint(idx, Color.decode(counter.color))
        rendererLeft.setSeriesStroke(idx, lineStroke)
      case (counter, series, true) =>
        val idx = leftStackDataset.getSeriesCount
        println(">>>>>>>>>>> " + counter.name + " " + series)
        leftStackDataset.addSeries(series)
        rendererLeftStack.setSeriesPaint(idx, Color.decode(counter.color))
    }
    rangeAxisLeft.setLabelFont(labelFont)
    rangeAxisLeft.setTickLabelFont(tickFont)

    val rightDataset = new DefaultTableXYDataset
    val rightStackDataset = new DefaultTableXYDataset
    val rendererRight = new XYLineAndShapeRenderer
    val rendererRightStack = new StackedXYAreaRenderer2
    val rangeAxisRight = new NumberAxis(rightSeries.map(_._1.name).mkString(", "))
    var maxY = 0.0
    rangeAxisRight.setStandardTickUnits(NumberAxis.createIntegerTickUnits())
    rightSeries.foreach {
      case (counter, series, false) =>
        val idx = rightDataset.getSeriesCount
        rightDataset.addSeries(series)
        rendererRight.setSeriesPaint(idx, Color.decode(counter.color))
        rendererRight.setSeriesStroke(idx, lineStroke)
        maxY = if (series.getMaxY > maxY) series.getMaxY else maxY
      case (counter, series, true) =>
        val idx = rightStackDataset.getSeriesCount
        rightStackDataset.addSeries(series)
        rendererRightStack.setSeriesPaint(idx, Color.decode(counter.color))
        maxY = if (series.getMaxY > maxY) series.getMaxY else maxY
    }
    rangeAxisRight.setRange(0.0, maxY * 1.1)
    rangeAxisRight.setLabelFont(labelFont)
    rangeAxisRight.setTickLabelFont(tickFont)

    val plot = new XYPlot
    plot.setDataset(0, leftDataset)
    plot.setDataset(2, leftStackDataset)
    plot.setRangeAxis(0, rangeAxisLeft)
    plot.setDomainGridlinesVisible(true)
    plot.mapDatasetToRangeAxis(0, 0)
    plot.mapDatasetToRangeAxis(2, 0)

    plot.setRangeAxis(1, rangeAxisRight)
    plot.setDataset(1, rightDataset)
    plot.setDataset(3, rightStackDataset)
    plot.mapDatasetToRangeAxis(1, 1)
    plot.mapDatasetToRangeAxis(3, 1)

    plot.setRenderer(0, rendererLeft)
    plot.setRenderer(1, rendererRight)
    plot.setRenderer(2, rendererLeftStack)
    plot.setRenderer(3, rendererRightStack)

    val domainAxis = new SymbolAxis(null, sprint.dayLabels.toArray)
    domainAxis.setStandardTickUnits(
      NumberAxis.createIntegerTickUnits())
    domainAxis.setRange(-0.5, sprint.numberOfDays + 0.5)
    domainAxis.setLabelFont(labelFont)
    domainAxis.setTickLabelFont(tickFont)
    plot.setDomainAxis(domainAxis)

    val legend = new LegendTitle(plot)
    legend.setMargin(new RectangleInsets(1.0, 1.0, 1.0, 1.0))
    legend.setFrame(new LineBorder())
    legend.setBackgroundPaint(Color.white)
    val annotation = new XYTitleAnnotation(0.99, 0.99, legend, RectangleAnchor.TOP_RIGHT)
    plot.addAnnotation(annotation)

    style match {
      case DisplayStyles.Normal =>
        domainAxis.setTickLabelPaint(Color.black)
        rangeAxisLeft.setTickLabelPaint(Color.black)
        rangeAxisLeft.setLabelPaint(Color.black)
        rangeAxisRight.setTickLabelPaint(Color.black)
        rangeAxisRight.setLabelPaint(Color.black)
      case DisplayStyles.Black =>
        domainAxis.setTickLabelPaint(Color.white)
        rangeAxisLeft.setTickLabelPaint(Color.white)
        rangeAxisLeft.setLabelPaint(Color.white)
        rangeAxisRight.setTickLabelPaint(Color.white)
        rangeAxisRight.setLabelPaint(Color.white)
    }
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
    DayCount.findAllForSprint(sprint.id.get).foreach {
      dayCount =>
        dayCount.counterValues.zipWithIndex.foreach {
          case (counterValue, idx) =>
            if (dayCount.dayNum == 0)
              seriesSeq(idx)._2.add(-1, counterValue.value)
            seriesSeq(idx)._2.add(dayCount.dayNum, counterValue.value)
        }
    }
  }

  private def splitLeftRight(seriesSeq: Seq[(SprintCounter, XYSeries)]):
  (Seq[(SprintCounter, XYSeries, Boolean)], Seq[(SprintCounter, XYSeries, Boolean)]) = {
    val leftSeries = Seq.newBuilder[(SprintCounter, XYSeries, Boolean)]
    val rightSeries = Seq.newBuilder[(SprintCounter, XYSeries, Boolean)]

    seriesSeq.foreach {
      case (counter, series) =>
        counter.side match {
          case SprintCounterSide.Left =>
            leftSeries += ((counter, series, false))
          case SprintCounterSide.Right =>
            rightSeries += ((counter, series, false))
          case SprintCounterSide.LeftStack =>
            leftSeries += ((counter, series, true))
          case SprintCounterSide.RightStack =>
            rightSeries += ((counter, series, true))

        }
    }
    (leftSeries.result(), rightSeries.result())
  }
}
