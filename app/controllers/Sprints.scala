package controllers

import play.api.mvc.{Action, Controller}
import models.Sprint
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._

object Sprints extends Controller {
  val sprintForm = Form(
    tuple(
      "title" -> text(maxLength = 255),
      "num" -> number(min = 1, max = 10000)
    )
  )

  def index = Action {
    Ok(views.html.sprints.index(Sprint.findAll(), sprintForm))
  }

  def create = Action {
    implicit request =>
      sprintForm.bindFromRequest.fold(
      formWithErrors => BadRequest, {
        case (title, num) =>
          new Sprint(title, num).save
          Ok(views.html.sprints.index(Sprint.findAll(), sprintForm))
      })
  }
}