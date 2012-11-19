package actor.monitorProcessors

import org.specs2.mock.Mockito
import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import models._
import actors.monitorProcessors.{SonarProcessor, JenkinsProcessor}
import play.api.libs.ws.Response
import play.api.libs.json.Json
import play.api.libs.ws.Response
import play.api.test.FakeApplication
import scala.Some

class SonarProcessorSpec extends Specification with Mockito {
  "Sonar processor" should {
    "should encode http url" in {
      val url = "http://nemo.sonarsource.org/dashboard/index/427172?did=1"

      SonarProcessor.apiUrl(url) must be_==("http://nemo.sonarsource.org/api/resources?resource=427172&metrics=coverage,violations,blocker_violations,critical_violations,major_violations,minor_violations,info_violations&format=json")
    }

    "should encode https url" in {
      var url = "https://sonar.somewhere.de/sonar/dashboard/index/50647"

      SonarProcessor.apiUrl(url) must be_==("https://sonar.somewhere.de/sonar/api/resources?resource=50647&metrics=coverage,violations,blocker_violations,critical_violations,major_violations,minor_violations,info_violations&format=json")
    }

    "process json correctly" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val project = Project(Some(1), "Project")

        project.insert

        val statusMonitor =
          StatusMonitor(Some(1), 1, "Ci", StatusMonitorTypes.Sonar.id, "http://localhost", None, None, true, 10, 60, None, None)

        statusMonitor.insert

        val response = sucessfulJobResponse

        SonarProcessor.process(statusMonitor, response)

        val statusValues = StatusValue.findAllForStatusMonitor(1)
        statusValues must have size (1)
        statusValues(0).status must be_==(StatusTypes.Ok)
      }
    }
  }

  private def sucessfulJobResponse: Response = {
    val response = mock[Response]

    val body = """[{"id":427172,"key":"org.codehaus.sonar-plugins.java:java",""" +
      """"name":"Sonar Java","scope":"PRJ","qualifier":"TRK","date":"2012-11-18T02:46:54+0000",""" +
      """"lname":"Sonar Java","lang":"java","version":"1.1-SNAPSHOT","description":"",""" +
      """"msr":[{"key":"coverage","val":78.5,"frmt_val":"78,5%"},""" +
      """{"key":"violations","val":171.0,"frmt_val":"171"},""" +
      """{"key":"blocker_violations","val":0.0,"frmt_val":"0"},""" +
      """{"key":"critical_violations","val":0.0,"frmt_val":"0"},""" +
      """{"key":"major_violations","val":93.0,"frmt_val":"93"},""" +
      """{"key":"minor_violations","val":16.0,"frmt_val":"16"},""" +
      """{"key":"info_violations","val":62.0,"frmt_val":"62"}]}]"""

    response.status returns OK
    response.body returns body
    response.json returns Json.parse(body)
    response
  }
}
