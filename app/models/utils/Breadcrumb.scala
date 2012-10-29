package models.utils

import play.api.mvc.Call

case class Breadcrumb(name:String, link:Call, current:Boolean)
