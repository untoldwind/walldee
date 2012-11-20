package models.config

import java.util.Date
import play.api.libs.json.{JsNumber, JsObject, JsValue, Format}

case class BackupConfig(lastBackup: Option[Date])

object BackupConfig {

  implicit object BackupConfigFormat extends Format[BackupConfig] {
    override def reads(json: JsValue): BackupConfig =
      BackupConfig(
        (json \ "lastBackup").asOpt[Long].map(new Date(_)))

    override def writes(backupConfig: BackupConfig): JsValue = JsObject(
      backupConfig.lastBackup.map(d => "lastBackup" -> JsNumber(d.getTime)).toSeq)
  }

}
