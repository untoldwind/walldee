package models.statusValues

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import play.api.libs.json.{JsObject, Json}

class FreestyleStatusSpec extends Specification with Mockito {
  "FreestyleStatus" should {
    "convert json to map" in {
      val expected = Seq(
        "nutrition.daily-values.carb.@units" -> "g",
        "nutrition.daily-values.carb._text" -> 300,
        "nutrition.daily-values.cholesterol.@units" -> "mg",
        "nutrition.daily-values.cholesterol._text" -> 300,
        "nutrition.daily-values.fiber.@units" -> "g",
        "nutrition.daily-values.fiber._text" -> 25,
        "nutrition.daily-values.protein.@units" -> "g",
        "nutrition.daily-values.protein._text" -> 50,
        "nutrition.daily-values.saturated-fat.@units" -> "g",
        "nutrition.daily-values.saturated-fat._text" -> 20,
        "nutrition.daily-values.sodium.@units" -> "mg",
        "nutrition.daily-values.sodium._text" -> 2400,
        "nutrition.daily-values.total-fat.@units" -> "g",
        "nutrition.daily-values.total-fat._text" -> 65,
        "nutrition.food[0].calories.@fat" -> 100,
        "nutrition.food[0].calories.@total" -> 110,
        "nutrition.food[0].carb" -> 2,
        "nutrition.food[0].cholesterol" -> 5,
        "nutrition.food[0].fiber" -> 0,
        "nutrition.food[0].mfr" -> "Sunnydale",
        "nutrition.food[0].minerals.ca" -> 0,
        "nutrition.food[0].minerals.fe" -> 0,
        "nutrition.food[0].name" -> "Avocado Dip",
        "nutrition.food[0].protein" -> 1,
        "nutrition.food[0].saturated-fat" -> 3,
        "nutrition.food[0].serving.@units" -> "g",
        "nutrition.food[0].serving._text" -> 29,
        "nutrition.food[0].sodium" -> 210,
        "nutrition.food[0].total-fat" -> 11,
        "nutrition.food[0].vitamins.a" -> 0,
        "nutrition.food[0].vitamins.c" -> 0,
        "nutrition.food[1].calories.@fat" -> 35,
        "nutrition.food[1].calories.@total" -> 300,
        "nutrition.food[1].carb" -> 54,
        "nutrition.food[1].cholesterol" -> 0,
        "nutrition.food[1].fiber" -> 3,
        "nutrition.food[1].mfr" -> "Thompson",
        "nutrition.food[1].minerals.ca" -> 8,
        "nutrition.food[1].minerals.fe" -> 20,
        "nutrition.food[1].name" -> "Bagels, New York Style",
        "nutrition.food[1].protein" -> 11,
        "nutrition.food[1].saturated-fat" -> 1,
        "nutrition.food[1].serving.@units" -> "g",
        "nutrition.food[1].serving._text" -> 104,
        "nutrition.food[1].sodium" -> 510,
        "nutrition.food[1].total-fat" -> 4,
        "nutrition.food[1].vitamins.a" -> 0,
        "nutrition.food[1].vitamins.c" -> 0
      ).toMap
      freestyleStatus.values must be_==(expected)
    }
  }

  val json = """{
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

  val freestyleStatus = FreestyleStatus(Json.parse(json).asOpt[JsObject])
}
