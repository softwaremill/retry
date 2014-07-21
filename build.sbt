organization := "me.lessis"

name := "retry"

version := "0.2.0"

description := "a library of simple primitives for asynchronously retrying Scala Futures"

crossScalaVersions := Seq("2.10.4", "2.11.1")

libraryDependencies ++= Seq(
  "me.lessis" %% "odelay-core" % "0.1.0",
  "org.scalatest" %% "scalatest" % "2.2.0" % "test")

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

bintraySettings

bintray.Keys.packageLabels in bintray.Keys.bintray := (LsKeys.tags in LsKeys.lsync).value

resolvers += bintray.Opts.resolver.mavenRepo("softprops")
