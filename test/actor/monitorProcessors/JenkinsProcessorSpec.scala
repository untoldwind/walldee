package actor.monitorProcessors

import org.specs2.mock.Mockito
import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import models._
import actors.monitorProcessors.JenkinsProcessor
import play.api.libs.ws.Response
import play.api.libs.json.Json
import play.api.libs.ws.Response
import play.api.test.FakeApplication
import scala.Some
import models.statusValues.ResponseInfo

class JenkinsProcessorSpec extends Specification with Mockito {
  "Jenkins processor" should {
    "not encode direct json url" in {
      val url = "http://localhost/jobs/bla/123/api/json"
      val statusMonitor = mock[StatusMonitor]

      statusMonitor.url returns url
      new JenkinsProcessor(statusMonitor).apiUrl must be_==(url)
    }

    "add api/json to url with /" in {
      val url = "http://localhost/jobs/bla/123/"
      val statusMonitor = mock[StatusMonitor]

      statusMonitor.url returns url
      new JenkinsProcessor(statusMonitor).apiUrl must be_==(url + "api/json")
    }

    "add /api/json to url without /" in {
      val url = "http://localhost/jobs/bla/123"
      val statusMonitor = mock[StatusMonitor]

      statusMonitor.url returns url
      new JenkinsProcessor(statusMonitor).apiUrl must be_==(url + "/api/json")
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
            typeNum = StatusMonitorTypes.Jenkins.id,
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

        new JenkinsProcessor(statusMonitor).process(response)

        val statusValues = StatusValue.findAllForStatusMonitor(1)
        statusValues must have size (1)
        statusValues(0).status must be_==(StatusTypes.Ok)
      }
    }
  }

  private def sucessfulJobResponse: ResponseInfo = {
    val body =
      """{"actions":[],"description":"","displayName":"ruboto_rubies","displayNameOrNull":null,""" +
        """"name":"ruboto_rubies","url":"http://ci.jruby.org/job/ruboto_rubies/","buildable":true,""" +
        """"builds":[{"number":37,"url":"http://ci.jruby.org/job/ruboto_rubies/37/"},""" +
        """{"number":36,"url":"http://ci.jruby.org/job/ruboto_rubies/36/"},""" +
        """{"number":35,"url":"http://ci.jruby.org/job/ruboto_rubies/35/"},""" +
        """{"number":34,"url":"http://ci.jruby.org/job/ruboto_rubies/34/"},""" +
        """{"number":33,"url":"http://ci.jruby.org/job/ruboto_rubies/33/"},""" +
        """{"number":32,"url":"http://ci.jruby.org/job/ruboto_rubies/32/"},""" +
        """{"number":31,"url":"http://ci.jruby.org/job/ruboto_rubies/31/"},""" +
        """{"number":30,"url":"http://ci.jruby.org/job/ruboto_rubies/30/"},""" +
        """{"number":29,"url":"http://ci.jruby.org/job/ruboto_rubies/29/"},""" +
        """{"number":28,"url":"http://ci.jruby.org/job/ruboto_rubies/28/"}],"color":"blue",""" +
        """"firstBuild":{"number":10,"url":"http://ci.jruby.org/job/ruboto_rubies/10/"},""" +
        """"healthReport":[{"description":"Build-Stabilität: 1 der letzten 5 Builds schlug fehl.","iconUrl":"health-60to79.png","score":80}],""" +
        """"inQueue":false,"keepDependencies":false,""" +
        """"lastBuild":{"number":37,"url":"http://ci.jruby.org/job/ruboto_rubies/37/"},""" +
        """"lastCompletedBuild":{"number":37,"url":"http://ci.jruby.org/job/ruboto_rubies/37/"},""" +
        """"lastFailedBuild":{"number":35,"url":"http://ci.jruby.org/job/ruboto_rubies/35/"},""" +
        """"lastStableBuild":{"number":37,"url":"http://ci.jruby.org/job/ruboto_rubies/37/"},""" +
        """"lastSuccessfulBuild":{"number":37,"url":"http://ci.jruby.org/job/ruboto_rubies/37/"},""" +
        """"lastUnstableBuild":null,"lastUnsuccessfulBuild":{"number":35,"url":"http://ci.jruby.org/job/ruboto_rubies/35/"},""" +
        """"nextBuildNumber":38,"property":[],"queueItem":null,"concurrentBuild":false,""" +
        """"downstreamProjects":[{"name":"ruboto_update","url":"http://ci.jruby.org/job/ruboto_update/","color":"blue"}],""" +
        """"scm":{},"upstreamProjects":[{"name":"ruboto_standalone","url":"http://ci.jruby.org/job/ruboto_standalone/","color":"red_anime"}],""" +
        """"activeConfigurations":[""" +
        """{"name":"ANDROID_OS=android-15,ANDROID_TARGET=android-15,RUBOTO_PLATFORM=CURRENT,RUBY_IMPL=ruby-1.8.7,label=ruboto",""" +
        """"url":"http://ci.jruby.org/job/ruboto_rubies/./ANDROID_OS=android-15,ANDROID_TARGET=android-15,RUBOTO_PLATFORM=CURRENT,RUBY_IMPL=ruby-1.8.7,label=ruboto/","color":"blue"},""" +
        """{"name":"ANDROID_OS=android-15,ANDROID_TARGET=android-15,RUBOTO_PLATFORM=CURRENT,RUBY_IMPL=ruby-1.9.3,label=ruboto",""" +
        """"url":"http://ci.jruby.org/job/ruboto_rubies/./ANDROID_OS=android-15,ANDROID_TARGET=android-15,RUBOTO_PLATFORM=CURRENT,RUBY_IMPL=ruby-1.9.3,label=ruboto/","color":"blue"},""" +
        """{"name":"ANDROID_OS=android-15,ANDROID_TARGET=android-15,RUBOTO_PLATFORM=CURRENT,RUBY_IMPL=jruby,label=ruboto",""" +
        """"url":"http://ci.jruby.org/job/ruboto_rubies/./ANDROID_OS=android-15,ANDROID_TARGET=android-15,RUBOTO_PLATFORM=CURRENT,RUBY_IMPL=jruby,label=ruboto/","color":"blue"},""" +
        """{"name":"ANDROID_OS=android-15,ANDROID_TARGET=android-15,RUBOTO_PLATFORM=CURRENT,RUBY_IMPL=rbx,label=ruboto",""" +
        """"url":"http://ci.jruby.org/job/ruboto_rubies/./ANDROID_OS=android-15,ANDROID_TARGET=android-15,RUBOTO_PLATFORM=CURRENT,RUBY_IMPL=rbx,label=ruboto/","color":"blue"}]}"""

    ResponseInfo(
      statusCode = OK,
      statusText = "OK",
      headers = Seq.empty,
      body = body
    )
  }
}
