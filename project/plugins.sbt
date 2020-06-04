addSbtPlugin("com.eed3si9n" % "sbt-projectmatrix" % "0.5.2")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.33")
addSbtPlugin("org.scala-native" % "sbt-scala-native" % "0.3.9")

val sbtSoftwaremillVersion = "1.9.5"
addSbtPlugin("com.softwaremill.sbt-softwaremill" % "sbt-softwaremill-common" % sbtSoftwaremillVersion)
addSbtPlugin("com.softwaremill.sbt-softwaremill" % "sbt-softwaremill-publish" % sbtSoftwaremillVersion)
