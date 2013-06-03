import sbt._
import sbt.Keys._

object Common {
  def settings: Seq[Setting[_]] = Seq(
    version := "0.1.0-SNAPSHOT",
    crossScalaVersions := Seq("2.9.3", "2.10.0", "2.10.1"),
    scalaVersion := "2.9.3"
  )
}

object Build extends sbt.Build {
  lazy val root = Project(
    "retry", file("."), settings =
      Defaults.defaultSettings ++ Common.settings ++ Seq(
      publish := { }
      )
    ).aggregate(core, netty, twitter) 

  def module(name: String) =
    Project("retry-%s" format name,
            file(name),
            settings = Defaults.defaultSettings ++ Common.settings)

  lazy val core = module("core")

  lazy val netty = module("netty").dependsOn(core)

  lazy val twitter = module("twitter").dependsOn(core)
}
