package controllers.widgets

import play.api.mvc.{Action, Controller}
import org.jfree.data.category.{CategoryDataset, DefaultCategoryDataset}
import org.jfree.chart.{JFreeChart, StandardChartTheme, ChartFactory}
import org.jfree.chart.plot.{CategoryPlot, PlotOrientation}
import java.awt.{Color, GradientPaint, Font}
import org.jfree.chart.axis.{CategoryAxis, CategoryLabelPositions, NumberAxis}
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import org.jfree.chart.renderer.category.{LineAndShapeRenderer, BarRenderer}
import org.jfree.chart.labels.StandardCategoryToolTipGenerator

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
  private def createLeftDataset: CategoryDataset = {

    // row keys...
    val series1 = "First";
    val series2 = "Second";
    val series3 = "Third";

    // column keys...
    val category1 = "Category 1";
    val category2 = "Category 2";
    val category3 = "Category 3";
    val category4 = "Category 4";
    val category5 = "Category 5";

    // create the dataset...
    val dataset = new DefaultCategoryDataset();

    dataset.addValue(1.0, series1, category1);
    dataset.addValue(4.0, series1, category2);
    dataset.addValue(3.0, series1, category3);
    dataset.addValue(5.0, series1, category4);
    dataset.addValue(5.0, series1, category5);

    dataset.addValue(5.0, series2, category1);
    dataset.addValue(7.0, series2, category2);
    dataset.addValue(6.0, series2, category3);
    dataset.addValue(8.0, series2, category4);
    dataset.addValue(4.0, series2, category5);

    dataset
  }

  private def createRightDataset: CategoryDataset = {

    // row keys...
    val series1 = "First";
    val series2 = "Second";
    val series3 = "Third";

    // column keys...
    val category1 = "Category 1";
    val category2 = "Category 2";
    val category3 = "Category 3";
    val category4 = "Category 4";
    val category5 = "Category 5";

    // create the dataset...
    val dataset = new DefaultCategoryDataset();

    dataset.addValue(4.0, series3, category1);
    dataset.addValue(6.0, series3, category2);
    dataset.addValue(2.0, series3, category3);
    dataset.addValue(3.0, series3, category4);
    dataset.addValue(null, series3, category5);

    dataset
  }

  private def createChart(leftDataset: CategoryDataset, rightDataset: CategoryDataset): JFreeChart = {

    val plot = new CategoryPlot
    val rangeAxisLeft = new NumberAxis("Points");
    rangeAxisLeft.setStandardTickUnits(
      NumberAxis.createIntegerTickUnits());
    val rendererLeft = new LineAndShapeRenderer();
    rendererLeft.setSeriesPaint(0, Color.blue);
    rendererLeft.setBaseToolTipGenerator(
      new StandardCategoryToolTipGenerator());
    plot.setDataset(0, leftDataset)
    plot.setRangeAxis(0, rangeAxisLeft)
    plot.setDomainGridlinesVisible(true);

    val rangeAxisRight = new NumberAxis("Tasks");
    rangeAxisRight.setStandardTickUnits(
      NumberAxis.createIntegerTickUnits());
    plot.setRangeAxis(1, rangeAxisRight);
    plot.setDataset(1, rightDataset);
    plot.mapDatasetToRangeAxis(1, 1);
    val rendererRight = new LineAndShapeRenderer();
    rendererRight.setSeriesPaint(0, Color.red);

    plot.setRenderer(0, rendererLeft);
    plot.setRenderer(1, rendererRight);

    def domainAxis = new CategoryAxis("Over")
    plot.setDomainAxis(domainAxis)
    domainAxis.setCategoryLabelPositions(
      CategoryLabelPositions.createUpRotationLabelPositions(
        scala.math.Pi / 6.0));

    new JFreeChart("Score Bord", new Font("SansSerif", Font.BOLD, 12),
      plot, true)
  }
}
