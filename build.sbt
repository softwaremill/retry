val scala211 = "2.11.12"
val scala212 = "2.12.10"
val scala213 = "2.13.2"

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
      Seq("org.scalatest" %%% "scalatest" % "3.0.8" % "test",
        "com.softwaremill.odelay" %%% "odelay-core" % "0.3.1",
        "org.scala-lang.modules" %%% "scala-collection-compat" % "2.1.6"
      )
  )
  .jvmPlatform(
    scalaVersions = List(scala211, scala212, scala213)
  )
  .jsPlatform(
    scalaVersions = List(scala212, scala213)
  )
