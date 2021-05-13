package net.seichi915.seichi915autosave.configuration

import net.seichi915.seichi915autosave.Seichi915AutoSave
import org.bukkit.ChatColor

import java.io.File

object Configuration {
  def saveDefaultConfig(): Unit = Seichi915AutoSave.instance.saveDefaultConfig()

  def isAutoSaveEnabled: Boolean =
    Seichi915AutoSave.instance.getConfig.getBoolean("AutoSave.Enable")

  def getAutoSaveInterval: Int =
    Seichi915AutoSave.instance.getConfig.getInt("AutoSave.Interval")

  def isAutoSaveMessageEnabled: Boolean =
    Seichi915AutoSave.instance.getConfig.getBoolean("AutoSave.MessageEnable")

  def getAutoSaveStartMessage: String =
    ChatColor.translateAlternateColorCodes(
      '&',
      Seichi915AutoSave.instance.getConfig.getString("AutoSave.StartMessage")
    )

  def getAutoSaveFinishMessage: String =
    ChatColor.translateAlternateColorCodes(
      '&',
      Seichi915AutoSave.instance.getConfig.getString("AutoSave.FinishMessage")
    )

  def isAutoBackupEnabled: Boolean =
    Seichi915AutoSave.instance.getConfig.getBoolean("AutoBackup.Enable")

  def getAutoBackupInterval: Int =
    Seichi915AutoSave.instance.getConfig.getInt("AutoBackup.Interval")

  def getAutoBackupLocation: File =
    new File(
      Seichi915AutoSave.instance.getConfig.getString("AutoBackup.Location")
    )

  def isAutoBackupMessageEnabled: Boolean =
    Seichi915AutoSave.instance.getConfig.getBoolean("AutoBackup.MessageEnable")

  def getAutoBackupStartMessage: String =
    ChatColor.translateAlternateColorCodes(
      '&',
      Seichi915AutoSave.instance.getConfig.getString("AutoBackup.StartMessage")
    )

  def getAutoBackupFinishMessage: String =
    ChatColor.translateAlternateColorCodes(
      '&',
      Seichi915AutoSave.instance.getConfig
        .getString("AutoBackup.FinishMessage")
    )
}
