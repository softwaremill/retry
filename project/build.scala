import sbt._
import sbt.Keys._

object Common {
  def settings: Seq[Setting[_]] = Seq(
    organization := "me.lessis",
    version := "0.1.0",
    crossScalaVersions := Seq("2.9.3", "2.10.2"),
    scalaVersion := "2.10.2",
    licenses <<= version(v =>
      Seq("MIT" ->
          url("https://github.com/softprops/retry/blob/%s/LICENSE" format v))),
    homepage := Some(url("https://github.com/softprops/retry/")),
    publishTo := Some(Opts.resolver.sonatypeStaging),
    publishArtifact in Test := false,
    publishMavenStyle := true,
    pomExtra := (
      <scm>
        <url>git@github.com:softprops/retry.git</url>
        <connection>scm:git:git@github.com:softprops/retry.git</connection>
      </scm>
      <developers>
        <developer>
          <id>softprops</id>
          <name>Doug Tangren</name>
          <url>http://github.com/softprops</url>
      </developer>
      </developers>)) ++ ls.Plugin.lsSettings ++ Seq(
      ls.Plugin.LsKeys.tags in ls.Plugin.LsKeys.lsync := Seq("github", "gist")
    )
}

object Build extends sbt.Build {
  lazy val root = Project(
    "retry", file("."), settings =
      Defaults.defaultSettings ++ Common.settings ++ Seq(
        test := { }, // no tests
        publish := { }, // skip publishing for this root project.
        publishLocal := { }, // skip publishing locally,
        ls.Plugin.LsKeys.skipWrite := true // don't track root in ls
      )
    ).aggregate(core, netty, twitter) 

  def module(name: String) =
    Project("retry-%s" format name,
            file(name),
            settings = Defaults.defaultSettings ++ Common.settings)

  lazy val core = module("core")

  lazy val netty = module("netty").dependsOn(core)

  lazy val twitter = module("twitter")
    .dependsOn(core)
}
