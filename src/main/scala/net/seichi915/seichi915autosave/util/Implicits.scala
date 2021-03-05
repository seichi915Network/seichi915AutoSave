package net.seichi915.seichi915autosave.util

import cats.effect.IO
import org.bukkit.{Bukkit, ChatColor}
import org.bukkit.plugin.java.JavaPlugin

object Implicits {
  implicit class StringOps(string: String) {
    def toNormalMessage: String =
      s"${ChatColor.AQUA}[${ChatColor.WHITE}seichi915AutoSave${ChatColor.AQUA}]${ChatColor.RESET} $string"

    def toSuccessMessage: String =
      s"${ChatColor.AQUA}[${ChatColor.GREEN}seichi915AutoSave${ChatColor.AQUA}]${ChatColor.RESET} $string"

    def toWarningMessage: String =
      s"${ChatColor.AQUA}[${ChatColor.GOLD}seichi915AutoSave${ChatColor.AQUA}]${ChatColor.RESET} $string"

    def toErrorMessage: String =
      s"${ChatColor.AQUA}[${ChatColor.RED}seichi915AutoSave${ChatColor.AQUA}]${ChatColor.RESET} $string"
  }

  implicit class AnyOps(any: Any) {
    def isNull: Boolean = Option(any).flatMap(_ => Some(false)).getOrElse(true)

    def nonNull: Boolean = !isNull
  }

  implicit class IOOps(io: IO[_]) {
    def unsafeRunOnServerThread(javaPlugin: JavaPlugin): Unit =
      Bukkit.getScheduler
        .runTask(javaPlugin, (() => io.unsafeRunSync()): Runnable)
  }
}
