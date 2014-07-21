addSbtPlugin("me.lessis" % "ls-sbt" % "0.1.3")

//addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.2.0")

resolvers += Resolver.url(
  "bintray-sbt-plugin-releases",
    url("http://dl.bintray.com/content/sbt/sbt-plugin-releases"))(
        Resolver.ivyStylePatterns)

addSbtPlugin("me.lessis" % "bintray-sbt" % "0.1.2")
