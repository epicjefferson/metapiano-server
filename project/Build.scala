import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "patchwerk"
    val appVersion      = "1.0"

    val appDependencies = Seq(
      "org.jboss.netty" % "netty" % "3.2.7.Final"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
      resolvers ++= Seq("jboss repo" at "http://repository.jboss.org/nexus/content/groups/public-jboss/")
    )

}
