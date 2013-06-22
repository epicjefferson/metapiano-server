import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

    val appName         = "patchwerk"
    val appVersion      = "1.0"

    val appDependencies = Seq(
      jdbc,
      "com.h2database" % "h2" % "1.3.169",
      "org.squeryl" %% "squeryl" % "0.9.5-6",
      "postgresql" % "postgresql" % "9.1-901.jdbc4",
      "org.mockito" % "mockito-core" % "1.9.5" % "test",
      "org.jboss.netty" % "netty" % "3.2.7.Final",
      "com.rumblesan" %% "scalapd" % "0.1-SNAPSHOT"
    )

    val main = play.Project(
      appName,
      appVersion,
      appDependencies
    ).settings(
      scalacOptions ++= Seq("-feature", "-language:_"),
      resolvers ++= Seq("jboss repo" at "http://repository.jboss.org/nexus/content/groups/public-jboss/")
    )

}
