organization := "me.lessis"

name := "retry"

version := "0.2.0-SNAPSHOT"

crossScalaVersions := Seq("2.9.3", "2.10.3")

scalaVersion := crossScalaVersions.value.head

licenses <<= version(v =>
  Seq("MIT" ->
      url("https://github.com/softprops/retry/blob/%s/LICENSE" format v)))

homepage := Some(url("https://github.com/softprops/retry/"))

publishTo := Some(Opts.resolver.sonatypeStaging)

publishArtifact in Test := false

publishMavenStyle := true

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
  </developers>)

lsSettings

LsKeys.tags in LsKeys.lsync := Seq("github", "gist")

libraryDependencies ++= Seq(
  "me.lessis" %% "odelay-core" % "0.1.0-SNAPSHOT",
  "org.scalatest" %% "scalatest" % "1.9.2" % "test")

description := "a library of simple abstractions for asynchronously retrying failed scala.concurrent.Futures"

