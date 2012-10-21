package controllers.plugins

import play.api.mvc.{Action, Controller}
import org.jfree.data.category.{CategoryDataset, DefaultCategoryDataset}
import org.jfree.chart.{JFreeChart, StandardChartTheme, ChartFactory}
import org.jfree.chart.plot.{CategoryPlot, PlotOrientation}
import java.awt.{Color, GradientPaint}
import org.jfree.chart.renderer.category.BarRenderer
import org.jfree.chart.axis.{CategoryLabelPositions, NumberAxis}
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import play.api.libs.iteratee.Enumeratee
import java.awt.image.{RenderedImage, BufferedImage}

object BurndownChart extends Controller {
  def getPng(sprintId: Long) = Action {
    val chart = createChart(createDataset)

    val image = chart.createBufferedImage(400, 400)
    val out = new ByteArrayOutputStream()

    ImageIO.write(image, "png", out)
    out.flush()

    println(">>> " + out.toByteArray.length)
    Ok(content = out.toByteArray)
  }

  /**
   * Returns a sample dataset.
   *
   * @return The dataset.
   */
  private def createDataset: CategoryDataset = {

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

    dataset.addValue(4.0, series3, category1);
    dataset.addValue(3.0, series3, category2);
    dataset.addValue(2.0, series3, category3);
    dataset.addValue(3.0, series3, category4);
    dataset.addValue(6.0, series3, category5);

    dataset
  }

  private def createChart(dataset: CategoryDataset): JFreeChart = {

    ChartFactory.setChartTheme(new StandardChartTheme("JFree/Shadow",
      true));

    // create the chart...
    val chart = ChartFactory.createBarChart(
      "Bar Chart Demo 1", // chart title
      "Category", // domain axis label
      "Value", // range axis label
      dataset, // data
      PlotOrientation.VERTICAL, // orientation
      true, // include legend
      true, // tooltips?
      false // URLs?
    );

    // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...

    // set the background color for the chart...
    chart.setBackgroundPaint(Color.white);

    // get a reference to the plot for further customisation...
    def plot = chart.getPlot().asInstanceOf[CategoryPlot];

    // ******************************************************************
    //  More than 150 demo applications are included with the JFreeChart
    //  Developer Guide...for more information, see:
    //
    //  >   http://www.object-refinery.com/jfreechart/guide.html
    //
    // ******************************************************************

    // set the range axis to display integers only...
    def rangeAxis = plot.getRangeAxis().asInstanceOf[NumberAxis];
    rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

    // disable bar outlines...
    def renderer = plot.getRenderer().asInstanceOf[BarRenderer];
    renderer.setDrawBarOutline(false);

    // set up gradient paints for series...
    def gp0 = new GradientPaint(0.0f, 0.0f, Color.blue,
      0.0f, 0.0f, new Color(0, 0, 64));
    def gp1 = new GradientPaint(0.0f, 0.0f, Color.green,
      0.0f, 0.0f, new Color(0, 64, 0));
    def gp2 = new GradientPaint(0.0f, 0.0f, Color.red,
      0.0f, 0.0f, new Color(64, 0, 0));
    renderer.setSeriesPaint(0, gp0);
    renderer.setSeriesPaint(1, gp1);
    renderer.setSeriesPaint(2, gp2);

    def domainAxis = plot.getDomainAxis();
    domainAxis.setCategoryLabelPositions(
      CategoryLabelPositions.createUpRotationLabelPositions(
        scala.math.Pi / 6.0));
    // OPTIONAL CUSTOMISATION COMPLETED.

    return chart;

  }
}
