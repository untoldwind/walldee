package actor.monitorProcessors

import org.specs2.mock.Mockito
import org.specs2.mutable._

import play.api.test.Helpers._
import scala.xml.XML
import actors.monitorProcessors.FreestyleProcessor
import models._
import models.statusMonitors.{FreestyleTypes, FreestyleConfig}
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.Response
import play.api.test.FakeApplication
import scala.Some
import models.statusValues.{ResponseInfo, FreestyleStatus}


class FreestyleProcessorXmlSpec extends Specification with Mockito {
  "FreestyleProcessor XML" should {
    "leave urls unchanged" in {
      val url = "some nonsense"
      val statusMonitor = mock[StatusMonitor]

      statusMonitor.url returns url
      new FreestyleProcessor(statusMonitor).apiUrl must be_==(url)
    }

    "process xml correctly" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val project = Project(Some(1), "Project")

        project.insert

        val freestyleConfig = FreestyleConfig(freestyleType = FreestyleTypes.Xml)
        val statusMonitor =
          StatusMonitor(
            id = Some(1),
            projectId = 1,
            name = "Monitor",
            typeNum = StatusMonitorTypes.Freestyle.id,
            url = "http://localhost",
            username = None,
            password = None,
            active = true,
            keepHistory = 10,
            updatePeriod = 60,
            lastQueried = None,
            lastUpdated = None,
            configJson = Some(Json.stringify(Json.toJson(freestyleConfig)))
          )

        statusMonitor.insert

        val response = sucessfulJobResponse
        val processor = new FreestyleProcessor(statusMonitor)
        val (status, json) = processor.process(response)
        processor.updateStatus(status, json)

        val statusValues = StatusValue.findAllForStatusMonitor(1)
        statusValues must have size (1)

        val expectedJson = """{
                             |    "nutrition": {
                             |        "daily-values": {
                             |            "carb": {
                             |                "@units": "g",
                             |                "_text": 300
                             |            },
                             |            "cholesterol": {
                             |                "@units": "mg",
                             |                "_text": 300
                             |            },
                             |            "fiber": {
                             |                "@units": "g",
                             |                "_text": 25
                             |            },
                             |            "protein": {
                             |                "@units": "g",
                             |                "_text": 50
                             |            },
                             |            "saturated-fat": {
                             |                "@units": "g",
                             |                "_text": 20
                             |            },
                             |            "sodium": {
                             |                "@units": "mg",
                             |                "_text": 2400
                             |            },
                             |            "total-fat": {
                             |                "@units": "g",
                             |                "_text": 65
                             |            }
                             |        },
                             |        "food": [
                             |            {
                             |                "calories": {
                             |                    "@fat": 100,
                             |                    "@total": 110
                             |                },
                             |                "carb": 2,
                             |                "cholesterol": 5,
                             |                "fiber": 0,
                             |                "mfr": "Sunnydale",
                             |                "minerals": {
                             |                    "ca": 0,
                             |                    "fe": 0
                             |                },
                             |                "name": "Avocado Dip",
                             |                "protein": 1,
                             |                "saturated-fat": 3,
                             |                "serving": {
                             |                    "@units": "g",
                             |                    "_text": 29
                             |                },
                             |                "sodium": 210,
                             |                "total-fat": 11,
                             |                "vitamins": {
                             |                    "a": 0,
                             |                    "c": 0
                             |                }
                             |            },
                             |            {
                             |                "calories": {
                             |                    "@fat": 35,
                             |                    "@total": 300
                             |                },
                             |                "carb": 54,
                             |                "cholesterol": 0,
                             |                "fiber": 3,
                             |                "mfr": "Thompson",
                             |                "minerals": {
                             |                    "ca": 8,
                             |                    "fe": 20
                             |                },
                             |                "name": "Bagels, New York Style",
                             |                "protein": 11,
                             |                "saturated-fat": 1,
                             |                "serving": {
                             |                    "@units": "g",
                             |                    "_text": 104
                             |                },
                             |                "sodium": 510,
                             |                "total-fat": 4,
                             |                "vitamins": {
                             |                    "a": 0,
                             |                    "c": 0
                             |                }
                             |            }
                             |        ]
                             |    }
                             |}""".stripMargin


        statusValues(0).status must be(StatusTypes.Ok)
        statusValues(0).freestyleStatus must be_==(Some(FreestyleStatus(Json.parse(expectedJson).asOpt[JsObject])))
      }
    }

    "use xpath selector" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val project = Project(Some(1), "Project")

        project.insert

        val freestyleConfig = FreestyleConfig(freestyleType = FreestyleTypes.Xml, selector = Some("nutrition/food/minerals"))
        val statusMonitor =
          StatusMonitor(
            id = Some(1),
            projectId = 1,
            name = "Monitor",
            typeNum = StatusMonitorTypes.Freestyle.id,
            url = "http://localhost",
            username = None,
            password = None,
            active = true,
            keepHistory = 10,
            updatePeriod = 60,
            lastQueried = None,
            lastUpdated = None,
            configJson = Some(Json.stringify(Json.toJson(freestyleConfig)))
          )

        statusMonitor.insert

        val response = sucessfulJobResponse
        val processor = new FreestyleProcessor(statusMonitor)
        val (status, json) = processor.process(response)
        processor.updateStatus(status, json)

        val statusValues = StatusValue.findAllForStatusMonitor(1)
        statusValues must have size (1)

        val expectedJson = """{"minerals":[{"ca":0,"fe":0},{"ca":8,"fe":20}]}"""
        statusValues(0).status must be(StatusTypes.Ok)
        statusValues(0).freestyleStatus must be_==(Some(FreestyleStatus(Json.parse(expectedJson).asOpt[JsObject])))

      }
    }

    "fail if selector has empty result" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val project = Project(Some(1), "Project")

        project.insert

        val freestyleConfig = FreestyleConfig(freestyleType = FreestyleTypes.Xml, selector = Some("nutrition/something"))
        val statusMonitor =
          StatusMonitor(
            id = Some(1),
            projectId = 1,
            name = "Monitor",
            typeNum = StatusMonitorTypes.Freestyle.id,
            url = "http://localhost",
            username = None,
            password = None,
            active = true,
            keepHistory = 10,
            updatePeriod = 60,
            lastQueried = None,
            lastUpdated = None,
            configJson = Some(Json.stringify(Json.toJson(freestyleConfig)))
          )

        statusMonitor.insert

        val response = sucessfulJobResponse
        val processor = new FreestyleProcessor(statusMonitor)
        val (status, json) = processor.process(response)
        processor.updateStatus(status, json)

        val statusValues = StatusValue.findAllForStatusMonitor(1)
        statusValues must have size (1)
        statusValues(0).status must be(StatusTypes.Failure)
      }
    }

    "fail if response is not XML" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val project = Project(Some(1), "Project")

        project.insert

        val freestyleConfig = FreestyleConfig(freestyleType = FreestyleTypes.Xml)
        val statusMonitor =
          StatusMonitor(
            id = Some(1),
            projectId = 1,
            name = "Monitor",
            typeNum = StatusMonitorTypes.Freestyle.id,
            url = "http://localhost",
            username = None,
            password = None,
            active = true,
            keepHistory = 10,
            updatePeriod = 60,
            lastQueried = None,
            lastUpdated = None,
            configJson = Some(Json.stringify(Json.toJson(freestyleConfig)))
          )

        statusMonitor.insert

        val response = sucessfulInvalidResponse
        val processor = new FreestyleProcessor(statusMonitor)
        val (status, json) = processor.process(response)
        processor.updateStatus(status, json)

        val statusValues = StatusValue.findAllForStatusMonitor(1)
        statusValues must have size (1)
        statusValues(0).status must be(StatusTypes.Failure)
      }
    }
  }

  private def sucessfulJobResponse: ResponseInfo = {
    val body = """<?xml version="1.0"?>
                 |<?xml-stylesheet type="text/css" href="nutrition.css"?>
                 |<nutrition>
                 |
                 |<daily-values>
                 |	<total-fat units="g">65</total-fat>
                 |	<saturated-fat units="g">20</saturated-fat>
                 |	<cholesterol units="mg">300</cholesterol>
                 |	<sodium units="mg">2400</sodium>
                 |	<carb units="g">300</carb>
                 |	<fiber units="g">25</fiber>
                 |	<protein units="g">50</protein>
                 |</daily-values>
                 |
                 |<food>
                 |	<name>Avocado Dip</name>
                 |	<mfr>Sunnydale</mfr>
                 |	<serving units="g">29</serving>
                 |	<calories total="110" fat="100"/>
                 |	<total-fat>11</total-fat>
                 |	<saturated-fat>3</saturated-fat>
                 |	<cholesterol>5</cholesterol>
                 |	<sodium>210</sodium>
                 |	<carb>2</carb>
                 |	<fiber>0</fiber>
                 |	<protein>1</protein>
                 |	<vitamins>
                 |		<a>0</a>
                 |		<c>0</c>
                 |	</vitamins>
                 |	<minerals>
                 |		<ca>0</ca>
                 |		<fe>0</fe>
                 |	</minerals>
                 |</food>
                 |
                 |<food>
                 |	<name>Bagels, New York Style </name>
                 |	<mfr>Thompson</mfr>
                 |	<serving units="g">104</serving>
                 |	<calories total="300" fat="35"/>
                 |	<total-fat>4</total-fat>
                 |	<saturated-fat>1</saturated-fat>
                 |	<cholesterol>0</cholesterol>
                 |	<sodium>510</sodium>
                 |	<carb>54</carb>
                 |	<fiber>3</fiber>
                 |	<protein>11</protein>
                 |	<vitamins>
                 |		<a>0</a>
                 |		<c>0</c>
                 |	</vitamins>
                 |	<minerals>
                 |		<ca>8</ca>
                 |		<fe>20</fe>
                 |	</minerals>
                 |</food>
                 |</nutrition>
                 | """.stripMargin

    ResponseInfo(
      statusCode = OK,
      statusText = "OK",
      headers = Seq.empty,
      body = body
    )
  }

  private def sucessfulInvalidResponse: ResponseInfo = {
    val body = """Something>not even remotely<XML"""

    ResponseInfo(
      statusCode = OK,
      statusText = "OK",
      headers = Seq.empty,
      body = body
    )
  }
}
