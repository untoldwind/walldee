package actor.monitorProcessors

import org.specs2.mock.Mockito
import org.specs2.mutable._

import play.api.test.Helpers._
import actors.monitorProcessors.IcingaProcessor
import play.api.libs.ws.Response
import play.api.libs.json.Json
import play.api.test.FakeApplication
import models.{StatusTypes, StatusValue, StatusMonitorTypes, StatusMonitor}

class IcingaProcessorSpec extends Specification with Mockito {
  "Icinga processor" should {
    "should not encode direct json url" in {
      val url = "https://icinga/cgi-bin/icinga/status.cgi?hostgroup=hosts&style=overview&nostatusheader&jsonoutput"

      IcingaProcessor.apiUrl(url) must be_==(url)
    }

    "should add jsonOutput to url" in {
      val url = "https://icinga/cgi-bin/icinga/status.cgi?hostgroup=hosts&style=overview&nostatusheader"

      IcingaProcessor.apiUrl(url) must be_==(url + "&jsonoutput")
    }

    "process json correctly" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val statusMonitor =
          StatusMonitor(Some(1), "Monitor", StatusMonitorTypes.Icinga.id, "http://localhost", None, None, true, 10, 60, None, None)

        statusMonitor.insert

        val response = sucessfulJobResponse

        IcingaProcessor.process(statusMonitor, response)

        val statusValues = StatusValue.findAllForStatusMonitor(1)
        statusValues must have size (1)
        statusValues(0).status must be_==(StatusTypes.Failure)
      }
    }
  }

  private def sucessfulJobResponse: Response = {
    val response = mock[Response]

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

    response.status returns OK
    response.body returns body
    response.json returns Json.parse(body)
    response
  }
}