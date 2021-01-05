package net.seichi915.seichi915autosave

import java.io.{
  BufferedInputStream,
  BufferedOutputStream,
  DataInputStream,
  DataOutputStream,
  File,
  FileOutputStream
}
import java.net.{HttpURLConnection, URL}
import java.nio.file.Files
import java.util.zip.ZipFile

import net.seichi915.seichi915autosave.command._
import net.seichi915.seichi915autosave.configuration.Configuration
import net.seichi915.seichi915autosave.task._
import net.seichi915.seichi915autosave.util.Util
import org.bukkit.Bukkit
import org.bukkit.command.{CommandExecutor, TabExecutor}
import org.bukkit.plugin.java.JavaPlugin

import scala.jdk.CollectionConverters._

object Seichi915AutoSave {
  var instance: Seichi915AutoSave = _

  var backupWorkDirectory: File = _
  var seichi915BackupFile: File = _
}

class Seichi915AutoSave extends JavaPlugin {
  Seichi915AutoSave.instance = this

  override def onEnable(): Unit = {
    Configuration.saveDefaultConfig()
    if (Configuration.isAutoBackupEnabled)
      if (!Configuration.getAutoBackupLocation
            .exists() || !Configuration.getAutoBackupLocation.isDirectory)
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
    if (Configuration.isAutoBackupEnabled) {
      try {
        Seichi915AutoSave.backupWorkDirectory =
          new File(getDataFolder, "workspace")
        if (Seichi915AutoSave.backupWorkDirectory.exists())
          Util.rmDir(Seichi915AutoSave.backupWorkDirectory)
        val seichi915BackupDirectory =
          new File(Seichi915AutoSave.backupWorkDirectory, "seichi915Backup")
        seichi915BackupDirectory.mkdirs()
        getLogger.info(
          s"[1/5] seichi915Backup v${Configuration.getSeichi915BackupVersion}をダウンロードしています...")
        val sourceCodeURL = new URL(
          s"https://github.com/seichi915Network/seichi915Backup/archive/v${Configuration.getSeichi915BackupVersion}.zip")
        val httpURLConnection =
          sourceCodeURL.openConnection().asInstanceOf[HttpURLConnection]
        httpURLConnection.setAllowUserInteraction(false)
        httpURLConnection.setInstanceFollowRedirects(true)
        httpURLConnection.setRequestMethod("GET")
        httpURLConnection.connect()
        val statusCode = httpURLConnection.getResponseCode
        if (statusCode != HttpURLConnection.HTTP_OK) {
          getLogger.severe(s"seichi915Backupをダウンロードできませんでした: $statusCode")
          Bukkit.shutdown()
          return
        }
        val dataInputStream = new DataInputStream(
          httpURLConnection.getInputStream)
        val seichi915BackupSourceFile = new File(
          seichi915BackupDirectory,
          s"v${Configuration.getSeichi915BackupVersion}.zip")
        val dataOutputStream = new DataOutputStream(
          new BufferedOutputStream(
            new FileOutputStream(seichi915BackupSourceFile)))
        var bytes = new Array[Byte](4096)
        var read = 0
        while ({
          read = dataInputStream.read(bytes)
          read
        } != -1) dataOutputStream.write(bytes, 0, read)
        dataInputStream.close()
        dataOutputStream.close()
        httpURLConnection.disconnect()
        getLogger.info(
          s"[2/5] seichi915Backup v${Configuration.getSeichi915BackupVersion}を解凍しています...")
        val zipFile = new ZipFile(seichi915BackupSourceFile)
        val enumZip = zipFile.entries()
        read = 0
        bytes = new Array[Byte](1024)
        while (enumZip.hasMoreElements) {
          val zipEntry = enumZip.nextElement()
          val outFile =
            new File(seichi915BackupDirectory.getAbsolutePath, zipEntry.getName)
          if (zipEntry.isDirectory) outFile.mkdir()
          else {
            val bufferedInputStream = new BufferedInputStream(
              zipFile.getInputStream(zipEntry))
            if (!outFile.getParentFile.exists())
              outFile.getParentFile.mkdirs()
            val bufferedOutputStream = new BufferedOutputStream(
              new FileOutputStream(outFile))
            while ({
              read = bufferedInputStream.read(bytes)
              read
            } != -1) bufferedOutputStream.write(bytes, 0, read)
            bufferedOutputStream.flush()
            bufferedOutputStream.close()
            bufferedInputStream.close()
          }
        }
        zipFile.close()
        getLogger.info(
          s"[3/5] seichi915Backup v${Configuration.getSeichi915BackupVersion}をビルドしています...")
        val seichi915BackupSourceDirectory = new File(
          seichi915BackupDirectory,
          s"seichi915Backup-${Configuration.getSeichi915BackupVersion}")
        val runtime = Runtime.getRuntime
        val process = runtime.exec(
          "cargo build",
          null,
          seichi915BackupSourceDirectory
        )
        process.waitFor()
        getLogger.info(
          s"[4/5] seichi915Backup v${Configuration.getSeichi915BackupVersion}の準備をしています...")
        val seichi915BackupTargetDirectory =
          new File(seichi915BackupSourceDirectory, "target")
        if (new File(seichi915BackupTargetDirectory, "debug")
              .exists()) {
          val seichi915BackupDebugDirectory =
            new File(seichi915BackupTargetDirectory, "debug")
          val sourcePath = new File(
            seichi915BackupDebugDirectory,
            s"seichi915Backup${if (Util.isWindows) ".exe" else ""}").toPath
          val targetPath = new File(
            seichi915BackupDirectory,
            s"seichi915Backup${if (Util.isWindows) ".exe" else ""}").toPath
          Files.copy(sourcePath, targetPath)
        } else if (new File(seichi915BackupTargetDirectory, "release")
                     .exists()) {
          val seichi915BackupReleaseDirectory =
            new File(seichi915BackupTargetDirectory, "release")
          val sourcePath = new File(
            seichi915BackupReleaseDirectory,
            s"seichi915Backup${if (Util.isWindows) ".exe" else ""}").toPath
          val targetPath = new File(
            seichi915BackupDirectory,
            s"seichi915Backup${if (Util.isWindows) ".exe" else ""}").toPath
          Files.copy(sourcePath, targetPath)
        }
        getLogger.info(s"[5/5] クリーンアップしています...")
        seichi915BackupSourceFile.delete()
        Util.rmDir(seichi915BackupSourceDirectory)
        Seichi915AutoSave.seichi915BackupFile = new File(
          seichi915BackupDirectory,
          s"seichi915Backup${if (Util.isWindows) ".exe" else ""}")
      } catch {
        case e: Exception =>
          e.printStackTrace()
          getLogger.severe("seichi915Backupの準備に失敗しました。サーバーを停止します。")
          Bukkit.shutdown()
          return
      }
    }

    getLogger.info("seichi915AutoSaveが有効になりました。")
  }

  override def onDisable(): Unit = {
    if (Configuration.isAutoSaveEnabled)
      getServer.getWorlds.asScala.foreach(Util.saveWorld)
    getLogger.info("クリーンアップしています...")
    Util.rmDir(Seichi915AutoSave.backupWorkDirectory)

    getLogger.info("seichi915AutoSaveが無効になりました。")
  }
}
