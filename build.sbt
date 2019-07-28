val commonSettings = commonSmlBuildSettings ++ ossPublishSettings ++ Seq(
  scalacOptions += "-feature",
  organization := "com.softwaremill.retry",
  name := "retry",
  description := "a library of simple primitives for asynchronously retrying Scala Futures",
  crossScalaVersions := Seq("2.11.12", "2.12.6", "2.13.0"),
  scalaVersion in ThisBuild := crossScalaVersions.value.last,
)

lazy val retry = (crossProject in file ("."))
  .settings(commonSettings: _*)
  .jvmSettings(
    libraryDependencies ++=
      Seq("org.scalatest" %% "scalatest" % "3.0.8" % "test",
        "com.softwaremill.odelay" %% "odelay-core" % "0.3.1")
  )
  .jsSettings(
    libraryDependencies ++=
      Seq("org.scalatest" %%% "scalatest" % "3.0.8" % "test",
        "com.softwaremill.odelay" %%% "odelay-core" % "0.3.1")
  )

lazy val retryJs = retry.js
lazy val retryJvm = retry.jvm

// root project settings
commonSettings
publishArtifact := false
