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
import models.json.SprintCounterSide

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
    val (leftDataset, rightDataset) = createDatasets(sprint)
    val plot = new XYPlot
    val rangeAxisLeft = new NumberAxis("Points");
    rangeAxisLeft.setStandardTickUnits(
      NumberAxis.createIntegerTickUnits());
    val rendererLeft = new XYLineAndShapeRenderer();
    rendererLeft.setSeriesPaint(0, Color.blue);
    rendererLeft.setSeriesPaint(1, Color.green)
    plot.setDataset(0, leftDataset)
    plot.setRangeAxis(0, rangeAxisLeft)
    plot.setDomainGridlinesVisible(true);
    plot.mapDatasetToRangeAxis(0, 0)

    val rangeAxisRight = new NumberAxis("Tasks");
    rangeAxisRight.setStandardTickUnits(
      NumberAxis.createIntegerTickUnits());
    plot.setRangeAxis(1, rangeAxisRight);
    plot.setDataset(1, rightDataset);
    plot.mapDatasetToRangeAxis(1, 1);
    val rendererRight = new XYLineAndShapeRenderer();
    rendererRight.setSeriesPaint(0, Color.red);
    rendererRight.setSeriesPaint(1, Color.yellow);

    plot.setRenderer(0, rendererLeft);
    plot.setRenderer(1, rendererRight);

    val domainAxis = new NumberAxis("Days");
    domainAxis.setStandardTickUnits(
      NumberAxis.createIntegerTickUnits());
    domainAxis.setRange(0, sprint.numberOfDays)
    plot.setDomainAxis(domainAxis)

    new JFreeChart("Score Bord", new Font("SansSerif", Font.BOLD, 12),
      plot, true)
  }

  private def createDatasets(sprint: Sprint) = {
    val seriesByName = sprint.counters.map {
      counter =>
        val series = new XYSeries(counter.name, false, false)
        counter.name -> series
    }.toMap
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
    val leftDataset = new DefaultTableXYDataset
    val rightDataset = new DefaultTableXYDataset
    sprint.counters.foreach {
      counter =>
        seriesByName(counter.name)
        counter.side match {
          case SprintCounterSide.Left => leftDataset.addSeries(seriesByName(counter.name))
          case SprintCounterSide.Right => rightDataset.addSeries(seriesByName(counter.name))
        }
    }

    (leftDataset, rightDataset)
  }
}
