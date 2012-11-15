package controllers.widgets

import play.api.mvc.{Action, Controller}
import org.jfree.chart.JFreeChart
import org.jfree.chart.plot.XYPlot
import java.awt.{BasicStroke, Stroke, Color, Font}
import org.jfree.chart.axis.{SymbolAxis, NumberAxis}
import java.io.{DataOutputStream, ByteArrayOutputStream}
import javax.imageio.ImageIO
import org.jfree.data.xy.{XYSeries, DefaultTableXYDataset}
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer
import models._
import models.json.{SprintCounter, SprintCounterSide}
import play.api.data.Forms._
import models.widgetConfigs.{AlarmsConfig, BurndownChartConfig}
import org.jfree.chart.title.LegendTitle
import org.jfree.ui.{RectangleAnchor, RectangleEdge, RectangleInsets}
import org.jfree.chart.block.LineBorder
import org.jfree.chart.annotations.XYTitleAnnotation
import play.api.templates.Html
import java.security.MessageDigest
import org.bouncycastle.util.encoders.HexEncoder
import org.apache.commons.codec.digest.DigestUtils
import models.utils.DataDigest

object BurndownChart extends Controller with Widget[BurndownChartConfig] {
  val configMapping = mapping(
    "chartBackground" -> optional(text),
    "plotBackground" -> optional(text),
    "titleSize" -> optional(number),
    "tickSize" -> optional(number),
    "labelSize" -> optional(number),
    "lineWidth" -> optional(number)
  )(BurndownChartConfig.apply)(BurndownChartConfig.unapply)

  def render(display: Display, displayItem: DisplayItem): Html = {
    views.html.display.widgets.burndownChart.render(display, displayItem)
  }

  override def etag(display: Display, displayItem: DisplayItem): String = {
    val dataDigest = DataDigest()

    dataDigest.update(displayItem.posx)
    dataDigest.update(displayItem.posy)
    dataDigest.update(displayItem.width)
    dataDigest.update(displayItem.height)
    dataDigest.update(displayItem.styleNum)
    dataDigest.update(displayItem.widgetConfigJson)

    Sprint.findById(display.sprintId).map {
      sprint =>
        dataDigest.update(calculateETag(displayItem, sprint, displayItem.width, displayItem.height))
    }
    dataDigest.base64Digest()
  }

  def getPng(displayItemId: Long, sprintId: Long, width: Int, height: Int) = Action {
    request =>
      DisplayItem.findById(displayItemId).flatMap {
        displayItem =>
          Sprint.findById(sprintId).map {
            sprint =>
              val etag = calculateETag(displayItem, sprint, width, height) + System.currentTimeMillis().toString

              request.headers.get(IF_NONE_MATCH).filter(_ == etag).map(_ => NotModified).getOrElse {
                val chart = createChart(sprint, displayItem.style,
                  displayItem.burndownChartConfig.getOrElse(BurndownChartConfig()))

                val image = chart.createBufferedImage(width, height)
                val out = new ByteArrayOutputStream()

                ImageIO.write(image, "png", out)

                val response = Ok(content = out.toByteArray)

                response.withHeaders(ETAG -> etag)
              }
          }
      }.getOrElse(NotFound)
  }

  private def createChart(sprint: Sprint, style: DisplayStyles.Type, config: BurndownChartConfig): JFreeChart = {
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
    rangeAxisLeft.setStandardTickUnits(NumberAxis.createIntegerTickUnits())
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
    rangeAxisRight.setStandardTickUnits(NumberAxis.createIntegerTickUnits())
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
    val annotation = new XYTitleAnnotation(0.01, 0.01, legend, RectangleAnchor.BOTTOM_LEFT)
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

  private def calculateETag(displayItem: DisplayItem, sprint: Sprint, width: Int, height: Int) = {
    val dataDigest = DataDigest()

    dataDigest.update(displayItem.id)
    dataDigest.update(sprint.id)
    dataDigest.update(sprint.numberOfDays)
    dataDigest.update(sprint.languageTag)
    sprint.counters.foreach {
      counter =>
        dataDigest.update(counter.name)
        dataDigest.update(counter.color)
    }
    DayCount.findAllForSprint(sprint.id.get).foreach {
      dayCount =>
        dataDigest.update(dayCount.id)
        dataDigest.update(dayCount.dayNum)
        dayCount.counterValues.foreach {
          counterValue =>
            dataDigest.update(counterValue.value)
        }
    }

    dataDigest.base64Digest()
  }
}
