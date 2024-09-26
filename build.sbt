import com.softwaremill.SbtSoftwareMillCommon.commonSmlBuildSettings
import com.softwaremill.Publish.ossPublishSettings

val scala211 = "2.11.12"
val scala212 = "2.12.19"
val scala213 = "2.13.15"
val scala30 = "3.4.1"

val commonSettings = commonSmlBuildSettings ++ ossPublishSettings ++ List(
  organization := "com.softwaremill.retry"
)

lazy val rootProject = (project in file("."))
  .settings(commonSettings: _*)
  .settings(publish / skip := true, name := "retry", scalaVersion := scala213)
  .aggregate(retry.projectRefs: _*)

lazy val retry = (projectMatrix in file("retry"))
  .settings(commonSettings: _*)
  .settings(
    scalacOptions += "-feature",
    moduleName := "retry",
    name := "retry",
    description := "a library of simple primitives for asynchronously retrying Scala Futures",
    libraryDependencies ++=
      Seq(
        "org.scalatest" %%% "scalatest" % "3.2.18" % "test",
        "com.softwaremill.odelay" %%% "odelay-core" % "0.4.0",
        "org.scala-lang.modules" %%% "scala-collection-compat" % "2.12.0"
      )
  )
  .jvmPlatform(
    scalaVersions = List(scala211, scala212, scala213, scala30)
  )
  .jsPlatform(
    scalaVersions = List(scala212, scala213, scala30),
    settings = Seq(
      libraryDependencies += "io.github.cquiroz" %%% "scala-java-time" % "2.5.0"
    )
  )
