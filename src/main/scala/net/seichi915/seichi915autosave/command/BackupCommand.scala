package net.seichi915.seichi915autosave.command

import cats.effect.IO
import net.seichi915.seichi915autosave.Seichi915AutoSave
import net.seichi915.seichi915autosave.configuration.Configuration
import net.seichi915.seichi915autosave.util.Implicits._
import net.seichi915.seichi915autosave.util.Util
import org.bukkit.{Bukkit, World}
import org.bukkit.command.{Command, CommandExecutor, CommandSender, TabExecutor}
import org.bukkit.util.StringUtil

import java.util
import java.util.Collections
import scala.concurrent.ExecutionContext
import scala.jdk.CollectionConverters._

class BackupCommand extends CommandExecutor with TabExecutor {
  override def onCommand(sender: CommandSender,
                         command: Command,
                         label: String,
                         args: Array[String]): Boolean = {
    var targetWorlds = List[World]()
    if (args.isEmpty)
      targetWorlds =
        targetWorlds.appendedAll(Bukkit.getServer.getWorlds.asScala)
    else
      args.foreach { arg =>
        val world = Bukkit.getWorld(arg)
        if (world.isNull) {
          sender.sendMessage(s"ワールド $arg は見つかりませんでした。".toErrorMessage)
          return true
        }
        if (targetWorlds.contains(world)) {
          sender.sendMessage(s"ワールド $arg は既に指定されています。".toErrorMessage)
          return true
        }
        targetWorlds = targetWorlds.appended(world)
      }
    if (Configuration.isAutoBackupMessageEnabled)
      Bukkit.broadcastMessage(Configuration.getAutoBackupStartMessage)
    val task = IO {
      try {
        Util.backupWorlds(targetWorlds)
        if (Configuration.isAutoBackupMessageEnabled)
          Bukkit.broadcastMessage(Configuration.getAutoBackupFinishMessage)
        sender.sendMessage(
          s"${targetWorlds.length}個のワールドのバックアップが完了しました。".toSuccessMessage)
      } catch {
        case e: Exception =>
          e.printStackTrace()
          Seichi915AutoSave.instance.getLogger
            .warning(
              s"ワールド ${targetWorlds.map(_.getName).mkString(", ")} のバックアップに失敗しました。")
          sender.sendMessage(
            s"${targetWorlds.length}個のワールドのバックアップに失敗しました。".toErrorMessage)
      }
    }
    val contextShift = IO.contextShift(ExecutionContext.global)
    IO.shift(contextShift).flatMap(_ => task).unsafeRunAsyncAndForget()
    true
  }

  override def onTabComplete(sender: CommandSender,
                             command: Command,
                             alias: String,
                             args: Array[String]): util.List[String] = {
    val completions = new util.ArrayList[String]()
    import scala.jdk.CollectionConverters._
    StringUtil.copyPartialMatches(
      args.last,
      Bukkit.getWorlds.asScala.map(_.getName).asJava,
      completions)
    Collections.sort(completions)
    completions
  }
}
