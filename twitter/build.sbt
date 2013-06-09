organization := "me.lessis"

name := "retry-twitter"

libraryDependencies += "com.twitter" % "util-core"  % "6.3.4"

resolvers += "twitter" at "http://maven.twttr.com/"

crossScalaVersions := Seq("2.10.0", "2.10.1")

scalaVersion := "2.10.0"
