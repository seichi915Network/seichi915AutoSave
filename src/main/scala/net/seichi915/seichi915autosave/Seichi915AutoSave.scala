package net.seichi915.seichi915autosave

import net.seichi915.seichi915autosave.command._
import net.seichi915.seichi915autosave.configuration.Configuration
import net.seichi915.seichi915autosave.task._
import net.seichi915.seichi915autosave.util.Util
import org.bukkit.command.{CommandExecutor, TabExecutor}
import org.bukkit.plugin.java.JavaPlugin

import scala.jdk.CollectionConverters._

object Seichi915AutoSave {
  var instance: Seichi915AutoSave = _
}

class Seichi915AutoSave extends JavaPlugin {
  Seichi915AutoSave.instance = this

  override def onEnable(): Unit = {
    Configuration.saveDefaultConfig()
    if (Configuration.isAutoBackupEnabled && !Configuration.getAutoBackupLocation
          .exists())
      Configuration.getAutoBackupLocation.mkdirs()
    if (Configuration.isAutoSaveEnabled)
      new AutoSaveTask().runTaskTimer(this,
                                      Configuration.getAutoSaveInterval,
                                      Configuration.getAutoSaveInterval)
    if (Configuration.isAutoBackupEnabled)
      new AutoBackupTask().runTaskTimer(this,
                                        Configuration.getAutoBackupInterval,
                                        Configuration.getAutoBackupInterval)
    Map(
      "save" -> new SaveCommand,
      "backup" -> new BackupCommand
    ).foreach {
      case (commandName: String, commandExecutor: CommandExecutor) =>
        getServer.getPluginCommand(commandName).setExecutor(commandExecutor)
        getServer
          .getPluginCommand(commandName)
          .setTabCompleter(commandExecutor.asInstanceOf[TabExecutor])
    }

    getLogger.info("seichi915AutoSaveが有効になりました。")
  }

  override def onDisable(): Unit = {
    if (Configuration.isAutoSaveEnabled)
      getServer.getWorlds.asScala.foreach(Util.saveWorld)

    getLogger.info("seichi915AutoSaveが無効になりました。")
  }
}
