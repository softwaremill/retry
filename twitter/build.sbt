organization := "me.lessis"

name := "retry-twitter"

libraryDependencies ++= Seq(
  "com.twitter" %% "util-core"  % "6.3.4",
  "com.twitter" %% "util-zk"  % "6.3.4",
  "com.twitter" %% "bijection-util" % "0.5.2" exclude("org.scalacheck", "scalacheck_2.10") exclude("org.scala-tools.testing", "specs_2.10")
)

description := "provides a retry.Timer implementation backed by a com.twitter.util.Timer"

resolvers += "twitter" at "http://maven.twttr.com/"
