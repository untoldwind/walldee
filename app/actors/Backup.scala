package actors

import play.api.db._
import play.api.Play.current

import akka.actor.Actor
import akka.event.slf4j.SLF4JLogging
import actors.Backup.CheckBackup
import models.{ConfigTypes, Config}
import java.sql.Connection
import org.joda.time.format.DateTimeFormat
import org.joda.time.DateMidnight
import java.util.Date
import models.config.BackupConfig
import play.api.libs.json.Json

class Backup extends Actor with SLF4JLogging {
  def receive = {
    case CheckBackup() =>
      val config = Config.findByType(ConfigTypes.Backup)

      config.map {
        config =>
          val lastBackup = config.backupConfig.flatMap(_.lastBackup)
          if (lastBackup.isEmpty || lastBackup.get.getTime <= System.currentTimeMillis() - 24L * 3600L * 1000L) {
            performBackup()
            new Config(ConfigTypes.Backup, Json.toJson(BackupConfig(Some(new Date)))).update
          }
      }.getOrElse {
        performBackup()
        new Config(ConfigTypes.Backup, Json.toJson(BackupConfig(Some(new Date)))).insert
      }
    case message =>
      log.error("Received invalid message " + message.toString)
  }

  private def performBackup() {
    val dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd")

    DB.withConnection {
      connection: Connection =>
        val stmt = connection.createStatement
        stmt.execute("BACKUP to 'backup-%s.zip'".format(dateFormatter.print(new DateMidnight())))
    }
  }
}

object Backup {

  case class CheckBackup()

}