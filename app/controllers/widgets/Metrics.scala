package controllers.widgets

import play.api.Play.current
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
import xml.NodeSeq
import play.api.cache.Cache
import org.joda.time.format.ISODateTimeFormat
import collection.immutable.SortedSet
import charts.metrics._
import scala.Some
import charts.Chart

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
    "severities" -> severityMapping,
    "showTrend" -> optional(boolean)
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
          <content type="html">
            {html}
          </content>
        </entry>, 0L)
    }.getOrElse((NodeSeq.Empty, 0L))
  }

  def getGaugePng(displayItemId: Long, projectId: Long, itemIdx: Int, etag: String) = Action {
    request =>
      DisplayItem.findById(displayItemId).map {
        displayItem =>
          request.headers.get(IF_NONE_MATCH).filter(_ == etag + "s").map(_ => NotModified).getOrElse {
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
                new CoverageGauge(statusMonitorsWithValues, displayItem.style, width, config, configItem)
              case MetricsItemTypes.ViolationsDetail =>
              new ViolationsGauge(statusMonitorsWithValues, displayItem.style, width, config, configItem)
            }

            Ok(content = chart.toPng).withHeaders(CONTENT_TYPE -> "image/png", ETAG -> etag)
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
