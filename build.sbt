val scala211 = "2.11.12"
val scala212 = "2.12.12"
val scala213 = "2.13.2"
val scala30 = "3.0.0"

val commonSettings = commonSmlBuildSettings ++ ossPublishSettings

lazy val retry = (projectMatrix in file("retry"))
  .settings(commonSettings: _*)
  .settings(
    scalacOptions += "-feature",
    organization := "com.softwaremill.retry",
    moduleName := "retry",
    name := "retry",
    description := "a library of simple primitives for asynchronously retrying Scala Futures",
    libraryDependencies ++=
      Seq("org.scalatest" %%% "scalatest" % "3.2.9" % "test",
        "com.softwaremill.odelay" %%% "odelay-core" % "0.3.3",
        "org.scala-lang.modules" %%% "scala-collection-compat" % "2.6.0"
      )
  )
  .jvmPlatform(
    scalaVersions = List(scala211, scala212, scala213, scala30)
  )
  .jsPlatform(
    scalaVersions = List(scala212, scala213, scala30)
  )
