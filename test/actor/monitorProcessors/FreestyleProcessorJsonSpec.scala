package actor.monitorProcessors

import play.api.test.Helpers._
import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import actors.monitorProcessors.FreestyleProcessor
import play.api.libs.ws.Response
import play.api.libs.json.{Json, JsObject}
import play.api.libs.ws.Response
import play.api.test.FakeApplication
import models._
import models.statusMonitors.{FreestyleTypes, FreestyleConfig}
import play.api.libs.ws.Response
import play.api.test.FakeApplication
import models.statusValues.{RequestSuccess, FreestyleStatus}

class FreestyleProcessorJsonSpec extends Specification with Mockito {
  "FreestyleProcessor JSON" should {
    "leave urls unchanged" in {
      val url = "some nonsense"
      val statusMonitor = mock[StatusMonitor]

      statusMonitor.url returns url
      new FreestyleProcessor(statusMonitor).apiUrl must be_==(url)
    }

    "process json correctly" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val project = Project(Some(1), "Project")

        project.insert

        val freestyleConfig = FreestyleConfig(freestyleType = FreestyleTypes.Json)
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
  }

  "use simple selector" in {
    running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
      val project = Project(Some(1), "Project")

      project.insert

      val freestyleConfig = FreestyleConfig(freestyleType = FreestyleTypes.Json, selector = Some("nutrition.food[0].minerals"))
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

      val expectedJson = """{"ca":0,"fe":0}"""
      statusValues(0).status must be(StatusTypes.Ok)
      statusValues(0).freestyleStatus must be_==(Some(FreestyleStatus(Json.parse(expectedJson).asOpt[JsObject])))

    }
  }

  "fail if selector has empty result" in {
    running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
      val project = Project(Some(1), "Project")

      project.insert

      val freestyleConfig = FreestyleConfig(freestyleType = FreestyleTypes.Json, selector = Some("nutrition/something"))
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

    "fail if response is not JSON" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val project = Project(Some(1), "Project")

        project.insert

        val freestyleConfig = FreestyleConfig(freestyleType = FreestyleTypes.Json)
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

  private def sucessfulJobResponse: RequestSuccess = {
    val body = """{
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

    RequestSuccess(
      statusCode = OK,
      statusText = "OK",
      headers = Seq.empty,
      body = body
    )
  }

  private def sucessfulInvalidResponse: RequestSuccess = {

    val body = """Something>not even remotely<JSON{}{}"""

    RequestSuccess(
      statusCode = OK,
      statusText = "OK",
      headers = Seq.empty,
      body = body
    )
  }
}
