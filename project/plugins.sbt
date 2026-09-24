addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.22.0")

val sbtSoftwareMillVersion = "3.0.1"
addSbtPlugin("com.softwaremill.sbt-softwaremill" % "sbt-softwaremill-common" % sbtSoftwareMillVersion)
addSbtPlugin("com.softwaremill.sbt-softwaremill" % "sbt-softwaremill-publish" % sbtSoftwareMillVersion)

addSbtPlugin("org.jetbrains.scala" % "sbt-ide-settings" % "1.1.4")
