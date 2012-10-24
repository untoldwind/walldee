package controllers.widgets

import play.api.mvc.{Action, Controller}
import org.jfree.data.category.{CategoryDataset, DefaultCategoryDataset}
import org.jfree.chart.{JFreeChart, StandardChartTheme, ChartFactory}
import org.jfree.chart.plot.{XYPlot, CategoryPlot, PlotOrientation}
import java.awt.{Color, GradientPaint, Font}
import org.jfree.chart.axis.{CategoryAxis, CategoryLabelPositions, NumberAxis}
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import org.jfree.chart.renderer.category.{LineAndShapeRenderer, BarRenderer}
import org.jfree.chart.labels.StandardCategoryToolTipGenerator
import org.jfree.data.xy.{XYSeries, DefaultTableXYDataset, DefaultXYDataset, XYDataset}
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer
import models.{DayCount, Sprint}
import models.json.{SprintCounter, SprintCounterSide}

object BurndownChart extends Controller {
  def getPng(sprintId: Long, width: Int, height: Int) = Action {
    Sprint.findById(sprintId).map {
      sprint =>
        val chart = createChart(sprint)

        val image = chart.createBufferedImage(width, height)
        val out = new ByteArrayOutputStream()

        ImageIO.write(image, "png", out)

        Ok(content = out.toByteArray)
    }.getOrElse(NotFound)
  }

  private def createChart(sprint: Sprint): JFreeChart = {
    val seriesByName = getSeriesByName(sprint)
    fillValues(sprint, seriesByName)
    val (leftSeries, rightSeries) = splitLeftRight(sprint, seriesByName)

    val leftDataset = new DefaultTableXYDataset
    val rendererLeft = new XYLineAndShapeRenderer
    val rangeAxisLeft = new NumberAxis(leftSeries.map(_._1.name).mkString(", "));
    rangeAxisLeft.setStandardTickUnits(
      NumberAxis.createIntegerTickUnits());
    leftSeries.foreach {
      case (counter, series) =>
        val idx = leftDataset.getSeriesCount
        leftDataset.addSeries(series)
        rendererLeft.setSeriesPaint(idx, Color.decode(counter.color))
    }

    val rightDataset = new DefaultTableXYDataset
    val rendererRight = new XYLineAndShapeRenderer
    val rangeAxisRight = new NumberAxis(rightSeries.map(_._1.name).mkString(", "))
    var maxY = 0.0
    rangeAxisRight.setStandardTickUnits(
      NumberAxis.createIntegerTickUnits());
    rightSeries.foreach {
      case (counter, series) =>
        val idx = leftDataset.getSeriesCount
        rightDataset.addSeries(series)
        rendererRight.setSeriesPaint(idx, Color.decode(counter.color))
        maxY = if (series.getMaxY > maxY) series.getMaxY else maxY
    }
    rangeAxisRight.setRange(0.0, maxY * 1.1)

    val plot = new XYPlot
    plot.setDataset(0, leftDataset)
    plot.setRangeAxis(0, rangeAxisLeft)
    plot.setDomainGridlinesVisible(true);
    plot.mapDatasetToRangeAxis(0, 0)

    plot.setRangeAxis(1, rangeAxisRight);
    plot.setDataset(1, rightDataset);
    plot.mapDatasetToRangeAxis(1, 1);

    plot.setRenderer(0, rendererLeft);
    plot.setRenderer(1, rendererRight);

    val domainAxis = new NumberAxis("Days");
    domainAxis.setStandardTickUnits(
      NumberAxis.createIntegerTickUnits());
    domainAxis.setRange(0, sprint.numberOfDays + 1)
    plot.setDomainAxis(domainAxis)

    val chart = new JFreeChart(null, new Font("SansSerif", Font.BOLD, 12),
      plot, true)
    chart.setBackgroundPaint(null)
    chart
  }

  private def getSeriesByName(sprint: Sprint): Map[String, XYSeries] = {
    sprint.counters.map {
      counter =>
        val series = new XYSeries(counter.name, false, false)
        counter.name -> series
    }.toMap
  }

  private def fillValues(sprint: Sprint, seriesByName: Map[String, XYSeries]) {
    DayCount.findAllForSprint(sprint.id).foreach {
      dayCount =>
        dayCount.counterValues.foreach {
          counterValue =>
            seriesByName.get(counterValue.name).map {
              series =>
                series.add(dayCount.dayNum, counterValue.value)
            }
        }
    }
  }

  private def splitLeftRight(sprint: Sprint, seriesByName: Map[String, XYSeries]):
  (Seq[(SprintCounter, XYSeries)], Seq[(SprintCounter, XYSeries)]) = {
    val leftSeries = Seq.newBuilder[(SprintCounter, XYSeries)]
    val rightSeries = Seq.newBuilder[(SprintCounter, XYSeries)]

    sprint.counters.foreach {
      counter =>
        val series = seriesByName(counter.name)
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
