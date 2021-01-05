package net.seichi915.seichi915autosave.task

import net.seichi915.seichi915autosave.configuration.Configuration
import net.seichi915.seichi915autosave.util.Util
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable

import scala.jdk.CollectionConverters._

class AutoSaveTask extends BukkitRunnable {
  override def run(): Unit = {
    if (Configuration.isAutoSaveMessageEnabled)
      Bukkit.broadcastMessage(Configuration.getAutoSaveStartMessage)
    Bukkit.getServer.getWorlds.asScala.foreach(Util.saveWorld)
    if (Configuration.isAutoSaveMessageEnabled)
      Bukkit.broadcastMessage(Configuration.getAutoSaveFinishMessage)
  }
}
