package models.statusValues

import play.api.libs.json._
import play.api.libs.json.JsObject

object HostServiceStatusTypes extends Enumeration {
  type Type = Value
  val Ok, Warning, Critical = Value
}

object HostStatusTypes extends Enumeration {
  type Type = Value
  val Up, Down = Value
}

case class HostStatus(name: String, hostStatus: HostStatusTypes.Type, serviceStatus: HostServiceStatusTypes.Type)

object HostStatus {

  implicit object HostStatusFormat extends Format[HostStatus] {
    override def reads(json: JsValue): JsResult[HostStatus] =
      JsSuccess(HostStatus(
        (json \ "name").as[String],
        HostStatusTypes((json \ "hostStatus").as[Int]),
        HostServiceStatusTypes((json \ "serviceStatus").as[Int])))

    override def writes(icingaHost: HostStatus): JsValue = JsObject(
      Seq("name" -> JsString(icingaHost.name),
        "hostStatus" -> JsNumber(icingaHost.hostStatus.id),
        "serviceStatus" -> JsNumber(icingaHost.serviceStatus.id)))
  }

}

case class HostsGroup(hosts: Seq[HostStatus])

object HostsGroup {

  implicit object HostsGroupFormat extends Format[HostsGroup] {
    override def reads(json: JsValue): JsResult[HostsGroup] =
      JsSuccess(HostsGroup(
        (json \ "hosts").as[Seq[HostStatus]]))

    override def writes(icingaGroup: HostsGroup): JsValue = JsObject(
      Seq("hosts" -> Json.toJson(icingaGroup.hosts)))
  }

}

case class HostsStatus(groups: Seq[HostsGroup])

object HostsStatus {

  implicit object HostsStatusFormat extends Format[HostsStatus] {
    override def reads(json: JsValue): JsResult[HostsStatus] =
      JsSuccess(HostsStatus(
        (json \ "groups").as[Seq[HostsGroup]]))

    override def writes(icingaStatus: HostsStatus): JsValue = JsObject(
      Seq("groups" -> Json.toJson(icingaStatus.groups)))
  }

}
