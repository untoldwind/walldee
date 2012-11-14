package controllers

import play.api.mvc.{Action, Controller}
import models.{Display, Sprint, Alarm}
import play.api.data.Form
import play.api.data.Forms._
import scala.Some
import models.json.SprintCounter
import java.sql.Timestamp



object Alarms extends Controller {
  def index = Action {
    Ok(views.html.alarm.index(Alarm.findAll, alarmForm()))
  }

  def create = Action {
    implicit request =>
      alarmForm().bindFromRequest().fold(
      formWithErrors => BadRequest(views.html.alarm.index(Alarm.findAll, formWithErrors)), {
        alarm =>
          alarm.insert
          Ok(views.html.alarm.index(Alarm.findAll, alarmForm()))
      })
  }

  def edit(alarmId: Long) = Action {
    Alarm.findById(alarmId).map {
      alarm =>
        Ok(views.html.alarm.edit(alarm, alarmForm(alarm)))
    }.getOrElse(NotFound)
  }

  def update(alarmId: Long) = Action {
    implicit request =>
      Alarm.findById(alarmId).map {
        alarm =>
          alarmForm(alarm).bindFromRequest().fold(
          formWithErrors => BadRequest(views.html.alarm.index(Alarm.findAll, formWithErrors)), {
            alarm =>
              alarm.update
              Redirect(routes.Alarms.index)
          })
      }.getOrElse(NotFound)
  }

  def delete(alarmId: Long) = Action {
    Alarm.findById(alarmId).map {
      alarm =>
        alarm.delete
        Ok(views.html.alarm.list(Alarm.findAll))
    }.getOrElse(NotFound)
  }

  private def alarmForm(alarm:Alarm = new Alarm): Form[Alarm] = Form(
    mapping(
      "id" ->  ignored(alarm.id),
      "name" -> text(maxLength = 255),
      "nextDate" -> date("dd-MM-yyyy HH:mm"),
      "repeatDays" -> optional(number(min = 1))
    )(Alarm.apply)(Alarm.unapply)).fill(alarm)
}
