package net.seichi915.seichi915autosave.util

import cats.effect.IO
import net.seichi915.seichi915autosave.Seichi915AutoSave
import net.seichi915.seichi915autosave.configuration.Configuration
import net.seichi915.seichi915autosave.util.Implicits._
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.bukkit.{Bukkit, World}

import java.io.{BufferedInputStream, File, FileInputStream, FileOutputStream}
import java.lang.reflect.Field
import java.util.{Calendar, TimeZone}
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
    if (Configuration.isAutoSaveEnabled)
      worlds.foreach { world =>
        IO(world.save()).unsafeRunOnServerThread(Seichi915AutoSave.instance)
      }
    while (
      Configuration.getAutoBackupLocation
        .listFiles()
        .length >= 50
    )
      Configuration.getAutoBackupLocation
        .listFiles()
        .min
        .delete()
    val workspace =
      new File(Seichi915AutoSave.instance.getDataFolder, "workspace")
    if (workspace.exists()) rmDir(workspace)
    workspace.mkdirs()
    worlds.foreach { world =>
      copyWorldDirectory(
        world.getWorldFolder,
        new File(workspace, world.getName)
      )
    }
    val calendar = Calendar.getInstance(TimeZone.getDefault)
    val archiveFileName =
      s"${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH) + 1}-${calendar
        .get(Calendar.DATE)}_${calendar.get(Calendar.HOUR_OF_DAY)}-${calendar
        .get(Calendar.MINUTE)}-${calendar.get(Calendar.SECOND)}.tar.gz"
    val archiveFile =
      new File(Configuration.getAutoBackupLocation, archiveFileName)
    compressDirectory(workspace, archiveFile)
    rmDir(workspace)
  }

  def rmDir(file: File): Unit = {
    if (file.isDirectory)
      file.listFiles().foreach(rmDir)
    file.delete()
  }

  def compressDirectory(directory: File, outputFile: File): Unit = {
    val tarArchiveOutputStream = new TarArchiveOutputStream(
      new FileOutputStream(outputFile)
    )
    def addItem(name: String, file: File): Unit = {
      if (file.isDirectory)
        file.listFiles().foreach { f =>
          addItem(s"$name/${f.getName}", f)
        }
      else {
        val archiveEntry = tarArchiveOutputStream.createArchiveEntry(file, name)
        tarArchiveOutputStream.putArchiveEntry(archiveEntry)
        val bufferedInputStream = new BufferedInputStream(
          new FileInputStream(file)
        )
        var size = 0
        val bytes = new Array[Byte](1024)
        while ({
          size = bufferedInputStream.read(bytes)
          size
        } > 0) tarArchiveOutputStream.write(bytes, 0, size)
        bufferedInputStream.close()
        tarArchiveOutputStream.closeArchiveEntry()
      }
    }
    directory.listFiles().foreach { file =>
      addItem(file.getName, file)
    }
    tarArchiveOutputStream.close()
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
