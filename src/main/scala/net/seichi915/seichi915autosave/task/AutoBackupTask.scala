package net.seichi915.seichi915autosave.task

import net.seichi915.seichi915autosave.Seichi915AutoSave
import net.seichi915.seichi915autosave.configuration.Configuration
import net.seichi915.seichi915autosave.util.Util
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable

import scala.concurrent.ExecutionContext.Implicits.global
import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success}

class AutoBackupTask extends BukkitRunnable {
  override def run(): Unit = {
    if (Configuration.isAutoBackupMessageEnabled)
      Bukkit.broadcastMessage(Configuration.getAutoBackupStartMessage)
    Util.backupWorlds(Bukkit.getServer.getWorlds.asScala.toList) onComplete {
      case Success(_) =>
        if (Configuration.isAutoBackupMessageEnabled)
          Bukkit.broadcastMessage(Configuration.getAutoBackupFinishMessage)
      case Failure(exception) =>
        exception.printStackTrace()
        Seichi915AutoSave.instance.getLogger
          .warning(
            s"ワールド ${Bukkit.getServer.getWorlds.asScala.toList.map(_.getName).mkString(", ")} のバックアップに失敗しました。")
    }
  }
}
