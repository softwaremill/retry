organization := "me.lessis"

name := "retry-core"

libraryDependencies ++= Seq(
  "me.lessis" %% "odelay-core" % "0.1.0-SNAPSHOT",
  "org.scalatest" %% "scalatest" % "1.9.2" % "test")

description := "a library of simple abstractions for asynchronously retrying failed scala.concurrent.Futures"

