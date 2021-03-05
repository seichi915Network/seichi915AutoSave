package net.seichi915.seichi915autosave.command

import net.seichi915.seichi915autosave.configuration.Configuration
import net.seichi915.seichi915autosave.util.Implicits._
import net.seichi915.seichi915autosave.util.Util
import org.bukkit.{Bukkit, World}
import org.bukkit.command.{Command, CommandExecutor, CommandSender, TabExecutor}
import org.bukkit.util.StringUtil

import java.util
import java.util.Collections
import scala.jdk.CollectionConverters._

class SaveCommand extends CommandExecutor with TabExecutor {
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
    if (Configuration.isAutoSaveMessageEnabled)
      Bukkit.broadcastMessage(Configuration.getAutoSaveStartMessage)
    targetWorlds.foreach(Util.saveWorld)
    if (Configuration.isAutoSaveMessageEnabled)
      Bukkit.broadcastMessage(Configuration.getAutoSaveFinishMessage)
    sender.sendMessage(
      s"${targetWorlds.length}個のワールドのセーブが完了しました。".toSuccessMessage)
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
