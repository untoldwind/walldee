package actor.monitorProcessors

import org.specs2.mock.Mockito
import org.specs2.mutable._

import play.api.test.Helpers._
import actors.monitorProcessors.IcingaProcessor
import play.api.libs.json.Json
import models._
import models.statusValues._
import play.api.libs.ws.Response
import scala.Some
import play.api.test.FakeApplication
import models.statusMonitors.IcingaConfig

class IcingaProcessorSpec extends Specification with Mockito {
  "Icinga processor" should {
    "should not encode direct json url" in {
      val url = "https://icinga/cgi-bin/icinga/status.cgi?hostgroup=hosts&style=overview&nostatusheader&jsonoutput"
      val statusMonitor = mock[StatusMonitor]

      statusMonitor.url returns url
      new IcingaProcessor(statusMonitor).apiUrl must be_==(url)
    }

    "should add jsonOutput to url" in {
      val url = "https://icinga/cgi-bin/icinga/status.cgi?hostgroup=hosts&style=overview&nostatusheader"
      val statusMonitor = mock[StatusMonitor]

      statusMonitor.url returns url
      new IcingaProcessor(statusMonitor).apiUrl must be_==(url + "&jsonoutput")
    }

    "process json correctly" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val project = Project(Some(1), "Project")

        project.insert

        val statusMonitor =
          StatusMonitor(
            id = Some(1),
            projectId = 1,
            name = "Monitor",
            typeNum = StatusMonitorTypes.Icinga.id,
            url = "http://localhost",
            username = None,
            password = None,
            active = true,
            keepHistory = 10,
            updatePeriod = 60,
            lastQueried = None,
            lastUpdated = None,
            configJson = None
          )

        statusMonitor.insert

        val response = sucessfulJobResponse
        val processor = new IcingaProcessor(statusMonitor)
        val (status, json) = processor.process(response)
        processor.updateStatus(status, json)

        val statusValues = StatusValue.findAllForStatusMonitor(1)
        statusValues must have size (1)
        statusValues(0).status must be_==(StatusTypes.Failure)
        statusValues(0).hostsStatus must be_==(Some(
          HostsStatus(Seq(HostsGroup(Seq(HostStatus("host1", HostStatusTypes.Up, HostServiceStatusTypes.Critical),
            HostStatus("host2", HostStatusTypes.Up, HostServiceStatusTypes.Ok),
            HostStatus("host3", HostStatusTypes.Up, HostServiceStatusTypes.Critical),
            HostStatus("host4", HostStatusTypes.Up, HostServiceStatusTypes.Ok),
            HostStatus("host5", HostStatusTypes.Up, HostServiceStatusTypes.Ok)))))))
      }
    }

    "apply the hostname filter" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val project = Project(Some(1), "Project")

        project.insert

        val icingaConfig = IcingaConfig(hostNameFilter = Some( """host[45]""".r))
        val statusMonitor =
          StatusMonitor(
            id = Some(1),
            projectId = 1,
            name = "Monitor",
            typeNum = StatusMonitorTypes.Icinga.id,
            url = "http://localhost",
            username = None,
            password = None,
            active = true,
            keepHistory = 10,
            updatePeriod = 60,
            lastQueried = None,
            lastUpdated = None,
            configJson = Some(Json.stringify(Json.toJson(icingaConfig)))
          )

        statusMonitor.insert

        val response = sucessfulJobResponse
        val processor = new IcingaProcessor(statusMonitor)
        val (status, json) = processor.process(response)
        processor.updateStatus(status, json)

        val statusValues = StatusValue.findAllForStatusMonitor(1)
        statusValues must have size (1)
        statusValues(0).status must be_==(StatusTypes.Ok)
        statusValues(0).hostsStatus must be_==(Some(
          HostsStatus(Seq(HostsGroup(Seq(
            HostStatus("host4", HostStatusTypes.Up, HostServiceStatusTypes.Ok),
            HostStatus("host5", HostStatusTypes.Up, HostServiceStatusTypes.Ok)))))))
      }
    }
  }

  private def sucessfulJobResponse: RequestSuccess = {
    val body = """{ "cgi_json_version": "1.7.1",
                 |"status": {
                 |"hostgroup_overview": [
                 |{ "hostgroup_name": "hosts",
                 |"members": [
                 |{ "host_name": "host1", "host_status": "UP", "services_status_ok": 10, "services_status_warning": 0, "services_status_unknown": 1, "services_status_critical": 3, "services_status_pending": 0 }
                 |,
                 |{ "host_name": "host2", "host_status": "UP", "services_status_ok": 11, "services_status_warning": 0, "services_status_unknown": 0, "services_status_critical": 0, "services_status_pending": 0 }
                 |,
                 |{ "host_name": "host3", "host_status": "UP", "services_status_ok": 23, "services_status_warning": 1, "services_status_unknown": 0, "services_status_critical": 4, "services_status_pending": 0 }
                 |,
                 |{ "host_name": "host4", "host_status": "UP", "services_status_ok": 10, "services_status_warning": 0, "services_status_unknown": 0, "services_status_critical": 0, "services_status_pending": 0 }
                 |,
                 |{ "host_name": "host5", "host_status": "UP", "services_status_ok": 10, "services_status_warning": 0, "services_status_unknown": 0, "services_status_critical": 0, "services_status_pending": 0 }
                 | ] }
                 | ]
                 |}
                 |}""".stripMargin

    RequestSuccess(
      statusCode = OK,
      statusText = "OK",
      headers = Seq.empty,
      body = body
    )
  }
}