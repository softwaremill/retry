import com.softwaremill.SbtSoftwareMillCommon.commonSmlBuildSettings
import com.softwaremill.Publish.ossPublishSettings

val scala212 = "2.12.21"
val scala213 = "2.13.18"
val scala30 = "3.4.1"

commonSmlBuildSettings
ossPublishSettings

organization := "com.softwaremill.retry"

lazy val rootProject = (project in file("."))
  .settings(publish / skip := true, name := "retry", moduleName := "retry-root", scalaVersion := scala213)
  .aggregate(retry.projectRefs*)

lazy val retry = (projectMatrix in file("retry"))
  .settings(
    scalacOptions += "-feature",
    moduleName := "retry",
    name := "retry",
    description := "a library of simple primitives for asynchronously retrying Scala Futures",
    libraryDependencies ++=
      Seq(
        "org.scalatest" %% "scalatest" % "3.2.18" % "test",
        "com.softwaremill.odelay" %% "odelay-core" % "0.4.0",
        "org.scala-lang.modules" %% "scala-collection-compat" % "2.12.0"
      )
  )
  .jvmPlatform(
    scalaVersions = List(scala212, scala213, scala30),
    settings = Seq(
      scalacOptions ++= (if (ScalaArtifacts.isScala3(scalaVersion.value)) Seq.empty else Seq("-release", "8"))
    )
  )
  .jsPlatform(
    scalaVersions = List(scala212, scala213, scala30),
    settings = Seq(
      libraryDependencies += "io.github.cquiroz" %% "scala-java-time" % "2.5.0"
    )
  )
