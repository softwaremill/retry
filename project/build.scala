import sbt._
import sbt.Keys._

object Common {
  def settings: Seq[Setting[_]] = Seq(
    version := "0.1.0-SNAPSHOT",
    crossScalaVersions := Seq("2.9.3", "2.10.1"),
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

  lazy val core = Project(
    "retry-core", file("core"),
    settings = Defaults.defaultSettings ++ Common.settings)

  lazy val netty = Project(
    "retry-netty", file("netty"),
    settings = Defaults.defaultSettings ++ Common.settings
  ).dependsOn(core)

  lazy val twitter = Project(
    "retry-twitter", file("twitter"),
    settings = Defaults.defaultSettings ++ Common.settings
  ).dependsOn(core)
}
