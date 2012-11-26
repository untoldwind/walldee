package controllers

import play.api.mvc.{Action, Controller}
import models.Alarm
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json._
import play.api.libs.json.JsArray
import play.api.libs.json.JsObject
import play.api.libs.json.JsNumber
import java.util.Date


object Alarms extends Controller {
  def index = Action {
    Ok(views.html.alarm.index(Alarm.findAll, alarmForm()))
  }

  def eventsJson(start:Long, end:Long) = Action {
    val events = JsArray(Alarm.findAllBetween(new Date(start * 1000L), new Date(end* 1000L)).map {
      alarm =>
        JsObject(Seq(
          "id" -> JsNumber(alarm.id.get),
          "start" -> JsNumber(alarm.nextDate.getTime),
          "end" -> JsNumber(alarm.nextDate.getTime + alarm.durationMins * 60L * 1000L),
          "title" -> JsString(alarm.name)
        ) ++
          alarm.repeatDays.map("repeatDays" -> JsNumber(_)).toSeq)
    })
    Ok(Json.stringify(events)).withHeaders(CONTENT_TYPE -> "application/json")
  }

  def create = Action {
    implicit request =>
      alarmForm().bindFromRequest().fold(
      formWithErrors => BadRequest(views.html.alarm.index(Alarm.findAll, formWithErrors)), {
        alarm =>
          alarm.insert
          Ok(views.html.alarm.list(Alarm.findAll))
      })
  }

  def update(alarmId: Long) = Action {
    implicit request =>
      Alarm.findById(alarmId).map {
        alarm =>
          alarmForm(alarm).bindFromRequest().fold(
          formWithErrors => BadRequest(views.html.alarm.index(Alarm.findAll, formWithErrors)), {
            alarm =>
              alarm.update
              Ok(views.html.alarm.list(Alarm.findAll))
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

  private def alarmForm(alarm: Alarm = new Alarm): Form[Alarm] = Form(
    mapping(
      "id" -> ignored(alarm.id),
      "name" -> text(maxLength = 255),
      "nextDate" -> date("yyyy-MM-dd HH:mm"),
      "durationMins" -> number,
      "repeatDays" -> optional(number(min = 1))
    )(Alarm.apply)(Alarm.unapply)).fill(alarm)
}
