package models.statusValues

import play.api.libs.json._
import play.api.libs.json.JsObject

object IcingaServiceStatusTypes extends Enumeration {
  type Type = Value
  val Ok, Warning, Critical = Value
}

object IcingaHostStatusTypes extends Enumeration {
  type Type = Value
  val Up, Down = Value
}

case class IcingaStatusHost(name: String, hostStatus: IcingaHostStatusTypes.Type, serviceStatus: IcingaServiceStatusTypes.Type)

object IcingaStatusHost {

  implicit object IcingaHostFormat extends Format[IcingaStatusHost] {
    override def reads(json: JsValue): IcingaStatusHost =
      IcingaStatusHost(
        (json \ "name").as[String],
        IcingaHostStatusTypes((json \ "hostStatus").as[Int]),
        IcingaServiceStatusTypes((json \ "serviceStatus").as[Int]))

    override def writes(icingaHost: IcingaStatusHost): JsValue = JsObject(
      Seq("name" -> JsString(icingaHost.name),
        "hostStatus" -> JsNumber(icingaHost.hostStatus.id),
        "serviceStatus" -> JsNumber(icingaHost.serviceStatus.id)))
  }

}

case class IcingaStatusGroup(hosts: Seq[IcingaStatusHost])

object IcingaStatusGroup {

  implicit object IcingaGroupFormat extends Format[IcingaStatusGroup] {
    override def reads(json: JsValue): IcingaStatusGroup =
      IcingaStatusGroup(
        (json \ "hosts").as[Seq[IcingaStatusHost]])

    override def writes(icingaGroup: IcingaStatusGroup): JsValue = JsObject(
      Seq("hosts" -> Json.toJson(icingaGroup.hosts)))
  }

}

case class IcingaStatus(groups: Seq[IcingaStatusGroup])

object IcingaStatus {

  implicit object IcingaStatusFormat extends Format[IcingaStatus] {
    override def reads(json: JsValue): IcingaStatus =
      IcingaStatus(
        (json \ "groups").as[Seq[IcingaStatusGroup]])

    override def writes(icingaStatus: IcingaStatus): JsValue = JsObject(
      Seq("groups" -> Json.toJson(icingaStatus.groups)))
  }

}
