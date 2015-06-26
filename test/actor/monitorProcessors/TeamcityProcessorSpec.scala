package actor.monitorProcessors

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import actors.monitorProcessors.{TeamcityProcessor, JenkinsProcessor}
import play.api.libs.ws.Response
import play.api.libs.json.Json
import play.api.test._
import play.api.test.Helpers._
import models._

class TeamcityProcessorSpec extends Specification with Mockito {
  "Teamcity processer" should {
    "convert url" in {
      val url = "http://teamcity.somewhere.de/viewType.html?buildTypeId=bt40&tab=buildTypeStatusDiv"

      TeamcityProcessor.apiUrl(url) must be_==("http://teamcity.somewhere.de/httpAuth/app/rest/builds/buildType:bt40,running:any")
    }

    "convert a new url" in {
      val url = "http://teamcity.somewhere.de/viewType.html?buildTypeId=EPostSafeUi_45LivingDocumentationApiUploadToConfluence&tab=buildTypeStatusDiv"

      TeamcityProcessor.apiUrl(url) must be_==("http://teamcity.somewhere.de/httpAuth/app/rest/builds/buildType:EPostSafeUi_45LivingDocumentationApiUploadToConfluence,running:any")
    }
    
    "convert a url with dashes in the host name" in {
      val url = "http://teamcity.some-where.de/viewType.html?buildTypeId=EPostSafeUi_45LivingDocumentationApiUploadToConfluence&tab=buildTypeStatusDiv"

      TeamcityProcessor.apiUrl(url) must be_==("http://teamcity.some-where.de/httpAuth/app/rest/builds/buildType:EPostSafeUi_45LivingDocumentationApiUploadToConfluence,running:any")
    }

    "process json correctly" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val project = Project(Some(1), "Project")

        project.insert

        val statusMonitor =
          StatusMonitor(
            id = Some(1),
            projectId = 1,
            name = "Ci",
            typeNum = StatusMonitorTypes.Teamcity.id,
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

        TeamcityProcessor.process(statusMonitor, response)

        val statusValues = StatusValue.findAllForStatusMonitor(1)
        statusValues must have size (1)
        statusValues(0).status must be_==(StatusTypes.Ok)
      }
    }
  }

  private def sucessfulJobResponse: Response = {
    val response = mock[Response]

    val body =
      """{"id":4756,"number":"187","status":"SUCCESS","href":"/httpAuth/app/rest/builds/id:4756",""" +
        """"webUrl":"http://teamcity.somewhere.de/viewLog.html?buildId=4756&buildTypeId=bt40",""" +
        """"personal":false,"history":false,"pinned":false,"statusText":"Success",""" +
        """"buildType":{"id":"bt40","name":"1. Build tested War",""" +
        """"href":"/httpAuth/app/rest/buildTypes/id:bt40","projectName":"Some Project",""" +
        """"projectId":"project11","webUrl":"http://teamcity.somewhere.de/viewType.html?buildTypeId=bt40"},""" +
        """"startDate":"20121120T091111+0100","finishDate":"20121120T091227+0100",""" +
        """"agent":{"href":"/httpAuth/app/rest/agents/id:5","id":5,"name":"Agent 3"},""" +
        """"tags":null,"properties":null,"snapshot-dependencies":[null],""" +
        """"artifact-dependencies":[null],""" +
        """"revisions":{"revision":[{"version":"210446","vcs-root-instance":""" +
        """{"id":"49","vcs-root-id":"26","name":"Some Project (Team-Branch)",""" +
        """"href":"/httpAuth/app/rest/vcs-roots/id:26/instances/id:49"}}]},""" +
        """"triggered":{"type":"vcs","details":"svn","date":"20121119T135610+0100"},""" +
        """"changes":{"count":1,"href":"/httpAuth/app/rest/changes?build=id:4756"}}"""

    response.status returns OK
    response.body returns body
    response.json returns Json.parse(body)
    response
  }
}
