import sbtrelease.ReleaseStateTransformations._

val commonSettings = Seq(
  scalacOptions += "-feature",
  organization := "com.softwaremill.retry",
  name := "retry",
  description := "a library of simple primitives for asynchronously retrying Scala Futures",
  crossScalaVersions := Seq("2.11.11", "2.12.4"),
  scalaVersion in ThisBuild := crossScalaVersions.value.last,
  licenses := Seq(
    ("MIT", url(s"https://github.com/softprops/odelay/blob/${version.value}/LICENSE"))),
  // publishing
  publishTo := Some(
    if (isSnapshot.value)
      Opts.resolver.sonatypeSnapshots
    else
      Opts.resolver.sonatypeStaging
  ),
  publishArtifact in Test := false,
  publishMavenStyle := true,
  scmInfo := Some(
    ScmInfo(url("https://github.com/softwaremill/retry"),
      "scm:git:git@github.com/softwaremill/retry.git")),
  developers := List(
    Developer("adamw", "Adam Warski", "", url("https://softwaremill.com")),
    Developer("softprops", "Doug Tangren", "", url("https://github.com/softprops"))),
  homepage := Some(url("http://softwaremill.com/open-source")),
  sonatypeProfileName := "com.softwaremill",
  // sbt-release
  releaseCrossBuild := true,
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  releaseProcess := Seq(
    checkSnapshotDependencies,
    inquireVersions,
    // publishing locally so that the pgp password prompt is displayed early
    // in the process
    releaseStepCommand("publishLocalSigned"),
    runClean,
    runTest,
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    publishArtifacts,
    setNextVersion,
    commitNextVersion,
    releaseStepCommand("sonatypeReleaseAll"),
    pushChanges
  )
)

lazy val retry = (crossProject in file ("."))
  .settings(commonSettings: _*)
  .jvmSettings(
    libraryDependencies ++=
      Seq("org.scalatest" %% "scalatest" % "3.0.1" % "test",
        "com.softwaremill.odelay" %% "odelay-core" % "0.3.0")
  )
  .jsSettings(
    libraryDependencies ++=
      Seq("org.scalatest" %%% "scalatest" % "3.0.1" % "test",
        "com.softwaremill.odelay" %%% "odelay-core" % "0.3.0")
  )

lazy val retryJs = retry.js
lazy val retryJvm = retry.jvm

// root project settings
publishArtifact := false
