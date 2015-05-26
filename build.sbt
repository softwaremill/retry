import BintrayPlugin.autoImport._

organization := "me.lessis"

name := "retry"

version := "0.2.1"

description := "a library of simple primitives for asynchronously retrying Scala Futures"

crossScalaVersions := Seq("2.10.5", "2.11.6")

libraryDependencies ++= Seq(
  "me.lessis" %% "odelay-core" % "0.1.0",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test")

scalacOptions += "-feature"

scalaVersion := crossScalaVersions.value.last

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
