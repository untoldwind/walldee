package models.display

import play.api.libs.json.{JsResult, JsValue, Format}

case class AnimationConfig(animations: Seq[Animation]) {

}

object AnimationConfig {

  implicit object AnimationConfigFormat extends Format[AnimationConfig] {
    def reads(json: JsValue): JsResult[AnimationConfig] = null

    def writes(animationConfig: AnimationConfig): JsValue = null
  }

}