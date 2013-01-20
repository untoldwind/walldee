package controllers.widgets

import play.api.Play.current
import metrics._
import play.api.data.Forms._
import models.statusValues.MetricSeverityTypes
import models._
import play.api.templates.Html
import play.api.mvc.{RequestHeader, Controller, Action}
import utils.{AtomState, DataDigest}
import widgetConfigs.{MetricsConfig, MetricsItem, MetricsItemTypes}
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import org.jfree.chart.JFreeChart
import java.awt.{Font, Color}
import org.jfree.data.general.DefaultValueDataset
import org.jfree.chart.plot.dial._
import scala.Some
import xml.NodeSeq
import play.api.cache.Cache
import org.joda.time.format.ISODateTimeFormat
import collection.immutable.SortedSet

object Metrics extends Controller with Widget[MetricsConfig] {
  def itemTypeMapping = number.transform[MetricsItemTypes.Type](
    id => MetricsItemTypes(id),
    itemType => itemType.id
  )

  def severityMapping = mapping(
    "Blocker" -> boolean,
    "Critical" -> boolean,
    "Major" -> boolean,
    "Minor" -> boolean,
    "Info" -> boolean
  ) {
    (blocker, critical, major, minor, info) =>
      MetricSeverityTypes.values
      val severities = Seq.newBuilder[MetricSeverityTypes.Type]
      if (blocker)
        severities += MetricSeverityTypes.Blocker
      if (critical)
        severities += MetricSeverityTypes.Critical
      if (major)
        severities += MetricSeverityTypes.Major
      if (minor)
        severities += MetricSeverityTypes.Minor
      if (info)
        severities += MetricSeverityTypes.Info
      severities.result()
  } {
    severities =>
      Some(
        severities.exists(_ == MetricSeverityTypes.Blocker),
        severities.exists(_ == MetricSeverityTypes.Critical),
        severities.exists(_ == MetricSeverityTypes.Major),
        severities.exists(_ == MetricSeverityTypes.Minor),
        severities.exists(_ == MetricSeverityTypes.Info)
      )
  }

  def metricsItemMapping = mapping(
    "itemType" -> itemTypeMapping,
    "asGauge" -> optional(boolean),
    "valueFont" -> optional(text),
    "valueSize" -> optional(number),
    "warnAt" -> optional(number),
    "severities" -> severityMapping
  )(MetricsItem.apply)(MetricsItem.unapply)

  def configMapping = mapping(
    "labelFont" -> optional(text),
    "labelSize" -> optional(number),
    "columns" -> optional(number),
    "items" -> seq(metricsItemMapping)
  )(MetricsConfig.apply)(MetricsConfig.unapply)

  override def renderHtml(display: Display, displayItem: DisplayItem) = {
    val projectIdOpt = displayItem.projectId.map(Some(_)).getOrElse(display.projectId)
    projectIdOpt.map {
      projectId =>
        val statusMonitorsWithValues = getStatusMonitorsWithValues(projectId)
        views.html.display.widgets.metrics(display, displayItem, projectId, statusMonitorsWithValues,
          calculateETag(displayItem, projectId, statusMonitorsWithValues))
    }.getOrElse(Html(""))
  }

  override def renderAtom(display: Display, displayItem: DisplayItem)
                         (implicit request: RequestHeader): (NodeSeq, Long) = {
    val projectIdOpt = displayItem.projectId.map(Some(_)).getOrElse(display.projectId)
    projectIdOpt.map {
      projectId =>
        val html = renderHtml(display, displayItem)
        val dateFormat = ISODateTimeFormat.dateTime().withZoneUTC()
        val lastUpdate = atomLastUpdate(display, displayItem, html)
        val title = displayItem.metricsConfig.map {
          metricsConfig =>
            "Metrics: " + metricsConfig.items.foldLeft(SortedSet.newBuilder[String]) {
            (set, item) =>
              set += item.itemType.toString
              set
          }.result().mkString(", ")
        }.getOrElse("Metrics")

        (<entry>
          <title>
            {title}
          </title>
          <id>
            {controllers.routes.DisplayItems.show(display.id.get, displayItem.id.get).absoluteURL()}
          </id>
          <link href={controllers.routes.DisplayItems.show(display.id.get, displayItem.id.get).absoluteURL()}></link>
          <updated>
            {dateFormat.print(lastUpdate)}
          </updated>
          <content type="html">{html}</content>
        </entry>, 0L)
    }.getOrElse((NodeSeq.Empty, 0L))
  }

  def getGaugePng(displayItemId: Long, projectId: Long, itemIdx: Int, etag: String) = Action {
    request =>
      DisplayItem.findById(displayItemId).map {
        displayItem =>
          request.headers.get(IF_NONE_MATCH).filter(_ == etag).map(_ => NotModified).getOrElse {
            val statusMonitors = StatusMonitor.finaAllForProject(projectId, Seq(StatusMonitorTypes.Sonar))
            val statusMonitorsWithValues = statusMonitors.map {
              statusMonitor =>
                (statusMonitor,
                  StatusValue.findAllForStatusMonitor(statusMonitor.id.get))
            }

            val config = displayItem.metricsConfig.getOrElse(MetricsConfig())
            val configItem = config.items(itemIdx)
            val width = (displayItem.width - 14) / displayItem.metricsConfig.flatMap(_.columns).getOrElse(1)
            val chart = configItem.itemType match {
              case MetricsItemTypes.Coverage =>
                createCoverageChart(statusMonitorsWithValues, displayItem.style, width, config, configItem)
              case MetricsItemTypes.ViolationsDetail =>
                createViolationDetailChart(statusMonitorsWithValues, displayItem.style, width, config, configItem)
            }
            val image = chart.createBufferedImage(width, width)
            val out = new ByteArrayOutputStream()

            ImageIO.write(image, "png", out)

            Ok(content = out.toByteArray).withHeaders(CONTENT_TYPE -> "image/png", ETAG -> etag)
          }
      }.getOrElse(NotFound)
  }

  private def getStatusMonitorsWithValues(projectId: Long): Seq[(StatusMonitor, Option[StatusValue])] = {
    val statusMonitors = StatusMonitor.finaAllForProject(projectId, Seq(StatusMonitorTypes.Sonar))
    statusMonitors.map {
      statusMonitor =>
        (statusMonitor,
          StatusValue.findLastForStatusMonitor(statusMonitor.id.get))
    }
  }

  private def calculateETag(displayItem: DisplayItem, projectId: Long,
                            statusMonitors: Seq[(StatusMonitor, Option[StatusValue])]): String = {

    val dataDigest = DataDigest()

    dataDigest.update(displayItem.id)
    dataDigest.update(displayItem.width)
    dataDigest.update(displayItem.height)
    dataDigest.update(displayItem.widgetConfigJson)
    dataDigest.update(projectId)
    statusMonitors.foreach {
      case (statusMonitor, statusValueOpt) =>
        dataDigest.update(statusMonitor.id)
        dataDigest.update(statusMonitor.keepHistory)
        dataDigest.update(statusMonitor.configJson)
        statusValueOpt.map {
          statusValue =>
            dataDigest.update(statusValue.id)
            dataDigest.update(statusValue.valuesJson)
        }
    }

    dataDigest.base64Digest()
  }

  private def createCoverageChart(statusMonitors: Seq[(StatusMonitor, Seq[StatusValue])],
                                  style: DisplayStyles.Type, width: Int,
                                  config: MetricsConfig, item: MetricsItem): JFreeChart = {
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
      plot.addLayer(new FillPointer(2, if (lastCoverage > warnAt) warnColor else warnHighlight))
      plot.addLayer(new FillPointer(0, if (lastCoverage > warnAt) okColor else warnColor))
    } else {
      plot.addLayer(new FillPointer(0, if (lastCoverage > warnAt) okHighlight else warnHighlight))
      plot.addLayer(new FillPointer(1, if (lastCoverage > warnAt) okColor else warnColor))
    }
    val valueIndicator = new CoverageDialValueIndicator(valueFont)
    plot.addLayer(valueIndicator)
    plot.setDataset(0, new DefaultValueDataset(lastCoverage))
    plot.setDataset(1, new DefaultValueDataset(minCoverage))
    plot.setDataset(2, new DefaultValueDataset(maxCoverage))
    plot.setBackgroundPaint(null)

    val chart = new JFreeChart(null, titleFont, plot, false)
    chart.setBackgroundPaint(null)
    chart
  }

  private def createViolationDetailChart(statusMonitors: Seq[(StatusMonitor, Seq[StatusValue])],
                                         style: DisplayStyles.Type, width: Int,
                                         config: MetricsConfig, item: MetricsItem): JFreeChart = {
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
    plot.addScale(0, new FilledScale(maxViolations, if (currentViolations > warnAt) warnColor else okColor))
    plot.addLayer(new FillPointer(0, if (currentViolations > warnAt) warnHighlight else warnColor))
    val valueIndicator = new ViolationsDialValueIndicator(valueFont)
    plot.addLayer(valueIndicator)
    plot.setDataset(dataSet)
    plot.setBackgroundPaint(null)

    val chart = new JFreeChart(null, titleFont, plot, false)
    chart.setBackgroundPaint(null)
    chart
  }

  private def atomLastUpdate(display: Display, displayItem: DisplayItem, html: Html): Long = {
    val key = "Metrics-%d-%d".format(display.id.get, displayItem.id.get)
    val etag = DataDigest.etag(html)
    var state = Cache.getOrElse(key) {
      AtomState(etag, System.currentTimeMillis())
    }
    if (state.etag != etag) {
      state = AtomState(etag, System.currentTimeMillis())
      Cache.set(key, state)
    }
    state.lastUpdate
  }

  private val okHighlight = Color.decode("#00FF00")

  private val okColor = Color.decode("#008800")

  private val warnHighlight = Color.decode("#FF0000")

  private val warnColor = Color.decode("#880000")
}
