package models.config

import java.util.Date
import play.api.libs.json._
import play.api.libs.json.JsObject
import play.api.libs.json.JsNumber

case class BackupConfig(lastBackup: Option[Date])

object BackupConfig {

  implicit object BackupConfigFormat extends Format[BackupConfig] {
    override def reads(json: JsValue): JsResult[BackupConfig] =
      JsSuccess(BackupConfig(
        (json \ "lastBackup").asOpt[Long].map(new Date(_))))

    override def writes(backupConfig: BackupConfig): JsValue = JsObject(
      backupConfig.lastBackup.map(d => "lastBackup" -> JsNumber(d.getTime)).toSeq)
  }

}
