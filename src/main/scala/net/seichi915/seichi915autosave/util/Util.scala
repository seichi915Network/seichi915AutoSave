package net.seichi915.seichi915autosave.util

import net.seichi915.seichi915autosave.Seichi915AutoSave
import net.seichi915.seichi915autosave.configuration.Configuration
import org.bukkit.{Bukkit, World}

import java.io.{File, FileInputStream, FileOutputStream}
import java.lang.reflect.Field
import scala.annotation.tailrec

object Util {
  @tailrec
  def getField(clazz: Class[_], name: String): Option[Field] = {
    clazz.getDeclaredFields.find(_.getName.equals(name)) match {
      case s @ Some(field) =>
        field.setAccessible(true)
        s
      case None =>
        clazz.getSuperclass match {
          case null => None
          case s    => getField(s, name)
        }
    }
  }

  def saveWorld(world: World): Unit = {
    val server = Bukkit.getServer
    val minecraftServer =
      getField(server.getClass, "console").get.get(server)
    getField(minecraftServer.getClass, "autosavePeriod").get
      .set(minecraftServer, 0)
    world.save()
  }

  def backupWorlds(worlds: List[World]): Unit = {
    if (Configuration.isAutoSaveEnabled) worlds.foreach { world =>
      Bukkit.getScheduler.runTask(Seichi915AutoSave.instance,
                                  (() => saveWorld(world)): Runnable)
    }
    val workDirectory =
      new File(Seichi915AutoSave.backupWorkDirectory, "Worlds")
    workDirectory.mkdirs()
    worlds.foreach { world =>
      val source = world.getWorldFolder
      val target = new File(workDirectory, world.getName)
      copyWorldDirectory(source, target)
    }
    val command =
      s"${Seichi915AutoSave.seichi915BackupFile.getAbsolutePath} --target=. --destination=${Configuration.getAutoBackupLocation.getAbsolutePath}"
    val runtime = Runtime.getRuntime
    val process = runtime.exec(
      command,
      null,
      workDirectory
    )
    process.waitFor()
    rmDir(workDirectory)
  }

  def isWindows: Boolean =
    System.getProperty("os.name").toLowerCase.startsWith("windows")

  def rmDir(file: File): Unit = {
    if (file.isFile) file.delete()
    else {
      val files = file.listFiles()
      files.foreach(rmDir)
      file.delete()
    }
  }

  def copyWorldDirectory(source: File, target: File): Unit = {
    if (source.isDirectory) {
      if (!target.exists()) target.mkdir()
      val files = source.list()
      files.foreach { file =>
        val sourceFile = new File(source, file)
        val targetFile = new File(target, file)
        copyWorldDirectory(sourceFile, targetFile)
      }
    } else {
      if (source.getName.equals("session.lock")) return
      val inputStream = new FileInputStream(source)
      val outputStream = new FileOutputStream(target)
      val bytes = new Array[Byte](1024)
      var read = 0
      while ({
        read = inputStream.read(bytes)
        read
      } != -1) outputStream.write(bytes, 0, read)
      inputStream.close()
      outputStream.close()
    }
  }
}
