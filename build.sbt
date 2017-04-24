import BintrayPlugin.autoImport._

organization := "me.lessis"

name := "retry"

version := "0.3.0"

description := "a library of simple primitives for asynchronously retrying Scala Futures"

crossScalaVersions := Seq("2.10.5", "2.11.6", "2.12.1")
scalaVersion in ThisBuild := crossScalaVersions.value.last

val libs = Seq(
  "me.lessis" %% "odelay-core" % "0.2.0",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test")

// remove me
resolvers += Resolver.file("Local", file( Path.userHome.absolutePath + "/.ivy2/local"))(Resolver.ivyStylePatterns)

lazy val retry = (crossProject in file ("."))
  .settings(libraryDependencies ++= libs,
    scalacOptions += "-feature")

lazy val retryJs = retry.js
lazy val retryJvm = retry.jvm

licenses :=
  Seq("MIT" ->
      url(s"https://github.com/softprops/${name.value}/blob/${version.value}/LICENSE"))

homepage := Some(url(s"https://github.com/softprops/${name.value}/"))

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

LsKeys.tags in LsKeys.lsync := Seq("future", "retry")

bintrayPackageLabels := (LsKeys.tags in LsKeys.lsync).value

resolvers += sbt.Resolver.bintrayRepo("softprops","maven")

externalResolvers in LsKeys.lsync := (resolvers in bintray).value
