package net.seichi915.seichi915autosave.task

import cats.effect.IO
import net.seichi915.seichi915autosave.Seichi915AutoSave
import net.seichi915.seichi915autosave.configuration.Configuration
import net.seichi915.seichi915autosave.util.Util
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable

import scala.concurrent.ExecutionContext
import scala.jdk.CollectionConverters._

class AutoBackupTask extends BukkitRunnable {
  override def run(): Unit = {
    if (Configuration.isAutoBackupMessageEnabled)
      Bukkit.broadcastMessage(Configuration.getAutoBackupStartMessage)
    val task = IO {
      try {
        Util.backupWorlds(Bukkit.getServer.getWorlds.asScala.toList)
        if (Configuration.isAutoBackupMessageEnabled)
          Bukkit.broadcastMessage(Configuration.getAutoBackupFinishMessage)
      } catch {
        case e: Exception =>
          e.printStackTrace()
          Seichi915AutoSave.instance.getLogger
            .warning(
              s"ワールド ${Bukkit.getServer.getWorlds.asScala.toList.map(_.getName).mkString(", ")} のバックアップに失敗しました。")
      }
    }
    val contextShift = IO.contextShift(ExecutionContext.global)
    IO.shift(contextShift).flatMap(_ => task).unsafeRunAsyncAndForget()
  }
}
