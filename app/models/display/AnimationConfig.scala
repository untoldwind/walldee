package models.display

import play.api.libs.json.{JsValue, Format}

case class AnimationConfig(animations: Seq[Animation]) {

}

object AnimationConfig {

  implicit object AnimationConfigFormat extends Format[AnimationConfig] {
    def reads(json: JsValue): AnimationConfig = null

    def writes(animationConfig: AnimationConfig): JsValue = null
  }

}