ThisBuild / scalaVersion := "2.13.4"
ThisBuild / version := "1.0.3"
ThisBuild / description := "seichi915Network ワールド自動セーブ・バックアッププラグイン"

resolvers ++= Seq(
  "hub.spigotmc.org" at "https://hub.spigotmc.org/nexus/content/repositories/snapshots/",
  "oss.sonatype.org" at "https://oss.sonatype.org/content/repositories/snapshots",
  "maven.elmakers.com" at "https://maven.elmakers.com/repository/",
  "papermc.io" at "https://papermc.io/repo/repository/maven-public/"
)

libraryDependencies ++= Seq(
  "com.destroystokyo.paper" % "paper-api" % "1.14.4-R0.1-SNAPSHOT",
  "org.typelevel" %% "cats-effect" % "2.3.1",
  "org.apache.commons" % "commons-compress" % "1.20"
)

assemblyMergeStrategy in assembly := {
  case PathList("javax", "servlet", _ @_*) => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith ".properties" =>
    MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith ".xml"       => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith ".types"     => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith ".class"     => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith "plugin.yml" => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith "config.yml" => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith "Syntax.java" =>
    MergeStrategy.first
  case "application.conf" => MergeStrategy.concat
  case "unwanted.txt"     => MergeStrategy.discard
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

lazy val root = (project in file("."))
  .settings(
    name := "seichi915AutoSave",
    assemblyOutputPath in assembly := baseDirectory.value / "target" / "build" / s"seichi915AutoSave-${version.value}.jar"
  )
