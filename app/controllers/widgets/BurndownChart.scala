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

object BurndownChart extends Controller {
  def getPng(sprintId: Long, width: Int, height: Int) = Action {
    val chart = createChart(createLeftDataset, createRightDataset)

    val image = chart.createBufferedImage(width, height)
    val out = new ByteArrayOutputStream()

    ImageIO.write(image, "png", out)

    Ok(content = out.toByteArray)
  }

  /**
   * Returns a sample dataset.
   *
   * @return The dataset.
   */
  private def createLeftDataset: XYDataset = {

    // create the dataset...
    val dataset = new DefaultTableXYDataset();
    val series1 = new XYSeries("Series 1", false, false);
    series1.add(1.0, 20.0);
    series1.add(2.0, 50.0);
    series1.add(3.0, 40.0);
    series1.add(4.0, 70.0);
    dataset.addSeries(series1);
    val series2 = new XYSeries("Series 1", false, false);
    series2.add(1.0, 25.0);
    series2.add(2.0, 23.0);
    series2.add(3.0, 19.0);
    series2.add(4.0, 1.0);
    dataset.addSeries(series2);

    dataset
  }

  private def createRightDataset: XYDataset = {

    // create the dataset...
    val dataset = new DefaultTableXYDataset();
    val series3 = new XYSeries("Series 3", false, false);
    series3.add(1.0, 1.0);
    series3.add(2.0, 2.0);
    series3.add(3.0, 3.0);
    series3.add(4.0, 2.0);
    dataset.addSeries(series3);

    dataset
  }

  private def createChart(leftDataset: XYDataset, rightDataset: XYDataset): JFreeChart = {

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

    plot.setRenderer(0, rendererLeft);
    plot.setRenderer(1, rendererRight);

    val domainAxis = new NumberAxis("Days");
    domainAxis.setStandardTickUnits(
      NumberAxis.createIntegerTickUnits());
    domainAxis.setRange(0, 10.0)
    plot.setDomainAxis(domainAxis)

    new JFreeChart("Score Bord", new Font("SansSerif", Font.BOLD, 12),
      plot, true)
  }
}
