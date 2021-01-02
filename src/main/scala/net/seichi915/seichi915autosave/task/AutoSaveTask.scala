package net.seichi915.seichi915autosave.task

import net.seichi915.seichi915autosave.Seichi915AutoSave
import net.seichi915.seichi915autosave.configuration.Configuration
import net.seichi915.seichi915autosave.util.Util
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable

import scala.concurrent.ExecutionContext.Implicits.global
import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success}

class AutoSaveTask extends BukkitRunnable {
  override def run(): Unit = {
    if (Configuration.isAutoSaveMessageEnabled)
      Bukkit.broadcastMessage(Configuration.getAutoSaveStartMessage)
    Bukkit.getServer.getWorlds.asScala.foreach(world => {
      Util.saveWorld(world) onComplete {
        case Success(_) =>
          if (Configuration.isAutoSaveMessageEnabled)
            Bukkit.broadcastMessage(Configuration.getAutoSaveFinishMessage)
        case Failure(exception) =>
          exception.printStackTrace()
          Seichi915AutoSave.instance.getLogger
            .warning(s"ワールド ${world.getName} のセーブに失敗しました。")
      }
    })
  }
}
